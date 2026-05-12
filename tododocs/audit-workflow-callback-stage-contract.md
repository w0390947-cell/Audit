# AI审核工作流回调阶段数据对接说明

日期：2026-05-12

本文档用于交给工作流系统侧，确认并规范 AI 审核工作流回调中 `stages` 及 `result` 的生成内容。目标是让业务系统“AI任务详情 -> 流转状态”组件可以稳定展示四个业务阶段：

- 任务接收
- 内容解析
- 审核分析
- 结果处理

## 1. 当前业务系统接收方式

业务系统目前提供两个工作流回调接口：

```http
POST /audit/ai/workflow/callback
Authorization: Bearer ${callbackToken}
Content-Type: application/json
```

用途：终态回调。工作流最终成功或最终失败时调用。

```http
POST /audit/ai/workflow/stageCallback
Authorization: Bearer ${callbackToken}
Content-Type: application/json
```

用途：运行中阶段回调。工作流执行过程中用于刷新阶段状态和任务进度。

说明：

- 如果业务系统未配置 `callbackToken`，则不校验 Authorization；如果配置了，则必须完全匹配 `Bearer ${callbackToken}`。
- `biz_id` 必填，当前格式必须是 `AI-TASK-${aiTaskId}`，否则业务系统无法找到对应 AI 任务。
- `workflow_task_id` 必填，且同一次工作流执行必须稳定不变；业务系统会将其作为本次运行 `run_id`。
- 终态回调 `/callback` 会整体替换同一个 `aiTaskId + runId` 下的阶段。
- 运行中阶段回调 `/stageCallback` 会增量合并同一个 `aiTaskId + runId` 下的阶段。
- `callback_event_id` 建议必传且全局唯一，用于幂等去重；同一事件重试必须使用同一个值。
- 如果同一个 `callback_event_id` 已处理成功，业务系统会忽略重复回调；如果上次处理状态为 `failed`，业务系统允许使用同一个 `callback_event_id` 重试。

## 2. 重要限制：`/callback` 只适合终态回调

业务系统终态回调接口 `/audit/ai/workflow/callback` 判断成功的规则：

- `task_status = SUCCESS`，或
- `status = completed`

如果不满足以上规则，业务系统会把该回调视为失败回调，并把 AI 任务标记为失败。

因此，请工作流系统侧确认：

1. 是否只在工作流最终成功或最终失败时调用该回调接口？
2. 如果需要“运行中阶段实时刷新”，请不要直接向 `/callback` 发送 `running` 任务级回调；请使用下面的阶段回调接口。

### 2.1 运行中阶段回调接口

运行中阶段刷新请调用：

```http
POST /audit/ai/workflow/stageCallback
Authorization: Bearer ${callbackToken}
Content-Type: application/json
```

阶段回调语义：

- 只更新 `audit_ai_flow_stage` 和 AI 任务进度。
- 不保存最终 `result`。
- 不会把 AI 任务标记为 `completed` 或 `failed`。
- 同一个 `workflow_task_id` 会作为同一次运行的 `run_id`。
- 支持增量上报阶段：同一 `run_id` 下，业务系统会按 `stage_instance_id` 优先、否则按 `stage_code` 合并阶段；新阶段覆盖同 key 旧阶段，未回传的旧阶段保留。
- 如果 AI 任务已经是 `completed` 或 `failed`，阶段回调会被忽略，避免运行中旧消息覆盖终态。

阶段回调请求体可复用本文档的顶层字段和 `stages` 字段。运行中回调建议：

- `task_status = RUNNING` 或 `status = running`。
- `progress_percent` / `progress_text` 尽量提供，用于同步 AI 任务进度。
- `callback_event_id` 每次阶段事件保持唯一；同一事件重试使用同一个值。
- `result` 不需要提供。

运行中阶段回调示例：

```json
{
  "callback_event_id": "wf-audit-123-wf-run-987654-stage-003",
  "callback_time": "2026-05-12T09:28:45+08:00",
  "biz_id": "AI-TASK-123",
  "workflow_task_id": "wf-run-987654",
  "workflow_task_no": "WF202605120001",
  "task_status": "RUNNING",
  "status": "running",
  "progress_percent": 45,
  "progress_text": "正在执行审核分析",
  "stages": [
    {
      "stage_code": "ai_audit",
      "stage_instance_id": "ai_audit_001",
      "stage_name": "AI审核分析",
      "stage_status": "RUNNING",
      "summary": "正在调用模型进行审核分析",
      "detail": "已完成依据检索，正在生成结构化问题清单",
      "output": {
        "reference_count": 12,
        "basis_chunks_used_in_prompt": 8
      },
      "sort_num": 60
    }
  ]
}
```

## 3. 顶层回调字段要求

建议回调 JSON 顶层字段如下：

```json
{
  "callback_event_id": "wf-audit-20260512-0001-final",
  "callback_time": "2026-05-12T09:30:00+08:00",
  "biz_id": "AI-TASK-123",
  "workflow_code": "audit_review_workflow",
  "workflow_name": "AI审核工作流",
  "workflow_task_id": "wf-run-987654",
  "workflow_task_no": "WF202605120001",
  "task_status": "SUCCESS",
  "status": "completed",
  "progress_percent": 100,
  "progress_text": "AI审核工作流执行完成",
  "started_at": "2026-05-12T09:28:00+08:00",
  "finished_at": "2026-05-12T09:30:00+08:00",
  "duration_ms": 120000,
  "stages": [],
  "result": {},
  "error": null
}
```

字段说明：

| 字段 | 是否必填 | 说明 |
| --- | --- | --- |
| `callback_event_id` | 建议必填 | 幂等键。重试必须使用同一个值，不同执行事件必须不同；已成功处理的重复事件会被忽略，处理失败的同 ID 事件允许重试。 |
| `biz_id` | 必填 | 必须为 `AI-TASK-${aiTaskId}`。 |
| `workflow_task_id` | 必填 | 必须稳定传入，作为本次运行标识，影响阶段替换、阶段增量合并和详情页读取最新 run。 |
| `workflow_task_no` | 建议必填 | 展示和排查使用。 |
| `task_status` / `status` | 必填其一 | 成功请传 `task_status=SUCCESS` 或 `status=completed`。失败请传 `FAILED/failed` 并带 `error`。 |
| `started_at` / `finished_at` | 建议必填 | 支持 `yyyy-MM-dd HH:mm:ss` 或 ISO-8601 带时区格式。 |
| `duration_ms` | 建议必填 | 毫秒。 |
| `stages` | 强烈建议必填 | 用于“流转状态”组件展示。 |
| `result` | 成功必填 | 用于保存 AI 摘要和发现项。 |
| `error` | 失败必填 | 用于失败原因展示。 |

## 4. stages 字段要求

每个阶段对象支持以下字段：

```json
{
  "stage_code": "file_parse",
  "stage_instance_id": "file_parse_001",
  "stage_name": "报告文件解析",
  "stage_status": "SUCCESS",
  "agent_name": "文档解析智能体",
  "started_at": "2026-05-12T09:28:10+08:00",
  "finished_at": "2026-05-12T09:28:35+08:00",
  "duration_ms": 25000,
  "summary": "完成报告 PDF 文本解析",
  "detail": "解析文本 12000 字；识别段落 86 块",
  "output": {
    "char_count": 12000,
    "block_count": 86
  },
  "error": null,
  "sort_num": 20
}
```

字段说明：

| 字段 | 是否必填 | 说明 |
| --- | --- | --- |
| `stage_code` | 必填 | 前端按该字段归并到四个业务阶段。 |
| `stage_instance_id` | 建议必填 | 同一 `stage_code` 可多次出现时用于区分。 |
| `stage_name` | 必填 | 处理日志中展示。 |
| `stage_status` | 必填 | 支持 `SUCCESS/FAILED/ERROR/RUNNING/CANCELED/CANCELLED`，会映射为 completed/failed/running/skipped。 |
| `agent_name` | 建议必填 | 处理节点名称。 |
| `started_at` / `finished_at` | 建议必填 | 阶段耗时和时间展示。 |
| `duration_ms` | 建议必填 | 阶段耗时，毫秒。 |
| `summary` | 必填 | 阶段摘要，卡片和日志使用。 |
| `detail` | 建议必填 | 详细日志，支持用中文分号或换行分隔。 |
| `output` | 建议必填 | 结构化指标，前端会读取其中部分 key。 |
| `error` | 失败必填 | 错误对象或字符串。 |
| `sort_num` | 必填 | 阶段排序，建议 10、20、30 递增。 |

## 5. stage_code 规范

前端当前按以下规则把工作流子阶段归并到四个业务阶段。

### 5.1 任务接收

归并代码：

- `queued`
- `input_validate`

建议至少返回：

| stage_code | stage_name | 建议 output |
| --- | --- | --- |
| `queued` | 任务接收 | `{ "queue_position": 1 }` |
| `input_validate` | 输入校验 | `{ "report_file_count": 1, "basis_file_count": 2 }` |

### 5.2 内容解析

归并代码：

- `file_parse`
- `target_file_parse`
- `basis_file_parse`
- `text_split`
- `target_text_split`

前端当前读取的 `output` key：

- `char_count`
- `block_count`
- `chunk_count`

建议至少返回：

| stage_code | stage_name | 建议 output |
| --- | --- | --- |
| `target_file_parse` 或 `file_parse` | 报告文件解析 | `{ "char_count": 12000, "block_count": 86 }` |
| `basis_file_parse` | 依据文件解析 | `{ "file_count": 2, "char_count": 56000, "block_count": 320 }` |
| `target_text_split` 或 `text_split` | 文本切分 | `{ "chunk_count": 128 }` |

### 5.3 审核分析

归并代码：

- `knowledge_retrieve`
- `uploaded_basis_match`
- `basis_pack_or_match`
- `ai_audit`
- `result_validate`

前端当前读取的 `output` key：

- 依据命中：`reference_count`、`references_used_in_prompt`、`basis_chunks_used_in_prompt`
- 模型调用：`model_call_count`，可从 `model_usage_summary` 嵌套对象中读取
- 发现问题：`finding_count`、`totalIssues`、`total_issues`

建议至少返回：

| stage_code | stage_name | 建议 output |
| --- | --- | --- |
| `knowledge_retrieve` | 知识库检索 | `{ "reference_count": 12 }` |
| `uploaded_basis_match` 或 `basis_pack_or_match` | 上传依据匹配 | `{ "basis_chunks_used_in_prompt": 8 }` |
| `ai_audit` | AI审核分析 | `{ "model_call_count": 1, "finding_count": 3 }` |
| `result_validate` | 结果校验 | `{ "total_issues": 3, "valid_issue_count": 3 }` |

### 5.4 结果处理

归并代码：

- `result_save`
- `callback`

前端当前读取的 `output` key：

- `issue_count`

建议至少返回：

| stage_code | stage_name | 建议 output |
| --- | --- | --- |
| `result_save` | 结果保存 | `{ "issue_count": 3 }` |
| `callback` | 回调通知 | `{ "callback_status": "success" }` |

## 6. result 字段要求

成功回调必须提供 `result`，否则业务系统会尝试通过 `result_url` / `task_id` 去工作流系统查询结果。

建议直接在回调中携带：

```json
{
  "summary": "本次AI审核发现3类问题，建议补充依据说明并修正报告编号。",
  "findings": [
    {
      "type": "数据错误",
      "title": "报告编号与依据文件不一致",
      "content": "报告第1页编号与依据文件登记编号不一致。",
      "severity": "medium",
      "location": {
        "page": 1,
        "section": "报告首页"
      },
      "suggestion": "请核对并统一报告编号。"
    }
  ]
}
```

兼容字段：

- 问题数组支持 `findings` 或 `issues`。
- 问题类型支持 `type` 或 `finding_type`。
- 问题标题支持 `title` 或 `finding_title`。
- 问题内容支持 `content`、`finding_content` 或 `problem`。
- 严重程度支持 `severity` 或 `risk_level`。

## 7. 失败回调要求

失败回调建议：

```json
{
  "callback_event_id": "wf-audit-20260512-0001-failed",
  "biz_id": "AI-TASK-123",
  "workflow_task_id": "wf-run-987654",
  "task_status": "FAILED",
  "status": "failed",
  "progress_percent": 60,
  "progress_text": "AI审核分析失败",
  "started_at": "2026-05-12T09:28:00+08:00",
  "finished_at": "2026-05-12T09:29:30+08:00",
  "duration_ms": 90000,
  "stages": [
    {
      "stage_code": "ai_audit",
      "stage_name": "AI审核分析",
      "stage_status": "FAILED",
      "summary": "模型调用失败",
      "detail": "调用模型服务超时",
      "error": {
        "code": "MODEL_TIMEOUT",
        "message": "模型服务 60 秒未响应"
      },
      "sort_num": 50
    }
  ],
  "error": {
    "code": "MODEL_TIMEOUT",
    "message": "模型服务 60 秒未响应",
    "stage_code": "ai_audit"
  }
}
```

注意：

- `error.message` 或 `progress_text` 会作为业务系统 AI 任务失败原因。
- 失败终态回调会把 AI 任务状态标记为 `failed`。
- 如果失败时 `stages` 为空，业务系统会尝试构造一个兜底失败阶段，但前端展示会不如明确阶段准确。

## 8. 完整成功回调示例

```json
{
  "callback_event_id": "wf-audit-123-wf-run-987654-success",
  "callback_time": "2026-05-12T09:30:00+08:00",
  "biz_id": "AI-TASK-123",
  "workflow_code": "audit_review_workflow",
  "workflow_name": "AI审核工作流",
  "workflow_task_id": "wf-run-987654",
  "workflow_task_no": "WF202605120001",
  "task_status": "SUCCESS",
  "status": "completed",
  "progress_percent": 100,
  "progress_text": "AI审核工作流执行完成",
  "started_at": "2026-05-12T09:28:00+08:00",
  "finished_at": "2026-05-12T09:30:00+08:00",
  "duration_ms": 120000,
  "stages": [
    {
      "stage_code": "queued",
      "stage_name": "任务接收",
      "stage_status": "SUCCESS",
      "summary": "任务已接收",
      "detail": "任务进入AI审核队列",
      "sort_num": 10
    },
    {
      "stage_code": "input_validate",
      "stage_name": "输入校验",
      "stage_status": "SUCCESS",
      "summary": "输入文件校验通过",
      "detail": "报告文件1个；依据文件2个",
      "output": {
        "report_file_count": 1,
        "basis_file_count": 2
      },
      "sort_num": 20
    },
    {
      "stage_code": "target_file_parse",
      "stage_name": "报告文件解析",
      "stage_status": "SUCCESS",
      "summary": "报告内容解析完成",
      "detail": "解析文本12000字；识别段落86块",
      "output": {
        "char_count": 12000,
        "block_count": 86
      },
      "sort_num": 30
    },
    {
      "stage_code": "target_text_split",
      "stage_name": "文本切分",
      "stage_status": "SUCCESS",
      "summary": "文本分片完成",
      "detail": "生成128个文本分片",
      "output": {
        "chunk_count": 128
      },
      "sort_num": 40
    },
    {
      "stage_code": "knowledge_retrieve",
      "stage_name": "依据检索",
      "stage_status": "SUCCESS",
      "summary": "命中12条依据片段",
      "detail": "从审核文件库和上传依据中检索相关依据",
      "output": {
        "reference_count": 12,
        "basis_chunks_used_in_prompt": 8
      },
      "sort_num": 50
    },
    {
      "stage_code": "ai_audit",
      "stage_name": "AI审核分析",
      "stage_status": "SUCCESS",
      "summary": "AI审核完成，发现3个问题",
      "detail": "完成报告与依据的结构化比对",
      "output": {
        "model_call_count": 1,
        "finding_count": 3
      },
      "sort_num": 60
    },
    {
      "stage_code": "result_save",
      "stage_name": "结果保存",
      "stage_status": "SUCCESS",
      "summary": "审核结果保存完成",
      "detail": "保存3条问题记录",
      "output": {
        "issue_count": 3
      },
      "sort_num": 70
    },
    {
      "stage_code": "callback",
      "stage_name": "回调通知",
      "stage_status": "SUCCESS",
      "summary": "回调业务系统成功",
      "detail": "业务系统已接收AI审核结果",
      "output": {
        "callback_status": "success"
      },
      "sort_num": 80
    }
  ],
  "result": {
    "summary": "本次AI审核发现3个问题，建议按问题清单修订报告。",
    "findings": [
      {
        "type": "数据错误",
        "title": "报告编号与依据文件不一致",
        "content": "报告第1页编号与依据文件登记编号不一致。",
        "severity": "medium",
        "location": {
          "page": 1,
          "section": "报告首页"
        },
        "suggestion": "请核对并统一报告编号。"
      }
    ]
  }
}
```

## 9. 需要工作流系统侧确认的问题

请工作流系统侧逐项确认：

1. 是否能保证终态回调一定包含 `biz_id = AI-TASK-${aiTaskId}`？
2. 是否能保证每次执行一定传入稳定的 `workflow_task_id`，并在重试同一次回调时保持不变？
3. 是否能提供稳定且唯一的 `callback_event_id`，并确保同一事件重试沿用原值？
4. 是否确认终态回调发送到 `/callback`，运行中阶段回调发送到 `/stageCallback`？
5. 是否能按本文档的 `stage_code` 枚举输出阶段？如已有不同阶段编码，请提供映射表。
6. 是否能为每个阶段提供 `sort_num`，确保展示顺序稳定？
7. 是否能为内容解析、审核分析、结果处理阶段提供本文档列出的 `output` 指标 key？
8. 是否能在成功回调中直接携带 `result.summary` 和 `result.findings`，避免业务系统二次查询 `result_url`？
9. 失败时是否能提供顶层 `error.message`，并在失败阶段中也提供 `error`？
10. 时间字段是否统一使用 ISO-8601 带时区格式，例如 `2026-05-12T09:30:00+08:00`？

## 10. 业务系统侧后续可选优化

以下不是工作流系统侧必须处理，属于后续体验优化：

- 前端进一步支持阶段级局部刷新或 WebSocket 推送，减少详情页轮询。
- 将当前前端四阶段分组配置后端化，减少前后端阶段编码耦合。
