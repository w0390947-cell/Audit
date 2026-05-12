# AI任务详情-执行流程组件第二阶段实施方案

## 1. 背景

第一阶段会用现有 `audit_ai_task` 数据在前端推导 AI 执行流程。该方案能快速纠正页面语义，但无法展示真实工作流内部步骤。

第二阶段目标是将 AI 工作流执行过程结构化落库，并由详情接口返回真实步骤数据，使 `AI任务详情` 页面能够展示准确的执行链路、状态、耗时、日志和产物。

## 2. 目标

为 AI 任务提供真实执行流程数据，支持页面展示：

- 每个 AI 工作流步骤的名称。
- 每个步骤的执行状态。
- 开始时间、结束时间、耗时。
- 关联智能体或工作流节点。
- 步骤摘要。
- 关键输出。
- 错误信息。
- 可选处理日志。

## 3. 范围

本阶段涉及：

- 数据库新增 AI 执行步骤表。
- 后端领域对象、Mapper、Service、Controller 返回结构扩展。
- AI 工作流回调解析和持久化。
- 前端 `detail.vue` 读取真实 `flowStageList` 并渲染。

不建议在本阶段同时重构：

- 报告预览服务。
- 检测结果展示卡片。
- 人工审核接口。
- 队列页统计逻辑。

## 4. 现有协议基线

实施前必须先和现有代码对齐，避免业务系统与工作流系统协议错位。

当前业务系统已有约定：

- 工作流回调地址为 `POST /audit/ai/workflow/callback`。
- 创建工作流任务时 `biz_id` 使用 `AI-TASK-{aiTaskId}` 格式。
- 当前回调 DTO 识别字段包括 `task_id`、`task_no`、`workflow_code`、`biz_id`、`task_status`、`result_url`、`finished_at`、`error`。
- 当前工作流终态成功值为 `SUCCESS`，失败值为 `FAILED` 或 `CANCELED`。
- 当前 `audit_ai_task.task_status` 字典只有 `waiting`、`executing`、`paused`、`completed`。失败时业务任务当前落库为 `paused`，不是 `failed`。

第二阶段可以扩展回调字段，但不应直接废弃现有字段。推荐业务系统兼容两套字段：

| 语义 | 现有字段 | 第二阶段扩展字段 |
| --- | --- | --- |
| 工作流任务ID | `task_id` | `workflow_task_id` |
| 工作流任务编号 | `task_no` | `workflow_task_no` |
| 工作流状态 | `task_status` (`SUCCESS/FAILED/RUNNING`) | `status` (`completed/failed/running`) |
| 结果查询地址 | `result_url` | 可选保留 |
| 节点明细 | 无 | `stages` |
| 最终结果 | 主动查询 `/result` | `result` |
| 回调幂等ID | 无 | `callback_event_id` |

## 5. 推荐数据模型

### 5.1 新增表：`audit_ai_flow_stage`

建议新增迁移脚本：

```text
backend/sql/audit_ai_flow_stage_migration.sql
```

表结构建议：

```sql
CREATE TABLE IF NOT EXISTS audit_ai_flow_stage (
  stage_id bigint NOT NULL AUTO_INCREMENT COMMENT '阶段主键',
  ai_task_id bigint NOT NULL COMMENT 'AI任务主键',
  run_id varchar(100) NOT NULL COMMENT '本次执行标识，默认使用workflow_task_id',
  workflow_task_id varchar(100) DEFAULT NULL COMMENT '工作流任务ID',
  workflow_task_no varchar(100) DEFAULT NULL COMMENT '工作流任务编号',
  stage_code varchar(64) NOT NULL COMMENT '阶段编码',
  stage_instance_id varchar(100) DEFAULT NULL COMMENT '阶段实例ID，工作流有重复节点时使用',
  stage_name varchar(100) NOT NULL COMMENT '阶段名称',
  stage_status varchar(32) NOT NULL COMMENT '阶段状态',
  agent_name varchar(100) DEFAULT NULL COMMENT '智能体或节点名称',
  start_time datetime DEFAULT NULL COMMENT '开始时间',
  end_time datetime DEFAULT NULL COMMENT '结束时间',
  duration_ms bigint DEFAULT NULL COMMENT '耗时毫秒',
  stage_summary varchar(500) DEFAULT NULL COMMENT '阶段摘要',
  stage_detail text COMMENT '阶段详情',
  output_json longtext COMMENT '阶段输出JSON',
  error_message varchar(1000) DEFAULT NULL COMMENT '错误信息',
  sort_num int DEFAULT 0 COMMENT '排序',
  create_by varchar(64) DEFAULT '' COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT '' COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (stage_id),
  KEY idx_ai_task_id (ai_task_id),
  KEY idx_ai_task_run_stage (ai_task_id, run_id, stage_code),
  KEY idx_run_id (run_id),
  KEY idx_workflow_task_id (workflow_task_id),
  KEY idx_stage_code (stage_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI任务执行流程阶段表';
```

`run_id` 用于隔离同一个 AI 任务的多次重跑。第一版推荐直接使用 `workflow_task_id`；如果工作流系统无法返回稳定任务 ID，则使用业务系统生成的 `AI-TASK-{aiTaskId}-{analysisNo}`。

不建议对 `(ai_task_id, run_id, stage_code)` 加唯一键，因为工作流可能返回多个同类节点，例如多个分片比对节点。重复回调幂等由 `audit_workflow_callback_event` 保证；同一 `run_id` 的阶段更新建议采用“先删除该 run 阶段，再批量插入”的方式。

### 5.2 新增表：`audit_workflow_callback_event`

为避免重复回调导致发现项重复写入或 `ai_analysis_count` 虚增，建议新增回调事件表：

```sql
CREATE TABLE IF NOT EXISTS audit_workflow_callback_event (
  event_id bigint NOT NULL AUTO_INCREMENT COMMENT '事件主键',
  callback_event_id varchar(100) NOT NULL COMMENT '工作流回调事件ID',
  ai_task_id bigint NOT NULL COMMENT 'AI任务主键',
  workflow_task_id varchar(100) DEFAULT NULL COMMENT '工作流任务ID',
  event_status varchar(20) NOT NULL DEFAULT 'processing' COMMENT 'processing/processed/ignored/failed',
  raw_payload longtext COMMENT '回调原文JSON',
  error_message varchar(1000) DEFAULT NULL COMMENT '处理失败原因',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (event_id),
  UNIQUE KEY uk_callback_event_id (callback_event_id),
  KEY idx_ai_task_workflow (ai_task_id, workflow_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工作流回调事件幂等表';
```

如果工作流系统短期不能提供 `callback_event_id`，业务系统可临时使用 `{workflow_task_id}:{status}:{finished_at}` 作为去重键，但这只能作为过渡方案。

### 5.3 阶段状态枚举

建议后端统一使用：

| 状态 | 含义 |
| --- | --- |
| pending | 未开始 |
| waiting | 等待中 |
| running | 执行中 |
| completed | 已完成 |
| failed | 已失败 |
| skipped | 已跳过 |

阶段状态可以使用 `failed`。但 `audit_ai_task.task_status` 第一版不新增 `failed` 时，整体失败仍映射为 `paused`，错误原因写入 `progress_text`，失败节点写入 `audit_ai_flow_stage.stage_status = failed`。

## 6. 后端对象设计

### 6.1 新增领域对象

建议新增：

```text
backend/ruoyi-system/src/main/java/com/ruoyi/system/domain/audit/AuditAiFlowStage.java
```

字段与 `audit_ai_flow_stage` 表保持一致。

### 6.2 扩展 `AuditAiTask`

在 `AuditAiTask` 中增加：

```java
private List<AuditAiFlowStage> flowStageList;
```

用于详情接口一次性返回任务和流程阶段。

### 6.3 新增 Mapper

建议新增：

```text
backend/ruoyi-system/src/main/java/com/ruoyi/system/mapper/audit/AuditAiFlowStageMapper.java
backend/ruoyi-system/src/main/resources/mapper/audit/AuditAiFlowStageMapper.xml
```

核心方法：

```java
List<AuditAiFlowStage> selectAuditAiFlowStageListByTaskId(Long aiTaskId);

int deleteAuditAiFlowStageByTaskId(Long aiTaskId);

int deleteAuditAiFlowStageByTaskIdAndRunId(@Param("aiTaskId") Long aiTaskId, @Param("runId") String runId);

int insertAuditAiFlowStage(AuditAiFlowStage stage);

int insertAuditAiFlowStageBatch(List<AuditAiFlowStage> list);
```

`selectAuditAiFlowStageListByTaskId` 默认只返回最新一次执行数据。推荐查询规则为：按 `audit_ai_flow_stage.update_time` 或 `create_time` 找到该任务最新 `run_id`，再返回该 `run_id` 下的阶段列表。

### 6.4 扩展 AI 任务详情服务

当前服务：

```java
AuditAiTask selectAuditAiTaskDetail(Long aiTaskId)
```

建议扩展为：

```java
AuditAiTask task = auditAiMapper.selectAuditAiTaskById(aiTaskId);
task.setFindingList(auditAiMapper.selectAuditAiFindingListByTaskId(aiTaskId));
task.setFlowStageList(auditAiFlowStageMapper.selectAuditAiFlowStageListByTaskId(aiTaskId));
```

接口路径保持不变：

```text
GET /audit/ai/{aiTaskId}
```

这样前端不用新增接口。

## 7. 工作流结果解析

### 7.1 数据来源

第二阶段应优先从 AI 工作流回调或主动查询结果中解析步骤数据。当前相关后端入口包括：

- `AuditWorkflowAuditServiceImpl`
- `AuditWorkflowCallbackController`
- `AuditAiAnalysisPersistenceServiceImpl`

第二阶段推荐以工作流回调为主。如果继续保留当前同步轮询逻辑，必须加幂等保护，避免同步轮询保存一次、终态回调再保存一次。

建议在工作流完成回调中完成：

1. 根据 `bizId` 解析 `aiTaskId`。
2. 查询 AI 任务。
3. 校验 `callback_event_id` 是否已处理，已处理则直接返回成功。
4. 解析 `workflow_task_id/task_id` 得到 `run_id`。
5. 如果当前任务已被新的 `run_id` 重跑，忽略旧回调或只记录事件，不覆盖页面展示数据。
6. 解析工作流返回的步骤数据。
7. 在同一事务中保存 `audit_ai_flow_stage`、`audit_ai_finding`，并更新 `audit_ai_task` 状态。
8. 写入 `audit_workflow_callback_event` 处理结果。

### 7.2 解析输出结构建议

无论工作流原始响应格式如何，后端应统一转换为：

```json
[
  {
    "stageCode": "preprocess",
    "stageInstanceId": "node-preprocess-1",
    "stageName": "报告预处理",
    "stageStatus": "completed",
    "agentName": "预处理智能体",
    "startTime": "2026-05-11 10:00:00",
    "endTime": "2026-05-11 10:00:30",
    "durationMs": 30000,
    "stageSummary": "完成报告文本抽取和结构化解析",
    "stageDetail": "抽取文本 12000 字；识别核心字段 18 个",
    "outputJson": "{}",
    "errorMessage": null,
    "sortNum": 20
  }
]
```

### 7.3 推荐阶段编码

| stageCode | stageName | 说明 |
| --- | --- | --- |
| queued | 任务入队 | AI 任务创建并进入队列 |
| preprocess | 报告预处理 | 报告文本抽取、格式转换、结构化 |
| retrieval | 依据检索 | 检索审核依据、标准和相关资料 |
| compare | 依据比对 | 报告内容与依据文件比对 |
| generate | 结果生成 | 生成问题、建议和摘要 |

如果工作流实际节点与以上不同，应以工作流节点为准，但后端仍建议保持稳定 `stageCode`。

`人工复核` 属于业务系统动作，不要求工作流系统返回。页面需要展示人工复核时，由前端继续基于 `reviewStatus/reviewOpinion/reviewer` 追加本地阶段，或由业务系统在详情 DTO 中单独追加业务阶段。

## 8. 前端实现建议

### 8.1 数据优先级

`detail.vue` 中执行流程数据优先级：

1. 如果 `detail.flowStageList` 非空，渲染真实步骤。
2. 如果为空，回退到第一阶段前端推导步骤。

示例：

```js
displayAiFlowStageList() {
  if (Array.isArray(this.detail.flowStageList) && this.detail.flowStageList.length) {
    return this.detail.flowStageList.map(this.normalizeFlowStage)
  }
  return this.aiFlowStageList
}
```

### 8.2 标准化前端阶段对象

后端字段映射到前端：

| 后端字段 | 前端字段 |
| --- | --- |
| stageCode | stageCode |
| stageName | stageName |
| stageStatus | status，需转换 |
| agentName | agentName |
| startTime/endTime | timeText |
| stageSummary | summary |
| stageDetail | lines |
| errorMessage | errorMessage |

状态转换规则：

| 后端 `stageStatus` | 前端 `status` |
| --- | --- |
| pending | pending |
| waiting | waiting |
| running | running |
| completed | done |
| failed | failed |
| skipped | paused |

不要直接把 `completed` 传给当前前端 `status`，否则现有样式无法命中 `stage-done`。

### 8.3 日志展示

第二阶段可以恢复 `处理日志` 按钮。

建议交互：

- 点击 `处理日志` 打开 `el-dialog`。
- 展示 `stageDetail`、`outputJson`、`errorMessage`。
- `outputJson` 使用格式化 JSON 只读展示。

如果日志数据过大，后续可拆分独立接口：

```text
GET /audit/ai/{aiTaskId}/flowStage/{stageId}
```

第一版可以不拆分接口。

## 9. API 返回示例

`GET /audit/ai/{aiTaskId}` 响应中的 `data` 建议增加：

```json
{
  "aiTaskId": 1001,
  "taskNo": "SF-202605110001",
  "taskStatus": "completed",
  "progressPercent": 100,
  "progressText": "AI分析完成",
  "flowStageList": [
    {
      "stageId": 1,
      "aiTaskId": 1001,
      "stageCode": "queued",
      "stageName": "任务入队",
      "stageStatus": "completed",
      "agentName": "AI队列调度器",
      "startTime": "2026-05-11 10:00:00",
      "endTime": "2026-05-11 10:00:01",
      "durationMs": 1000,
      "stageSummary": "任务已进入AI分析队列",
      "stageDetail": "优先级：高优先级；队列位置：第1位",
      "sortNum": 10
    }
  ],
  "findingList": []
}
```

## 10. 兼容策略

为避免影响已有任务：

- 旧任务没有 `flowStageList` 时，前端继续使用第一阶段推导步骤。
- 新任务在工作流回调后写入真实步骤。
- 手动重新分析时，必须生成新的 `run_id`，并让详情接口只展示最新 `run_id` 的阶段。
- 是否删除旧 `flowStageList` 由产品决定。第一版可保留历史数据但默认不展示。
- 分析失败时，也应写入至少一个失败阶段，便于页面展示原因。
- 业务任务失败状态短期仍使用 `paused`，不要写入当前字典不存在的 `failed`。

## 11. 迁移步骤

建议按以下顺序实施：

1. 新增 SQL 迁移脚本。
2. 新增 `AuditAiFlowStage` 和 `AuditWorkflowCallbackEvent` 领域对象。
3. 新增 Mapper 和 XML。
4. 扩展 `AuditAiTask.flowStageList`。
5. 修改 `AuditAiServiceImpl.selectAuditAiTaskDetail` 返回最新 `run_id` 的流程阶段。
6. 扩展 `AuditWorkflowCallback` DTO，兼容 `task_id/task_status` 与 `workflow_task_id/status/stages/result/callback_event_id`。
7. 修改工作流回调持久化逻辑，先幂等判断，再写入流程阶段和结果。
8. 扩展结果解析，兼容 `type/title/content` 与 `finding_type/finding_title/finding_content`。
9. 梳理同步轮询与回调的职责，避免同一次工作流执行重复保存结果。
10. 修改 `detail.vue` 优先渲染真实阶段，并完成后端状态到前端状态的转换。
11. 增加失败、重复回调、旧回调、空结果和重跑场景验证。

## 12. 验收标准

### 12.1 已完成任务

- 详情接口返回 `flowStageList`。
- 页面展示真实步骤名称、状态、时间和摘要。
- 不再展示前端推导的假步骤。

### 12.2 执行失败任务

- 至少有一个阶段状态为 `failed`。
- 页面展示错误信息。
- 如果未新增业务任务 `failed` 字典，`audit_ai_task.task_status` 应为 `paused`，失败阶段为 `failed`，二者语义需在页面上保持一致。

### 12.3 老任务兼容

- 没有 `flowStageList` 的旧任务仍能展示第一阶段推导流程。
- 页面不报错。

### 12.4 手动重新分析

- 重新分析后产生新的 `run_id`。
- 旧回调不能覆盖新 `run_id` 的页面展示结果。
- 页面展示本次最新执行流程。

### 12.5 重复回调

- 重复发送同一个 `callback_event_id` 时，业务系统返回成功但不重复处理。
- `audit_ai_finding` 不重复插入。
- `audit_ai_task.ai_analysis_count` 不因重复回调增加。

## 13. 风险和注意事项

### 13.1 工作流响应结构不稳定

如果工作流响应字段仍在变化，应增加解析适配层，不要让前端依赖原始响应结构。

### 13.2 日志体积过大

`outputJson` 可能很大。首版可以只返回摘要字段，日志详情后续拆独立接口。

### 13.3 状态一致性

需要保证：

- `audit_ai_task.task_status`
- `audit_ai_task.progress_percent`
- `audit_ai_flow_stage.stage_status`

三者语义一致，否则页面会出现一个地方成功、另一个地方失败的矛盾展示。

### 13.4 重试和历史

本方案已经要求增加 `run_id`。第一版默认只展示最新一次执行流程，历史执行数据可先只保留在表中，不做页面入口。

### 13.5 回调安全

当前 Controller 为匿名回调入口。第二阶段至少应继续支持 `Authorization: Bearer {callbackToken}`，如需更高安全性再扩展 HMAC 签名。无论采用哪种方式，鉴权失败的回调不能写入事件表为已处理。
