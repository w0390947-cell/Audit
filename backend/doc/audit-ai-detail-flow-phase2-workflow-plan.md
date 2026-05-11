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

## 4. 推荐数据模型

### 4.1 新增表：`audit_ai_flow_stage`

建议新增迁移脚本：

```text
backend/sql/audit_ai_flow_stage_migration.sql
```

表结构建议：

```sql
CREATE TABLE IF NOT EXISTS audit_ai_flow_stage (
  stage_id bigint NOT NULL AUTO_INCREMENT COMMENT '阶段主键',
  ai_task_id bigint NOT NULL COMMENT 'AI任务主键',
  workflow_task_id varchar(100) DEFAULT NULL COMMENT '工作流任务ID',
  workflow_task_no varchar(100) DEFAULT NULL COMMENT '工作流任务编号',
  stage_code varchar(64) NOT NULL COMMENT '阶段编码',
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
  KEY idx_workflow_task_id (workflow_task_id),
  KEY idx_stage_code (stage_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI任务执行流程阶段表';
```

### 4.2 阶段状态枚举

建议后端统一使用：

| 状态 | 含义 |
| --- | --- |
| pending | 未开始 |
| waiting | 等待中 |
| running | 执行中 |
| completed | 已完成 |
| failed | 已失败 |
| skipped | 已跳过 |

前端可映射为第一阶段定义的状态样式。

## 5. 后端对象设计

### 5.1 新增领域对象

建议新增：

```text
backend/ruoyi-system/src/main/java/com/ruoyi/system/domain/audit/AuditAiFlowStage.java
```

字段与 `audit_ai_flow_stage` 表保持一致。

### 5.2 扩展 `AuditAiTask`

在 `AuditAiTask` 中增加：

```java
private List<AuditAiFlowStage> flowStageList;
```

用于详情接口一次性返回任务和流程阶段。

### 5.3 新增 Mapper

建议新增：

```text
backend/ruoyi-system/src/main/java/com/ruoyi/system/mapper/audit/AuditAiFlowStageMapper.java
backend/ruoyi-system/src/main/resources/mapper/audit/AuditAiFlowStageMapper.xml
```

核心方法：

```java
List<AuditAiFlowStage> selectAuditAiFlowStageListByTaskId(Long aiTaskId);

int deleteAuditAiFlowStageByTaskId(Long aiTaskId);

int insertAuditAiFlowStage(AuditAiFlowStage stage);

int insertAuditAiFlowStageBatch(List<AuditAiFlowStage> list);
```

### 5.4 扩展 AI 任务详情服务

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

## 6. 工作流结果解析

### 6.1 数据来源

第二阶段应优先从 AI 工作流回调或主动查询结果中解析步骤数据。当前相关后端入口包括：

- `AuditWorkflowAuditServiceImpl`
- `AuditWorkflowCallbackController`
- `AuditAiAnalysisPersistenceServiceImpl`

建议在工作流完成回调中完成：

1. 根据 `bizId` 解析 `aiTaskId`。
2. 查询 AI 任务。
3. 解析工作流返回的步骤数据。
4. 保存 `audit_ai_flow_stage`。
5. 保存 `audit_ai_finding`。
6. 更新 `audit_ai_task` 状态。

### 6.2 解析输出结构建议

无论工作流原始响应格式如何，后端应统一转换为：

```json
[
  {
    "stageCode": "preprocess",
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

### 6.3 推荐阶段编码

| stageCode | stageName | 说明 |
| --- | --- | --- |
| queued | 任务入队 | AI 任务创建并进入队列 |
| preprocess | 报告预处理 | 报告文本抽取、格式转换、结构化 |
| retrieval | 依据检索 | 检索审核依据、标准和相关资料 |
| compare | 依据比对 | 报告内容与依据文件比对 |
| generate | 结果生成 | 生成问题、建议和摘要 |
| review | 人工复核 | 人工处理 AI 结果 |

如果工作流实际节点与以上不同，应以工作流节点为准，但后端仍建议保持稳定 `stageCode`。

## 7. 前端实现建议

### 7.1 数据优先级

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

### 7.2 标准化前端阶段对象

后端字段映射到前端：

| 后端字段 | 前端字段 |
| --- | --- |
| stageCode | stageCode |
| stageName | stageName |
| stageStatus | status |
| agentName | agentName |
| startTime/endTime | timeText |
| stageSummary | summary |
| stageDetail | lines |
| errorMessage | errorMessage |

### 7.3 日志展示

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

## 8. API 返回示例

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

## 9. 兼容策略

为避免影响已有任务：

- 旧任务没有 `flowStageList` 时，前端继续使用第一阶段推导步骤。
- 新任务在工作流回调后写入真实步骤。
- 手动重新分析时，建议先删除旧 `flowStageList`，再写入新步骤。
- 分析失败时，也应写入至少一个失败阶段，便于页面展示原因。

## 10. 迁移步骤

建议按以下顺序实施：

1. 新增 SQL 迁移脚本。
2. 新增 `AuditAiFlowStage` 领域对象。
3. 新增 Mapper 和 XML。
4. 扩展 `AuditAiTask.flowStageList`。
5. 修改 `AuditAiServiceImpl.selectAuditAiTaskDetail` 返回流程阶段。
6. 修改工作流回调持久化逻辑，写入流程阶段。
7. 修改 `detail.vue` 优先渲染真实阶段。
8. 增加失败和空数据场景验证。

## 11. 验收标准

### 11.1 已完成任务

- 详情接口返回 `flowStageList`。
- 页面展示真实步骤名称、状态、时间和摘要。
- 不再展示前端推导的假步骤。

### 11.2 执行失败任务

- 至少有一个阶段状态为 `failed`。
- 页面展示错误信息。
- 总任务状态和阶段状态一致。

### 11.3 老任务兼容

- 没有 `flowStageList` 的旧任务仍能展示第一阶段推导流程。
- 页面不报错。

### 11.4 手动重新分析

- 重新分析后旧阶段数据被清理或标记为历史。
- 页面展示本次最新执行流程。

## 12. 风险和注意事项

### 12.1 工作流响应结构不稳定

如果工作流响应字段仍在变化，应增加解析适配层，不要让前端依赖原始响应结构。

### 12.2 日志体积过大

`outputJson` 可能很大。首版可以只返回摘要字段，日志详情后续拆独立接口。

### 12.3 状态一致性

需要保证：

- `audit_ai_task.task_status`
- `audit_ai_task.progress_percent`
- `audit_ai_flow_stage.stage_status`

三者语义一致，否则页面会出现一个地方成功、另一个地方失败的矛盾展示。

### 12.4 重试和历史

如果业务需要保留每次分析历史，应增加 `run_id` 或 `analysis_no` 字段。当前方案默认只展示最新一次执行流程。
