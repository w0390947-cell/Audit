# “本安-矿用本安型手机”测试任务 AI审核阶段超时问题说明

日期：2026-05-12

本文档面向工作流系统侧，用于说明业务系统中“本安-矿用本安型手机”测试任务的最新执行结果和需要工作流侧排查/处理的问题。

## 1. 问题结论

本次任务不是文件解析失败，也不是审核文件库向量检索失败。

业务系统数据显示：

- 报告文件解析成功。
- 文本切分成功。
- 审核文件库知识库检索成功，并命中较多依据。
- 工作流已经回调到 `ai_audit-running`。
- 之后工作流侧没有继续回调 `ai_audit-success`、`ai_audit-failed`，也没有发送终态 `/audit/ai/workflow/callback`。
- 业务系统同步等待工作流结果达到 `poll-timeout-ms = 900000`，即 15 分钟后，将 AI 任务标记为失败。

因此，当前需要工作流系统侧重点排查：

1. `ai_audit` 节点为什么长时间停留在运行中。
2. 超时或异常后为什么没有发送阶段失败回调和终态失败回调。
3. 知识库命中 120 条依据后，AI审核节点是否出现提示词过大、模型调用超时、模型返回过慢或内部异常未捕获。

## 2. 任务基本信息

业务系统 AI 任务：

| 字段 | 值 |
| --- | --- |
| `ai_task_id` | `14` |
| `task_no` | `SF-1778564465623` |
| 产品名称 | `本安-矿用本安型手机` |
| 报告文件 | `etst_20260512134103A004.docx` |
| 提交时间 | `2026-05-12 13:41:06` |
| 当前任务状态 | `failed` |
| 当前复核状态 | `reviewing` |
| 当前进度 | `50` |
| 当前进度文案 | `AI分析失败：工作流任务执行超时` |
| 业务系统更新时间 | `2026-05-12 13:57:01` |

工作流任务：

| 字段 | 值 |
| --- | --- |
| `workflow_task_id` | `9` |
| `workflow_task_no` | `AUDIT-20260512-000003` |
| `workflow_code` | `policy_document_audit` |

业务系统工作流配置：

| 配置项 | 值 |
| --- | --- |
| `audit-workflow.base-url` | `http://127.0.0.1:8080` |
| `audit-workflow.callback-url` | `http://127.0.0.1:6039/audit/ai/workflow/callback` |
| `audit-workflow.poll-interval-ms` | `3000` |
| `audit-workflow.poll-timeout-ms` | `900000` |

## 3. 业务系统收到的阶段状态

截至 `2026-05-12 13:58:55`，业务系统 `audit_ai_flow_stage` 中该任务的阶段如下：

| 阶段 | 状态 | 时间 | 说明 |
| --- | --- | --- | --- |
| `input_validate` 输入校验 | `completed` | `13:42:04 - 13:42:04` | 输入校验完成 |
| `file_parse` 文件解析 | `completed` | `13:42:05 - 13:42:05` | 报告文件解析完成 |
| `text_split` 文本切分 | `completed` | `13:42:05 - 13:42:05` | 生成 15 个文本分片 |
| `knowledge_retrieve` 知识库检索 | `completed` | `13:42:05 - 13:42:17` | 检索成功，`reference_count = 120` |
| `ai_audit` AI审核 | `running` | `13:42:17 - NULL` | 之后未收到成功或失败阶段回调 |

关键阶段输出：

```json
{
  "stage_code": "knowledge_retrieve",
  "stage_status": "completed",
  "output": {
    "retrieval_status": "SUCCESS",
    "source_chunk_count": 15,
    "retrieval_count": 15,
    "reference_count": 120
  }
}
```

```json
{
  "stage_code": "ai_audit",
  "stage_status": "running",
  "stage_summary": "正在执行AI审核分析",
  "output": {}
}
```

## 4. 业务系统收到的回调事件

业务系统 `audit_workflow_callback_event` 中该任务收到的最后一条回调是：

| 时间 | `callback_event_id` | 状态 |
| --- | --- | --- |
| `2026-05-12 13:42:17` | `wf-audit-9-0-stage-ai_audit-running` | `processed` |

之后没有收到以下任何事件：

- `wf-audit-9-0-stage-ai_audit-success`
- `wf-audit-9-0-stage-ai_audit-failed`
- `wf-audit-9-0-success`
- `wf-audit-9-0-failed`
- 其他终态 `/audit/ai/workflow/callback`

这意味着业务系统不是根据工作流终态回调失败的，而是自身轮询工作流任务状态超时后失败的。

## 5. 向量检索情况

本次任务知识库检索是成功的。

业务系统 `audit_vector_search_log` 中，工作流任务 `AUDIT-20260512-000003` 有两批检索记录：

| 时间 | `request_id` | `query_count` | `result_count` | `top_resource_ids` | 耗时 |
| --- | --- | ---: | ---: | --- | ---: |
| `2026-05-12 13:42:11` | `AUDIT-20260512-000003-KB-BATCH-222244509333000` | `10` | `80` | `24,23,22` | `6241ms` |
| `2026-05-12 13:42:17` | `AUDIT-20260512-000003-KB-BATCH-222250950443500` | `5` | `40` | `24,22,23` | `5654ms` |

这说明审核文件库向量检索已恢复，且本次命中了三个新入库标准文件：

- `resource_id = 22`：`GBT3836.4-2021`
- `resource_id = 23`：`GBT3836.1-2021 爆炸性环境 第1部分：设备 通用要求`
- `resource_id = 24`：`GBT3836.2-2021`

## 6. 需要工作流系统侧排查的问题

请工作流系统侧按以下问题逐项排查并反馈：

1. `workflow_task_id = 9` / `AUDIT-20260512-000003` 在工作流系统内部的最终状态是什么？
2. `ai_audit` 节点从 `2026-05-12 13:42:17` 开始后，是否仍在运行、已超时、已失败，还是进程/线程中断？
3. 如果 `ai_audit` 节点失败或超时，为什么没有调用业务系统 `/audit/ai/workflow/stageCallback` 上报 `ai_audit` 的失败状态？
4. 如果整个工作流失败或超时，为什么没有调用业务系统 `/audit/ai/workflow/callback` 上报终态失败？
5. 本次 `knowledge_retrieve.reference_count = 120`，请确认 AI审核节点是否把 120 条依据全部拼入模型提示词。
6. 如果全部拼入，请确认是否超过模型上下文、单次请求长度、网关限制、模型服务超时限制或工作流节点超时限制。
7. 如果模型调用仍在执行，请提供模型调用开始时间、结束时间、耗时、模型名称、请求 token/字符规模、失败异常。
8. 如果模型调用失败，请在阶段失败回调中提供 `error.code` 和 `error.message`，不要只停留在 `running`。
9. 请确认工作流系统是否有节点级超时保护：节点超时时应立即发送 `stage_status=FAILED` 的阶段回调，并最终发送 `task_status=FAILED` 的终态回调。
10. 请确认工作流任务查询接口 `/api/audit/tasks/9` 在业务系统轮询期间返回的 `taskStatus` 是什么，以及为什么 15 分钟内未返回 `SUCCESS/FAILED/CANCELED`。

## 7. 建议工作流侧改造

### 7.1 AI审核节点必须有失败出口

`ai_audit` 节点不能只发送 running。无论模型调用失败、提示词超限、节点超时、内部异常，均应发送阶段失败回调：

```json
{
  "callback_event_id": "wf-audit-9-0-stage-ai_audit-failed",
  "biz_id": "AI-TASK-14",
  "workflow_task_id": "9",
  "workflow_task_no": "AUDIT-20260512-000003",
  "task_status": "RUNNING",
  "status": "running",
  "progress_percent": 70,
  "progress_text": "AI审核分析失败",
  "stages": [
    {
      "stage_code": "ai_audit",
      "stage_instance_id": "ai_audit_50",
      "stage_name": "AI审核",
      "stage_status": "FAILED",
      "summary": "AI审核分析失败",
      "detail": "请填写模型调用失败、提示词超限或节点超时的具体原因",
      "error": {
        "code": "MODEL_TIMEOUT_OR_PROMPT_TOO_LARGE",
        "message": "请填写真实错误信息"
      },
      "sort_num": 50
    }
  ]
}
```

随后发送终态失败回调：

```json
{
  "callback_event_id": "wf-audit-9-0-failed",
  "biz_id": "AI-TASK-14",
  "workflow_task_id": "9",
  "workflow_task_no": "AUDIT-20260512-000003",
  "task_status": "FAILED",
  "status": "failed",
  "progress_percent": 70,
  "progress_text": "AI审核分析失败",
  "stages": [
    {
      "stage_code": "ai_audit",
      "stage_instance_id": "ai_audit_50",
      "stage_name": "AI审核",
      "stage_status": "FAILED",
      "summary": "AI审核分析失败",
      "detail": "请填写真实失败原因",
      "error": {
        "code": "MODEL_TIMEOUT_OR_PROMPT_TOO_LARGE",
        "message": "请填写真实错误信息"
      },
      "sort_num": 50
    }
  ],
  "error": {
    "code": "MODEL_TIMEOUT_OR_PROMPT_TOO_LARGE",
    "message": "请填写真实错误信息",
    "stage_code": "ai_audit"
  }
}
```

### 7.2 控制进入 AI审核节点的依据规模

本次检索总结果数为 120。如果工作流侧将 120 条依据全部拼入模型提示词，极易引发：

- 提示词过长；
- 模型上下文超限；
- 模型调用耗时过长；
- 网关或节点超时；
- 模型响应被截断或无法解析。

建议工作流侧在 AI审核前增加依据压缩/筛选策略，例如：

- 每个报告分片只取 Top N 依据；
- 全局去重后限制最大依据片段数；
- 按分数阈值过滤低相关依据；
- 对依据先做摘要压缩，再送入 AI审核；
- 在阶段 output 中返回实际进入 prompt 的依据数量，例如 `basis_chunks_used_in_prompt`。

### 7.3 任务状态查询和回调语义保持一致

业务系统当前会轮询：

```http
GET /api/audit/tasks/9
```

如果工作流侧内部已经失败，该接口应及时返回 `taskStatus = FAILED`。同时，也应发送终态失败回调到业务系统。避免业务系统只能等 15 分钟后自行判定超时。

## 8. 业务系统侧观察到的风险

当前业务系统已经将 AI 任务标记为 `failed`，但阶段表中 `ai_audit` 仍是 `running`，因为没有收到工作流侧的失败阶段回调。

这会造成“任务主状态已失败，但流转状态里 AI审核阶段仍显示执行中”的不一致体验。

业务系统侧可以后续做兜底修复，但根因仍需要工作流侧保证：

- 节点失败必须回调阶段失败；
- 工作流失败必须回调终态失败；
- 任务查询接口应及时返回终态。

## 9. 工作流系统侧处理记录

处理日期：2026-05-12

已在工作流系统侧完成以下处理：

1. 为 `ai_audit` 分片审核节点增加整体超时保护配置 `audit.audit.ai-audit-timeout-seconds`，当前默认值为 `840` 秒。
2. 当 `ai_audit` 分片审核整体耗时超过该阈值时，工作流会取消未完成的分片模型调用，并抛出 `AI_AUDIT_TIMEOUT`。
3. 该异常会进入现有工作流引擎失败出口，执行以下动作：
   - 将当前节点日志标记为 `FAILED`；
   - 调用 `/audit/ai/workflow/stageCallback` 上报 `ai_audit` 阶段失败；
   - 将工作流任务状态标记为 `FAILED`；
   - 调用 `/audit/ai/workflow/callback` 上报终态失败。
4. 当前分片审核不会把 120 条依据一次性全部拼入单次模型提示词。分片模式下每个报告分片按 `audit.audit.chunk-max-reference-count` 和 `audit.audit.chunk-max-reference-chars` 控制进入 prompt 的依据数量，当前配置为每个分片最多 `4` 条、最多 `12000` 字符。
5. 终态失败回调中的 `error.code` 将为 `AI_AUDIT_TIMEOUT`，`error.message` 会说明 AI审核节点超过配置秒数并已取消未完成调用。

本次处理的目标是让工作流系统在业务系统 `poll-timeout-ms = 900000` 之前自行失败并回调，避免业务系统只能等待 15 分钟后自行判定超时，也避免详情页出现“任务主状态失败但 `ai_audit` 阶段仍 running”的状态不一致。

后续复测建议：

1. 重新发起“本安-矿用本安型手机”同类任务。
2. 如果模型调用仍超过 `840` 秒，应确认业务系统收到 `wf-audit-${workflow_task_id}-0-stage-ai_audit-failed` 和 `wf-audit-${workflow_task_id}-0-failed` 两类回调。
3. 查询 `GET /api/audit/tasks/{workflow_task_id}`，应在超时后返回 `taskStatus = FAILED`，不应继续停留在 `RUNNING`。
