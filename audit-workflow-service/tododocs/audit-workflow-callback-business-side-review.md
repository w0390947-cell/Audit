# AI审核工作流回调契约业务系统侧核对说明

日期：2026-05-12

## 0. 2026-05-12 更新说明

业务系统侧已根据上一轮核对意见更新 `tododocs/audit-workflow-callback-stage-contract.md`，新版契约已经明确：

- `biz_id` 必须为 `AI-TASK-${aiTaskId}`。
- `workflow_task_id` 必填，并作为稳定 `run_id`。
- 终态回调发送到 `/audit/ai/workflow/callback`。
- 运行中阶段回调发送到 `/audit/ai/workflow/stageCallback`。
- `/stageCallback` 按 `stage_instance_id` 优先、否则按 `stage_code` 增量合并阶段。
- 处理成功的重复 `callback_event_id` 忽略；处理失败的同 ID 事件允许重试。
- 失败终态回调会将 AI 任务状态标记为 `failed`。

工作流系统侧已完成首轮适配，代码位置为 `/mnt/d/TestEnvironment/PythonENV2/WorkflowforJava`。本次适配范围：

- 终态 `/callback` payload 补齐 `callback_event_id`、`callback_time`、`workflow_task_id`、`workflow_task_no`、`status`、`progress_percent`、`progress_text`、`started_at`、`finished_at`、`duration_ms`、`stages`、内嵌 `result`、标准化 `error`。
- 终态回调会从 `audit_task_node_log` 汇总生成 `stages`。
- 新增运行中阶段回调发送逻辑：节点开始发送 `RUNNING`，节点成功发送 `SUCCESS`，节点失败发送 `FAILED`。
- 阶段回调地址由 `callback_url` 自动派生：`/callback` 替换为 `/stageCallback`。
- 补齐部分阶段 `output` 指标：`input_validate` 增加 `report_file_count`、`basis_file_count`；`basis_file_parse` 增加 `file_count`、`char_count`、`block_count`。

本轮工作流系统侧修改文件：

- `src/main/java/com/audit/workflow/service/CallbackService.java`
- `src/main/java/com/audit/workflow/service/WorkflowEngine.java`
- `src/main/java/com/audit/workflow/repository/AuditTaskNodeLogRepository.java`
- `src/main/java/com/audit/workflow/node/InputValidateNodeExecutor.java`
- `src/main/java/com/audit/workflow/node/BasisFileParseNodeExecutor.java`

已执行编译验证：

```bash
mvn -DskipTests compile
```

结果：编译通过。

### 0.1 当前工作流侧字段策略

`biz_id`：

工作流系统侧不主动改写 `biz_id`，沿用业务系统创建工作流任务时传入的值。因此业务系统创建任务时必须传：

```text
AI-TASK-${aiTaskId}
```

`workflow_task_id`：

当前工作流系统侧使用内部任务主键：

```text
workflow_task_id = String.valueOf(task_id)
```

该值对同一次工作流执行稳定。若业务侧希望“重新分析”产生新的运行批次，需要工作流系统创建新的任务实例。

`callback_event_id`：

当前生成规则：

```text
wf-audit-${taskId}-${retryCount}-${eventType}
```

示例：

```text
wf-audit-123-0-success
wf-audit-123-0-failed
wf-audit-123-0-stage-ai_audit-running
wf-audit-123-0-stage-ai_audit-success
```

回调日志重试会复用原始 payload，因此会沿用同一个 `callback_event_id`。

### 0.2 仍需业务系统侧联调确认

请业务系统侧重点确认：

1. `/audit/ai/workflow/stageCallback` 已完成部署，并且接收 `task_status=RUNNING`、`status=running` 时只更新阶段和进度，不触发终态失败逻辑。
2. 阶段回调失败不会影响最终 `/callback` 终态回调处理。
3. 业务系统接受 `workflow_task_id = task_id` 作为同一次执行的稳定 runId。
4. 业务系统回调地址保持 `/audit/ai/workflow/callback` 形式，便于工作流系统自动派生 `/stageCallback`。
5. 失败终态回调可以将业务系统 AI 任务状态落为 `failed`，并能展示失败阶段。
6. 同一个 `callback_event_id` 如果上次处理状态为 `failed`，业务系统允许重试重放。

### 0.3 建议下一步联调顺序

建议按以下顺序联调：

1. 创建一个最小 AI 任务，确认工作流系统收到的 `biz_id` 为 `AI-TASK-${aiTaskId}`。
2. 观察 `/stageCallback` 是否能收到 `input_validate` 的 `RUNNING` 和 `SUCCESS` 回调。
3. 观察详情页“流转状态”是否能随阶段回调增量刷新。
4. 等待工作流成功结束，确认 `/callback` 终态成功回调包含完整 `stages` 和 `result`。
5. 人为制造一个节点失败，确认 `/callback` 终态失败回调能把业务系统 AI 任务标记为 `failed`，并展示失败阶段。
6. 人为让业务系统第一次处理回调失败，再重试同一 `callback_event_id`，确认业务系统侧允许 failed 事件重放。

> 以下内容为上一轮业务系统侧代码核对记录，主要用于保留问题来源和调整依据。新版契约及工作流系统侧适配状态以上述“2026-05-12 更新说明”为准。

本文档基于业务系统代码 `/mnt/d/TestEnvironment/PythonENV/Audit` 对 `tododocs/audit-workflow-callback-stage-contract.md` 进行了实现核对，用于反馈给业务系统侧确认。结论是：阶段展示、结果解析、终态回调判断等主体逻辑与契约基本一致，但 `biz_id` 前缀、失败状态描述和回调幂等重试语义存在需要业务系统侧确认或修正的差异。

## 1. 核对范围

核对的业务系统代码主要包括：

- 回调入口：`backend/ruoyi-admin/src/main/java/com/ruoyi/web/controller/audit/AuditWorkflowCallbackController.java`
- 回调处理：`backend/ruoyi-system/src/main/java/com/ruoyi/system/service/audit/impl/AuditWorkflowAuditServiceImpl.java`
- 阶段持久化：`backend/ruoyi-system/src/main/resources/mapper/audit/AuditAiFlowStageMapper.xml`
- 回调幂等表：`backend/sql/audit_ai_flow_stage_migration.sql`
- AI任务详情前端：`ruoyi-ui/src/views/audit/ai/detail.vue`

## 2. 与当前实现一致的内容

### 2.1 回调接口路径一致

业务系统当前回调接口为：

```http
POST /audit/ai/workflow/callback
```

控制器配置为：

- `@RequestMapping("/audit/ai/workflow")`
- `@PostMapping("/callback")`
- `@Anonymous`

因此文档中描述的回调路径与当前业务系统实现一致。

### 2.2 Authorization 校验逻辑一致

业务系统当前逻辑为：

- 如果 `audit-workflow.callback-token` 为空，则不校验 `Authorization`。
- 如果配置了 `callback-token`，则必须完全等于 `Bearer ${callbackToken}`。

当前 `application.yml` 中 `callback-token` 为空，因此本地环境默认不校验回调 Token。

### 2.3 当前接口确实只适合终态回调

业务系统当前只把以下回调视为成功：

- `task_status = SUCCESS`
- 或 `status = completed`

除以上情况外，都会进入失败处理分支。因此文档中提醒“不要直接发送 running 任务级回调，否则会被业务系统误判为失败回调”是准确的。

### 2.4 `workflow_task_id` 与 `run_id` 逻辑一致

业务系统会优先使用 `workflow_task_id` 作为 `run_id`。如果缺少 `workflow_task_id`，才会根据 `task_id`、`result_url` 或时间字段生成兜底运行 ID。

同一个 `aiTaskId + runId` 的阶段处理方式是：

1. 删除原有阶段；
2. 批量插入本次回调中的阶段。

因此文档中“同一个 `aiTaskId + runId` 的阶段会整体替换”的描述与实现一致。

### 2.5 `stages` 字段映射基本一致

业务系统支持并落库以下阶段字段：

- `stage_code`
- `stage_instance_id`
- `stage_name`
- `stage_status`
- `agent_name`
- `started_at`
- `finished_at`
- `duration_ms`
- `summary`
- `detail`
- `output`
- `error`
- `sort_num`

阶段状态映射如下：

| 工作流回调值 | 业务系统落库值 |
| --- | --- |
| `SUCCESS` | `completed` |
| `FAILED` / `ERROR` | `failed` |
| `RUNNING` | `running` |
| `CANCELED` / `CANCELLED` | `skipped` |
| 空值 | `pending` |

时间字段支持：

- `yyyy-MM-dd HH:mm:ss`
- ISO-8601 带时区格式，例如 `2026-05-12T09:30:00+08:00`

### 2.6 前端四阶段归并规则一致

AI任务详情页“流转状态”当前归并规则如下：

| 前端业务阶段 | 归并的 `stage_code` |
| --- | --- |
| 任务接收 | `queued`、`input_validate` |
| 内容解析 | `file_parse`、`target_file_parse`、`basis_file_parse`、`text_split`、`target_text_split` |
| 审核分析 | `knowledge_retrieve`、`uploaded_basis_match`、`basis_pack_or_match`、`ai_audit`、`result_validate` |
| 结果处理 | `result_save`、`callback` |

未匹配到以上枚举的阶段，会被合并到“审核分析”阶段展示。

前端读取的 `output` 指标也与契约文档基本一致：

- 内容解析：`char_count`、`block_count`、`chunk_count`
- 审核分析：`reference_count`、`references_used_in_prompt`、`basis_chunks_used_in_prompt`、`model_call_count`、`finding_count`、`totalIssues`、`total_issues`
- 结果处理：`issue_count`

### 2.7 `result` 字段解析一致

成功回调中如果直接携带 `result`，业务系统会优先使用该字段，不再二次查询结果接口。

当前兼容字段包括：

- 问题数组：`findings` 或 `issues`
- 问题类型：`type` 或 `finding_type`
- 问题标题：`title` 或 `finding_title`
- 问题内容：`content`、`finding_content` 或 `problem`
- 严重程度：`severity` 或 `risk_level`

如果成功回调没有 `result`，业务系统会尝试通过 `task_id` 或 `result_url` 查询工作流结果。

## 3. 发现的不一致与风险点

### 3.1 关键不一致：`biz_id` 前缀

当前契约文档写的是：

```text
biz_id = audit-ai-${aiTaskId}
```

但业务系统代码实际要求：

```text
biz_id = AI-TASK-${aiTaskId}
```

业务系统创建工作流任务时发送的也是 `AI-TASK-${aiTaskId}`，回调解析时也只接受 `AI-TASK-` 前缀。

影响：

- 如果工作流系统按当前契约文档回调 `audit-ai-123`，业务系统会直接拒绝，报“不支持的 biz_id”。
- 当前契约文档必须修正，或业务系统代码必须同步改造为兼容 `audit-ai-`。

建议：

优先修正文档，将所有 `audit-ai-${aiTaskId}` 改为 `AI-TASK-${aiTaskId}`。如果业务系统侧希望使用更语义化的小写前缀，则需要先改造业务系统代码，使其兼容新旧两种前缀。

### 3.2 失败回调后的任务状态描述不准确

当前契约文档描述为：非成功回调会把 AI 任务标记为失败。

但业务系统当前 SQL 实现为：

```sql
set task_status = 'paused',
    progress_text = #{progressText}
```

也就是说，当前业务系统实际将失败回调后的 AI 任务置为 `paused`，不是 `failed`。

影响：

- 文档里的“失败”可以理解为业务处理分支，但不是数据库状态值。
- 前端是否展示为失败、暂停或异常，需要结合当前页面状态映射再确认。

建议二选一：

1. 如果业务系统设计上希望失败回调后进入人工介入状态，则文档应改为“会被视为失败回调，并将 AI 任务置为暂停/异常待处理状态”。
2. 如果业务系统希望状态语义更直接，则应修改业务系统 SQL，将失败回调后的 `task_status` 改为 `failed`，并确认前端列表、详情、队列调度逻辑是否受影响。

### 3.3 `callback_event_id` 幂等重试语义有实现风险

契约文档建议：

```text
重试必须使用同一个 callback_event_id
```

该建议符合一般幂等设计。但当前业务系统实现是：

1. 先插入 `audit_workflow_callback_event`；
2. 如果插入时遇到唯一键冲突，则认为重复回调并直接忽略；
3. 即使第一次回调处理过程中异常，事件状态被标记为 `failed`，后续使用相同 `callback_event_id` 重试时仍会因为唯一键冲突而被忽略。

影响：

- 如果第一次回调已经入幂等表，但后续保存结果或阶段失败，工作流系统再用同一个 `callback_event_id` 重试，业务系统不会重新处理。
- 这会削弱“失败后重试可恢复”的能力。

建议：

业务系统侧应调整幂等处理逻辑：

- 如果已存在事件状态为 `processed`，可以忽略。
- 如果已存在事件状态为 `processing` 且未超时，可以忽略或返回处理中。
- 如果已存在事件状态为 `failed`，应允许重新处理，或至少提供人工重放机制。

### 3.4 `workflow_task_id` 缺失时会生成不稳定 runId

当前代码在缺少 `workflow_task_id` 时，会用 `finished_at`、`callback_time` 或当前时间生成兜底 `run_id`。

影响：

- 如果工作流系统不传 `workflow_task_id`，同一次运行的不同回调可能被识别为不同 `run_id`。
- 阶段替换和详情页读取最新运行都会变得不稳定。

建议：

继续保留契约中“`workflow_task_id` 建议必填”的要求，并建议工作流系统必须稳定传入。

## 4. 建议业务系统侧确认事项

请业务系统侧优先确认以下事项：

1. `biz_id` 是否确定继续使用当前实现中的 `AI-TASK-${aiTaskId}`。
2. 如果确定继续使用 `AI-TASK-`，请同步修正对外契约文档中的所有示例和说明。
3. 失败回调后，AI任务状态到底应为 `paused` 还是 `failed`。
4. `callback_event_id` 重试语义是否需要支持“处理失败后可重放”。
5. 是否需要在业务系统侧扩展运行中阶段回调。如果需要，应新增增量回调语义，不能复用当前终态回调逻辑。

## 5. 建议给工作流系统侧的修订口径

在业务系统代码不改的前提下，建议对工作流系统侧明确以下要求：

```text
1. biz_id 必须传 AI-TASK-${aiTaskId}，例如 AI-TASK-123。
2. workflow_task_id 必须稳定传入，作为本次执行 runId。
3. callback_event_id 建议全局唯一；同一次回调重试使用同一个 callback_event_id。
4. 当前回调接口只发送终态回调，不发送 running 任务级回调。
5. 成功回调必须满足 task_status=SUCCESS 或 status=completed。
6. 成功回调建议直接携带 result.summary 和 result.findings。
7. stages 中的 stage_code 尽量使用业务系统已支持的枚举。
8. 失败回调需要提供顶层 error.message，并尽量提供失败阶段 stages。
```

如果业务系统后续修正了 `biz_id` 前缀或失败状态语义，应同步更新该口径。

## 6. 结论

当前契约文档的主体方向是正确的，尤其是阶段字段、前端四阶段归并、`result` 解析和“当前接口只适合终态回调”的判断都与业务系统实现匹配。

但在对外发送给工作流系统前，建议至少先修正 `biz_id` 前缀问题。该问题是硬性阻断项，不修正会导致工作流回调无法被业务系统识别。
