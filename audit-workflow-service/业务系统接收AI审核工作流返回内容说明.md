# 业务系统接收 AI 审核工作流返回内容说明

## 1. 文档目的

本文档面向业务系统侧，说明 AI 审核工作流系统会向业务系统返回哪些内容、字段类型是什么、业务系统应如何解析。

当前业务系统与工作流系统之间有两类返回内容：

1. 业务系统主动调用工作流系统接口后，工作流系统返回 HTTP 响应。
2. 工作流任务结束后，工作流系统主动回调业务系统 `callback_url`。

其中最重要的是终态 callback。业务系统最终落库审核结果、审核发现项、流程阶段、任务状态时，应优先使用 callback 中的结构化内容。

## 2. 通用响应包装

业务系统主动调用工作流系统接口时，工作流系统统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | number | 工作流系统接口状态码，成功为 `200` |
| `message` | string | 状态说明，成功为 `success` |
| `data` | object / array / null | 实际业务数据 |

如果发生业务异常，通常返回：

```json
{
  "code": 500,
  "message": "ERROR_CODE: error message",
  "data": null
}
```

## 3. 查询可用工作流

接口：

```http
GET http://127.0.0.1:8080/api/audit/workflows
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "workflowCode": "policy_document_audit",
      "workflowName": "制度文件审核工作流",
      "description": "不上传依据文件，调用业务知识库进行审核",
      "enabled": true
    },
    {
      "workflowCode": "uploaded_basis_document_audit",
      "workflowName": "用户上传依据文件审核工作流",
      "description": "使用业务系统本次上传的依据文件进行审核",
      "enabled": true
    }
  ]
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `workflowCode` | string | 工作流编码，创建任务时传入 `workflow_code` |
| `workflowName` | string | 工作流名称 |
| `description` | string | 工作流说明 |
| `enabled` | boolean | 是否启用 |

## 4. 创建工作流任务返回内容

接口：

```http
POST http://127.0.0.1:8080/api/audit/tasks
Content-Type: application/json
```

请求示例：

```json
{
  "workflow_code": "policy_document_audit",
  "biz_id": "AI-TASK-37",
  "callback_url": "http://127.0.0.1:6039/audit/ai/workflow/callback",
  "input": {
    "file_id": "37",
    "file_url": "http://127.0.0.1:6039/profile/upload/report.docx",
    "file_name": "report.docx",
    "file_type": "docx",
    "metadata": {
      "review_task_id": "37",
      "product_name": "本安-矿用本安型手机"
    },
    "knowledge_scope": {
      "effective_only": true
    },
    "caller_context": {
      "user_id": "admin",
      "permission_mode": "explicit_scope"
    }
  }
}
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 22,
    "taskNo": "AUDIT-20260511-000013",
    "taskStatus": "PENDING",
    "workflow_task_id": "22",
    "workflow_task_no": "AUDIT-20260511-000013",
    "status": "accepted",
    "message": "任务已接收"
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `taskId` | number | 工作流任务 ID，用于查询任务状态、结果和回调重试 |
| `taskNo` | string | 工作流任务编号 |
| `taskStatus` | string | 初始任务状态，创建成功后为 `PENDING` |
| `workflow_task_id` | string | 工作流任务 ID 的字符串形式，便于业务系统统一存储 |
| `workflow_task_no` | string | 工作流任务编号 |
| `status` | string | 对外状态，创建成功后为 `accepted` |
| `message` | string | 创建结果说明 |

## 5. 查询任务状态返回内容

接口：

```http
GET http://127.0.0.1:8080/api/audit/tasks/{taskId}
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 22,
    "taskNo": "AUDIT-20260511-000013",
    "workflowCode": "policy_document_audit",
    "bizId": "AI-TASK-37",
    "taskStatus": "SUCCESS",
    "currentNodeCode": "callback",
    "summary": "{\"success\":true,\"summary\":\"本次审核发现17个问题，1个片段需人工复核\",\"risk_level\":\"high\",\"totalIssues\":17}",
    "errorCode": "",
    "errorMsg": null,
    "retryCount": 0,
    "createTime": "2026-05-11T20:37:17",
    "startTime": "2026-05-11T20:37:20",
    "finishTime": "2026-05-11T20:43:19"
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `taskId` | number | 工作流任务 ID |
| `taskNo` | string | 工作流任务编号 |
| `workflowCode` | string | 工作流编码 |
| `bizId` | string | 业务系统传入的业务 ID，原样返回 |
| `taskStatus` | string | 任务状态：`PENDING`、`RUNNING`、`SUCCESS`、`FAILED`、`CANCELED`、`RETRYING` |
| `currentNodeCode` | string | 当前或最后执行节点编码 |
| `summary` | string | 任务摘要 JSON 字符串；业务侧如需结构化结果，建议使用结果接口或 callback |
| `errorCode` | string | 失败错误码，成功时为空 |
| `errorMsg` | string / null | 失败错误信息，成功时为空 |
| `retryCount` | number | 当前重试次数 |
| `createTime` | string | 创建时间 |
| `startTime` | string / null | 开始执行时间 |
| `finishTime` | string / null | 结束时间 |

## 6. 查询审核结果返回内容

接口：

```http
GET http://127.0.0.1:8080/api/audit/tasks/{taskId}/result
```

当任务尚未生成结果时：

```json
{
  "code": 500,
  "message": "RESULT_NOT_FOUND: result not found",
  "data": null
}
```

成功返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "partial_success": true,
    "summary": "本次审核发现17个问题，1个片段需人工复核",
    "totalIssues": 17,
    "findings": [
      {
        "type": "标准不符",
        "title": "委托编号页眉不一致",
        "content": "待审报告中不同页面出现的委托编号不一致。",
        "severity": "high",
        "location": "第1页页眉",
        "suggestion": "请按审核依据统一委托编号。",
        "source_chunk_id": 349,
        "source_chunk_no": 1
      }
    ],
    "warnings": [
      {
        "type": "chunk_audit_failed",
        "source_chunk_id": 362,
        "source_chunk_no": 14,
        "error_code": "MODEL_TIMEOUT",
        "message": "request timed out",
        "retry_count": 0,
        "final_status": "FAILED"
      }
    ],
    "failed_chunks": [
      {
        "source_chunk_id": 362,
        "source_chunk_no": 14,
        "error_code": "MODEL_TIMEOUT",
        "message": "request timed out",
        "retry_count": 0,
        "final_status": "FAILED"
      }
    ],
    "audit_strategy": "chunk_then_merge",
    "chunk_success_count": 14,
    "chunk_failed_count": 1,
    "model_usage_summary": {
      "model": "qwen3.5-plus",
      "model_call_count": 15,
      "parallelism": 3,
      "max_retries": 2,
      "timeout_retries": 0,
      "future_timeout_seconds": 660,
      "chunk_success_count": 14,
      "chunk_failed_count": 1,
      "input_tokens": 31500,
      "output_tokens": 4100,
      "duration_ms": 330000
    },
    "retrieval_used_summary": {
      "reference_selection_strategy": "chunk_then_merge",
      "source_chunk_count": 15,
      "retrieval_count": 15,
      "reference_count": 60,
      "references_used_in_prompt": 60,
      "covered_source_chunk_count": 15,
      "uncovered_source_chunk_ids": [],
      "chunk_max_reference_count": 4,
      "chunk_max_reference_chars": 12000,
      "chunk_model_parallelism": 3,
      "chunk_reference_usage": [
        {
          "source_chunk_id": 349,
          "source_chunk_no": 1,
          "reference_count_before_prompt": 4,
          "references_used_in_prompt": 4
        }
      ],
      "reference_truncated": false
    }
  }
}
```

### 6.1 结果顶层字段说明

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `success` | boolean | AI 审核结果是否成功生成 |
| `partial_success` | boolean | 是否部分成功；当存在少量分片失败但整体结果仍可用时为 `true` |
| `summary` | string | 结果摘要，可直接展示 |
| `totalIssues` | number | 问题总数，等于 `findings.length` |
| `findings` | array | 审核发现项列表 |
| `warnings` | array | 非阻断告警，例如部分分片超时、建议人工复核 |
| `failed_chunks` | array | 失败分片列表 |
| `audit_strategy` | string | 审核策略；第一个工作流通常为 `chunk_then_merge` |
| `chunk_success_count` | number | 成功完成模型审核的分片数 |
| `chunk_failed_count` | number | 失败分片数 |
| `model_usage_summary` | object | 模型调用统计 |
| `retrieval_used_summary` | object | 知识库依据使用统计 |
| `basis_used_summary` | object | 仅用户上传依据文件工作流可能返回，表示上传依据文件使用情况 |

### 6.2 findings 字段说明

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `type` | string | 问题类型：`内容缺失`、`格式错误`、`数据异常`、`逻辑错误`、`标准不符`、`其他` |
| `title` | string | 问题标题 |
| `content` | string | 问题详细描述 |
| `severity` | string | 严重程度：`high`、`medium`、`low` |
| `location` | string | 问题所在章节、条款、表格、字段或分片位置 |
| `suggestion` | string | 修改建议 |
| `source_chunk_id` | number | 发现问题对应的待审文件分片 ID |
| `source_chunk_no` | number | 发现问题对应的分片序号 |

### 6.3 warnings 字段说明

常见 warning：

```json
{
  "type": "chunk_audit_failed",
  "source_chunk_id": 362,
  "source_chunk_no": 14,
  "error_code": "MODEL_TIMEOUT",
  "message": "request timed out",
  "retry_count": 0,
  "final_status": "FAILED"
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `type` | string | 告警类型，分片失败为 `chunk_audit_failed` |
| `source_chunk_id` | number | 失败分片 ID |
| `source_chunk_no` | number | 失败分片序号 |
| `error_code` | string | 失败错误码，例如 `MODEL_TIMEOUT`、`MODEL_RESPONSE_INVALID`、`MODEL_UNAVAILABLE` |
| `message` | string | 失败说明 |
| `retry_count` | number | 已重试次数 |
| `final_status` | string | 分片最终状态，通常为 `FAILED` |

## 7. 终态 callback 返回内容

当工作流任务进入终态后，工作流系统会向业务系统创建任务时传入的 `callback_url` 发起：

```http
POST {callback_url}
Content-Type: application/json
X-Request-Id: CALLBACK-{callback_id}
X-Audit-Task-No: {task_no}
```

callback 不再包裹 `code/message/data`，而是直接发送 payload。

### 7.1 成功 callback 完整示例

```json
{
  "biz_id": "AI-TASK-37",
  "task_id": 22,
  "task_no": "AUDIT-20260511-000013",
  "task_status": "SUCCESS",
  "workflow_task_id": "22",
  "workflow_task_no": "AUDIT-20260511-000013",
  "workflow_code": "policy_document_audit",
  "workflow_name": "制度文件审核工作流",
  "status": "completed",
  "progress_percent": 100,
  "progress_text": "AI分析完成",
  "started_at": "2026-05-11 20:37:20",
  "finished_at": "2026-05-11 20:43:19",
  "duration_ms": 359000,
  "stages": [
    {
      "stage_code": "input_validate",
      "stage_instance_id": "node-101",
      "stage_name": "输入校验",
      "stage_status": "completed",
      "agent_name": "输入校验组件",
      "started_at": "2026-05-11 20:37:20",
      "finished_at": "2026-05-11 20:37:20",
      "duration_ms": 30,
      "summary": "输入校验完成",
      "detail": "输出字段：validated",
      "output": {
        "validated": true
      },
      "error": null,
      "sort_num": 10
    },
    {
      "stage_code": "ai_audit",
      "stage_instance_id": "node-106",
      "stage_name": "AI审核",
      "stage_status": "completed",
      "agent_name": "AI审核智能体",
      "started_at": "2026-05-11 20:38:00",
      "finished_at": "2026-05-11 20:43:00",
      "duration_ms": 300000,
      "summary": "AI审核完成",
      "detail": "输出字段：audit_status、audit_mode、audit_strategy、chunk_result_count、chunk_success_count、chunk_failed_count、finding_count、retrieval_used_summary、model_usage_summary",
      "output": {
        "audit_status": "SUCCESS",
        "audit_mode": "business_report_findings",
        "audit_strategy": "chunk_then_merge",
        "chunk_result_count": 15,
        "chunk_success_count": 14,
        "chunk_failed_count": 1,
        "finding_count": 17
      },
      "error": null,
      "sort_num": 60
    }
  ],
  "result": {
    "summary": "本次审核发现17个问题，1个片段需人工复核",
    "review_opinion": "",
    "findings": [
      {
        "type": "标准不符",
        "title": "委托编号页眉不一致",
        "content": "待审报告中不同页面出现的委托编号不一致。",
        "finding_type": "标准不符",
        "finding_title": "委托编号页眉不一致",
        "finding_content": "待审报告中不同页面出现的委托编号不一致。",
        "severity": "high",
        "location": "第1页页眉",
        "suggestion": "请按审核依据统一委托编号。",
        "source_chunk_id": 349,
        "source_chunk_no": 1,
        "sort_num": 1
      }
    ],
    "raw_output": {
      "success": true,
      "partial_success": true,
      "summary": "本次审核发现17个问题，1个片段需人工复核",
      "totalIssues": 17,
      "findings": [],
      "warnings": [],
      "failed_chunks": [],
      "audit_strategy": "chunk_then_merge",
      "chunk_success_count": 14,
      "chunk_failed_count": 1,
      "model_usage_summary": {},
      "retrieval_used_summary": {}
    }
  },
  "summary": {
    "success": true,
    "summary": "本次审核发现17个问题，1个片段需人工复核",
    "overall_result": "需要整改",
    "risk_level": "high",
    "totalIssues": 17,
    "total_issues": 17,
    "partial_success": true,
    "chunk_success_count": 14,
    "chunk_failed_count": 1
  },
  "summary_text": "本次审核发现17个问题，1个片段需人工复核",
  "result_url": "/api/audit/tasks/22/result",
  "error": null,
  "callback_event_id": "evt-20260511204319-22-1a2b3c4d",
  "callback_time": "2026-05-11 20:43:19"
}
```

### 7.2 callback 顶层字段说明

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `biz_id` | string | 是 | 业务系统创建任务时传入的业务 ID，原样返回 |
| `task_id` | number | 是 | 工作流任务 ID |
| `task_no` | string | 是 | 工作流任务编号 |
| `task_status` | string | 是 | 工作流内部终态：`SUCCESS`、`FAILED`、`CANCELED` |
| `workflow_task_id` | string | 是 | 工作流任务 ID 字符串 |
| `workflow_task_no` | string | 是 | 工作流任务编号 |
| `workflow_code` | string | 是 | 工作流编码 |
| `workflow_name` | string / null | 否 | 工作流名称 |
| `status` | string | 是 | 对外状态：`completed`、`failed`、`cancelled` |
| `progress_percent` | number | 是 | 进度百分比，终态成功为 `100` |
| `progress_text` | string | 是 | 进度文案 |
| `started_at` | string / null | 否 | 开始时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `finished_at` | string / null | 否 | 结束时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `duration_ms` | number / null | 否 | 任务总耗时，单位毫秒 |
| `stages` | array | 是 | 节点执行明细 |
| `result` | object / null | 成功时是 | 成功时为审核结果，失败时为 `null` |
| `summary` | object | 是 | 结构化摘要。注意：该字段是对象，不是字符串 |
| `summary_text` | string | 是 | 纯文本摘要 |
| `result_url` | string / null | 成功时是 | 结果查询地址，失败时为 `null` |
| `error` | object / null | 失败时是 | 失败信息 |
| `callback_event_id` | string | 是 | 回调事件 ID，可用于幂等 |
| `callback_time` | string | 是 | 回调发送时间 |

### 7.3 summary 对象说明

`summary` 是对象结构：

```json
{
  "success": true,
  "summary": "本次审核发现17个问题，1个片段需人工复核",
  "overall_result": "需要整改",
  "risk_level": "high",
  "totalIssues": 17,
  "total_issues": 17,
  "partial_success": true,
  "chunk_success_count": 14,
  "chunk_failed_count": 1
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `success` | boolean | 工作流是否成功生成审核结果 |
| `summary` | string | 可展示的摘要文本 |
| `overall_result` | string | 总体结论：`通过`、`需要整改`、`失败` |
| `risk_level` | string | 最高风险等级：`high`、`medium`、`low`；失败时可为空 |
| `totalIssues` | number | 问题总数，驼峰命名 |
| `total_issues` | number | 问题总数，下划线命名，兼容字段 |
| `partial_success` | boolean | 是否部分成功 |
| `chunk_success_count` | number | 成功分片数 |
| `chunk_failed_count` | number | 失败分片数 |

业务系统如只需要展示摘要文案，可以使用：

```json
{
  "summary_text": "本次审核发现17个问题，1个片段需人工复核"
}
```

### 7.4 stages 数组字段说明

每个 `stages[]` 元素结构：

```json
{
  "stage_code": "ai_audit",
  "stage_instance_id": "node-106",
  "stage_name": "AI审核",
  "stage_status": "completed",
  "agent_name": "AI审核智能体",
  "started_at": "2026-05-11 20:38:00",
  "finished_at": "2026-05-11 20:43:00",
  "duration_ms": 300000,
  "summary": "AI审核完成",
  "detail": "输出字段：audit_status、audit_mode、audit_strategy",
  "output": {},
  "error": null,
  "sort_num": 60
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `stage_code` | string | 节点编码 |
| `stage_instance_id` | string | 节点执行实例 ID，格式为 `node-{log_id}` |
| `stage_name` | string | 节点名称 |
| `stage_status` | string | 节点状态：`completed`、`failed`、`skipped`、`running`、`pending`、`waiting` |
| `agent_name` | string | 执行组件或智能体名称 |
| `started_at` | string / null | 节点开始时间 |
| `finished_at` | string / null | 节点完成时间 |
| `duration_ms` | number | 节点耗时毫秒 |
| `summary` | string | 节点摘要 |
| `detail` | string | 节点详情 |
| `output` | object | 节点输出快照，字段因节点不同而不同 |
| `error` | object / null | 节点失败信息 |
| `sort_num` | number | 节点排序号 |

常见 `stage_code`：

| 节点编码 | 说明 |
| --- | --- |
| `input_validate` | 输入校验 |
| `file_parse` | 待审核文件解析 |
| `basis_file_parse` | 用户上传依据文件解析，仅上传依据工作流存在 |
| `text_split` | 待审核文件切分 |
| `knowledge_retrieve` | 调用业务知识库检索，仅第一个工作流存在 |
| `uploaded_basis_match` | 上传依据文件本地匹配，仅上传依据工作流存在 |
| `ai_audit` | AI 审核 |
| `result_validate` | 结果结构校验 |
| `result_save` | 结果保存 |
| `callback` | callback 节点标记 |

### 7.5 result 对象说明

callback 中的 `result` 是为业务系统落库和展示准备的结果对象：

```json
{
  "summary": "本次审核发现17个问题，1个片段需人工复核",
  "review_opinion": "",
  "findings": [],
  "raw_output": {}
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `summary` | string | 审核摘要 |
| `review_opinion` | string | 复核意见，当前通常为空 |
| `findings` | array | 规范化后的审核发现项，适合业务系统直接落库 |
| `raw_output` | object | 工作流原始审核结果，包含模型使用、知识库引用、告警等诊断字段 |

`result.findings[]` 字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `type` | string | 问题类型 |
| `title` | string | 问题标题 |
| `content` | string | 问题内容 |
| `finding_type` | string | 兼容字段，同 `type` |
| `finding_title` | string | 兼容字段，同 `title` |
| `finding_content` | string | 兼容字段，同 `content` |
| `severity` | string | 严重程度 |
| `location` | string | 问题位置 |
| `suggestion` | string | 修改建议 |
| `source_chunk_id` | number | 来源分片 ID |
| `source_chunk_no` | number | 来源分片序号 |
| `sort_num` | number | 排序号 |
| `evidence` | array | 依据或证据列表，如果模型返回了依据则可能存在 |

## 8. 失败 callback 示例

当工作流失败时，callback 示例：

```json
{
  "biz_id": "AI-TASK-38",
  "task_id": 23,
  "task_no": "AUDIT-20260511-000014",
  "task_status": "FAILED",
  "workflow_task_id": "23",
  "workflow_task_no": "AUDIT-20260511-000014",
  "workflow_code": "policy_document_audit",
  "workflow_name": "制度文件审核工作流",
  "status": "failed",
  "progress_percent": 80,
  "progress_text": "chunk audit failed ratio reached fatal threshold: 4/15",
  "started_at": "2026-05-11 21:10:00",
  "finished_at": "2026-05-11 21:15:00",
  "duration_ms": 300000,
  "stages": [],
  "result": null,
  "summary": {
    "success": false,
    "summary": "chunk audit failed ratio reached fatal threshold: 4/15",
    "overall_result": "失败",
    "risk_level": "",
    "totalIssues": 0,
    "total_issues": 0
  },
  "summary_text": "chunk audit failed ratio reached fatal threshold: 4/15",
  "result_url": null,
  "error": {
    "code": "CHUNK_AUDIT_INCOMPLETE",
    "message": "chunk audit failed ratio reached fatal threshold: 4/15",
    "detail": "chunk audit failed ratio reached fatal threshold: 4/15",
    "stage_code": "ai_audit"
  },
  "callback_event_id": "evt-20260511211500-23-9e8f7a6b",
  "callback_time": "2026-05-11 21:15:00"
}
```

失败字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `task_status` | string | `FAILED` 或 `CANCELED` |
| `status` | string | `failed` 或 `cancelled` |
| `result` | null | 失败时不返回成功结果 |
| `summary.success` | boolean | 失败时为 `false` |
| `summary.overall_result` | string | `失败` |
| `error.code` | string | 错误码 |
| `error.message` | string | 错误说明 |
| `error.detail` | string | 错误详情 |
| `error.stage_code` | string | 失败节点编码 |

## 9. callback 成功判定规则

工作流侧发送 callback 后，会根据业务系统响应判断是否成功。

成功条件：

```text
HTTP 状态码为 2xx，并且如果响应体中存在 code 字段，则 code 必须等于 200。
```

业务系统建议返回：

```json
{
  "code": 200,
  "msg": "操作成功"
}
```

如果业务系统返回 HTTP 200，但 body 为：

```json
{
  "code": 500,
  "msg": "JSON parse error ..."
}
```

工作流侧会判定 callback 失败，并记录为：

```text
callback_status = FAILED
error_msg = business callback code 500: JSON parse error ...
```

失败 callback 会进入自动重试。

## 10. 手动重触发 callback

接口：

```http
POST http://127.0.0.1:8080/api/audit/callbacks/{taskId}/retry
```

返回示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "task_id": 22,
    "retried_callbacks": 1
  }
}
```

说明：

1. 如果该任务存在失败 callback，会重试失败 callback。
2. 如果不存在失败 callback，但任务已经是终态 `SUCCESS`、`FAILED` 或 `CANCELED`，工作流会重新生成一条新的 callback 并发送。
3. 该接口可用于历史任务回调补偿。

## 11. 状态映射建议

工作流内部状态：

| 工作流 `task_status` | 对外 `status` | 业务系统建议状态 |
| --- | --- | --- |
| `PENDING` | `accepted` / `waiting` | `waiting` |
| `RUNNING` | `running` | `executing` |
| `SUCCESS` | `completed` | `completed` |
| `FAILED` | `failed` | `paused` 或 `failed` |
| `CANCELED` | `cancelled` | `paused` 或 `cancelled` |
| `RETRYING` | `running` | `executing` |

分片级失败不一定导致任务失败：

| 场景 | 工作流任务状态 | 说明 |
| --- | --- | --- |
| 所有分片成功 | `SUCCESS` | 正常成功 |
| 少量分片超时或失败，失败比例未达到阈值 | `SUCCESS` | `partial_success=true`，业务侧应提示人工复核失败片段 |
| 分片失败比例达到阈值 | `FAILED` | 错误码通常为 `CHUNK_AUDIT_INCOMPLETE` |
| 所有分片失败 | `FAILED` | 错误码通常为 `MODEL_CHUNK_AUDIT_ALL_FAILED` |

## 12. 业务系统解析建议

建议业务系统按以下优先级处理：

1. 通过 `biz_id` 定位业务 AI 任务。
2. 使用 `callback_event_id` 做幂等处理，避免重复回调重复落库。
3. 使用顶层 `status` 或 `task_status` 更新任务状态。
4. 使用顶层 `summary_text` 写入 `ai_summary`。
5. 使用 `summary` 对象记录结构化摘要和统计信息。
6. 使用 `result.findings` 落库审核发现项。
7. 使用 `stages` 落库流程阶段。
8. 如果 `partial_success=true` 或 `chunk_failed_count>0`，页面应展示“部分片段需人工复核”。
9. 如果 callback 失败后业务系统已经修复，可调用 `/api/audit/callbacks/{taskId}/retry` 触发补偿。
