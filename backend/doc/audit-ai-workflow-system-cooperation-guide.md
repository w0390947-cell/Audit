# AI任务详情流转状态-工作流系统侧配合说明

## 1. 文档目的

本文面向工作流系统侧，说明为了支持业务系统 `AI任务详情` 页面展示真实 `流转状态`，工作流系统需要提供哪些能力、字段、回调数据和状态约定。

业务系统目标是在 `AI任务详情` 页面中展示：

- AI 任务实际执行了哪些工作流节点。
- 每个节点由哪个智能体或组件处理。
- 每个节点是等待中、执行中、已完成、已失败还是已跳过。
- 每个节点的开始时间、结束时间和耗时。
- 每个节点的摘要、关键输出和错误信息。
- 最终 AI 检测结果、AI 摘要和整改建议。

如果工作流系统无法提供结构化步骤数据，业务系统只能根据 `audit_ai_task.task_status` 等任务级字段进行前端推导，无法准确展示真实执行链路。

## 2. 协作结论

第二阶段需要工作流系统侧配合。

业务系统可以完成：

- 创建 AI 任务。
- 调用工作流系统创建工作流任务。
- 接收工作流回调。
- 将回调结果落库。
- 在 `AI任务详情` 页面展示 `flowStageList`。

工作流系统需要完成：

- 接收业务系统传入的 `biz_id`，并在后续回调中原样返回。
- 在任务完成或失败时回调业务系统。
- 在回调中提供结构化的节点执行明细。
- 在回调中提供最终检测结果。
- 对失败节点提供明确错误信息。
- 保证同一任务多次回调时具备可幂等处理的标识。

## 3. 业务系统现状

业务系统中的 AI 任务实体是：

```text
audit_ai_task
```

核心字段包括：

- `ai_task_id`
- `review_task_id`
- `review_version_id`
- `task_no`
- `task_status`
- `progress_percent`
- `progress_text`
- `report_file_url`
- `ai_summary`
- `review_opinion`

当前详情接口：

```text
GET /audit/ai/{aiTaskId}
```

第二阶段业务系统计划新增 AI 流程阶段数据：

```text
audit_ai_flow_stage
```

并在详情接口中返回：

```json
{
  "flowStageList": []
}
```

## 4. 调用链路

### 4.1 创建工作流任务

业务系统发起 AI 分析时，会调用工作流系统创建任务。

业务系统会传入：

```json
{
  "biz_id": "audit-ai-1001",
  "callback_url": "https://业务系统域名/audit/workflow/callback",
  "input": {
    "ai_task_id": 1001,
    "review_task_id": 2001,
    "review_version_id": 3001,
    "task_no": "SF-202605110001",
    "product_name": "产品名称",
    "delivery_unit": "送检单位",
    "report_file_url": "https://业务系统域名/profile/audit/review/report.pdf",
    "basis_file_urls": [
      "https://业务系统域名/profile/audit/review/basis-a.pdf"
    ]
  }
}
```

工作流系统需要返回：

```json
{
  "workflow_task_id": "wf-task-202605110001",
  "workflow_task_no": "WF-202605110001",
  "status": "accepted",
  "message": "任务已接收"
}
```

### 4.2 回调业务系统

工作流系统完成、失败或产生关键状态变化时，回调业务系统。

回调地址由业务系统创建任务时传入：

```text
POST {callback_url}
```

业务系统需要通过 `biz_id` 识别 AI 任务，因此工作流系统必须在每次回调中原样返回 `biz_id`。

## 5. 回调时机要求

最低要求：工作流系统必须在任务终态回调一次。

建议要求：

- 任务开始时回调一次。
- 每个关键节点完成时可回调一次。
- 任务整体完成时回调一次。
- 任务整体失败时回调一次。

如果工作流系统只支持终态回调，也可以接受，但终态回调里必须包含完整 `stages` 数组。

## 6. 回调数据结构

### 6.1 顶层字段

工作流系统回调建议使用以下结构：

```json
{
  "biz_id": "audit-ai-1001",
  "workflow_task_id": "wf-task-202605110001",
  "workflow_task_no": "WF-202605110001",
  "workflow_code": "audit_report_review",
  "workflow_name": "报告AI审核工作流",
  "status": "completed",
  "progress_percent": 100,
  "progress_text": "AI分析完成",
  "started_at": "2026-05-11 10:00:00",
  "finished_at": "2026-05-11 10:03:10",
  "duration_ms": 190000,
  "stages": [],
  "result": {},
  "error": null,
  "callback_event_id": "evt-20260511000310-0001",
  "callback_time": "2026-05-11 10:03:10"
}
```

### 6.2 顶层字段说明

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `biz_id` | 是 | 业务系统传入的业务ID，必须原样返回，例如 `audit-ai-1001` |
| `workflow_task_id` | 是 | 工作流系统任务唯一ID |
| `workflow_task_no` | 否 | 工作流任务编号，用于展示或排查 |
| `workflow_code` | 否 | 工作流编码 |
| `workflow_name` | 否 | 工作流名称 |
| `status` | 是 | 工作流整体状态 |
| `progress_percent` | 否 | 整体进度，0-100 |
| `progress_text` | 否 | 整体进度说明 |
| `started_at` | 否 | 工作流开始时间 |
| `finished_at` | 否 | 工作流结束时间 |
| `duration_ms` | 否 | 总耗时毫秒 |
| `stages` | 是 | 工作流节点执行明细 |
| `result` | 终态成功时必填 | 最终检测结果 |
| `error` | 失败时必填 | 整体失败信息 |
| `callback_event_id` | 是 | 回调事件唯一ID，用于幂等 |
| `callback_time` | 是 | 回调时间 |

## 7. 状态枚举

### 7.1 工作流整体状态

| 状态 | 含义 |
| --- | --- |
| `accepted` | 已接收 |
| `waiting` | 等待调度 |
| `running` | 执行中 |
| `completed` | 已完成 |
| `failed` | 已失败 |
| `cancelled` | 已取消 |

业务系统会映射到 AI 任务状态：

| 工作流状态 | 业务系统 `task_status` |
| --- | --- |
| `accepted` | `waiting` |
| `waiting` | `waiting` |
| `running` | `executing` |
| `completed` | `completed` |
| `failed` | `failed` |
| `cancelled` | `paused` 或 `failed`，需双方确认 |

### 7.2 节点状态

| 状态 | 含义 |
| --- | --- |
| `pending` | 未开始 |
| `waiting` | 等待中 |
| `running` | 执行中 |
| `completed` | 已完成 |
| `failed` | 已失败 |
| `skipped` | 已跳过 |

## 8. stages 节点结构

工作流系统应在 `stages` 中提供每个关键节点的结构化信息。

示例：

```json
{
  "stage_code": "preprocess",
  "stage_name": "报告预处理",
  "stage_status": "completed",
  "agent_name": "预处理智能体",
  "started_at": "2026-05-11 10:00:10",
  "finished_at": "2026-05-11 10:00:35",
  "duration_ms": 25000,
  "summary": "完成报告文本抽取和结构化解析",
  "detail": "抽取文本 12000 字；识别核心字段 18 个",
  "output": {
    "text_length": 12000,
    "field_count": 18
  },
  "error": null,
  "sort_num": 20
}
```

### 8.1 stages 字段说明

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `stage_code` | 是 | 稳定节点编码 |
| `stage_name` | 是 | 节点展示名称 |
| `stage_status` | 是 | 节点状态 |
| `agent_name` | 否 | 智能体、工具或节点名称 |
| `started_at` | 否 | 节点开始时间 |
| `finished_at` | 否 | 节点结束时间 |
| `duration_ms` | 否 | 节点耗时毫秒 |
| `summary` | 否 | 一句话摘要 |
| `detail` | 否 | 详细说明，可用分号或换行分隔 |
| `output` | 否 | 节点结构化输出，JSON 对象 |
| `error` | 失败时必填 | 节点错误信息 |
| `sort_num` | 是 | 展示排序 |

## 9. 推荐节点编码

工作流系统可根据实际流程调整，但建议尽量稳定使用以下编码：

| stage_code | stage_name | 说明 |
| --- | --- | --- |
| `queued` | 任务入队 | 任务进入工作流队列 |
| `preprocess` | 报告预处理 | 报告下载、文本抽取、格式转换、字段识别 |
| `retrieval` | 依据检索 | 检索审核依据、标准文件、知识库片段 |
| `compare` | 依据比对 | 将报告内容与依据文件进行比对 |
| `generate` | 结果生成 | 生成问题、建议、摘要 |
| `finalize` | 结果归档 | 汇总最终输出 |

如果工作流系统内部节点更细，可以继续拆分，例如：

- `download_report`
- `parse_pdf`
- `extract_fields`
- `search_basis`
- `compare_fields`
- `generate_findings`

但每个 `stage_code` 必须稳定，不能同一含义每次返回不同编码。

## 10. result 最终结果结构

终态成功时，工作流系统应返回最终结构化结果。

```json
{
  "summary": "本次AI审核发现2类问题，建议补充检测依据说明并修正报告编号。",
  "review_opinion": "建议退回修改后重新提交。",
  "findings": [
    {
      "finding_type": "数据错误",
      "finding_title": "报告编号与任务单不一致",
      "finding_content": "报告编号为 A001，任务单编号为 B001，两者不一致。建议核对后修改。",
      "severity": "medium",
      "sort_num": 1,
      "evidence": [
        {
          "source": "报告正文",
          "quote": "报告编号：A001",
          "page": 1
        },
        {
          "source": "任务单",
          "quote": "任务编号：B001",
          "page": 1
        }
      ]
    }
  ],
  "raw_output": {}
}
```

### 10.1 result 字段说明

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `summary` | 否 | AI 总结，业务系统可保存为 `ai_summary` |
| `review_opinion` | 否 | AI 建议复核意见 |
| `findings` | 是 | 检测问题列表，可为空数组 |
| `raw_output` | 否 | 原始输出，便于排查 |

### 10.2 findings 字段说明

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `finding_type` | 是 | 问题类型 |
| `finding_title` | 是 | 问题标题 |
| `finding_content` | 是 | 问题内容 |
| `severity` | 否 | 严重程度 |
| `sort_num` | 是 | 排序 |
| `evidence` | 否 | 证据列表 |

业务系统当前可直接落库的字段包括：

- `finding_type`
- `finding_title`
- `finding_content`
- `sort_num`

`severity` 和 `evidence` 可先放入扩展 JSON 或后续扩表。

## 11. 失败回调要求

当工作流整体失败时，必须返回：

```json
{
  "status": "failed",
  "error": {
    "code": "REPORT_DOWNLOAD_FAILED",
    "message": "报告文件下载失败",
    "detail": "HTTP 404: report file not found",
    "stage_code": "preprocess"
  }
}
```

失败节点也应在 `stages` 中体现：

```json
{
  "stage_code": "preprocess",
  "stage_name": "报告预处理",
  "stage_status": "failed",
  "agent_name": "预处理智能体",
  "summary": "报告预处理失败",
  "detail": "报告文件下载失败",
  "error": {
    "code": "REPORT_DOWNLOAD_FAILED",
    "message": "报告文件下载失败",
    "detail": "HTTP 404: report file not found"
  },
  "sort_num": 20
}
```

业务系统会据此：

- 将 `audit_ai_task.task_status` 更新为 `failed`。
- 将 `progress_text` 更新为错误摘要。
- 将失败节点保存到 `audit_ai_flow_stage`。
- 在 `AI任务详情` 页面展示红色失败节点和错误信息。

## 12. 幂等和重试要求

工作流系统可能因为网络问题重复发送回调。为避免业务系统重复落库，工作流系统应提供：

- `callback_event_id`：每次回调事件唯一ID。
- `workflow_task_id`：工作流任务唯一ID。
- `biz_id`：业务任务唯一ID。

建议规则：

- 同一个 `callback_event_id` 重复发送时，业务系统应只处理一次。
- 同一个 `workflow_task_id` 的终态回调重复发送时，业务系统应覆盖更新为同一结果，不重复插入 findings。
- 如果业务系统触发重新分析，应创建新的 `workflow_task_id`，避免新旧结果混淆。

## 13. 时间格式要求

推荐统一使用：

```text
yyyy-MM-dd HH:mm:ss
```

例如：

```text
2026-05-11 10:03:10
```

如果工作流系统使用 ISO 8601，也可以接受，但需要提前约定时区：

```text
2026-05-11T10:03:10+08:00
```

所有时间应明确为北京时间或携带时区。

## 14. 文件访问要求

业务系统传给工作流系统的文件 URL 可能包括：

- 主报告文件 `report_file_url`
- 审核依据文件 `basis_file_urls`
- 附件文件 `appendix_file_urls`

工作流系统需要确认：

- 是否能访问业务系统文件 URL。
- 是否需要业务系统提供带签名的临时 URL。
- 文件下载失败时返回明确错误码和失败节点。
- 不要在回调中返回大文件内容，只返回摘要、结构化结果或文件引用。

## 15. 安全要求

工作流系统回调业务系统时建议支持以下安全机制之一：

- 请求头携带固定密钥，例如 `X-Workflow-Token`。
- 请求头携带 HMAC 签名，例如 `X-Workflow-Signature`。
- 使用内网地址和网关鉴权。

建议签名内容包括：

- 请求体原文。
- 时间戳。
- 共享密钥。

业务系统可以校验签名，防止伪造回调。

## 16. 完整成功回调示例

```json
{
  "biz_id": "audit-ai-1001",
  "workflow_task_id": "wf-task-202605110001",
  "workflow_task_no": "WF-202605110001",
  "workflow_code": "audit_report_review",
  "workflow_name": "报告AI审核工作流",
  "status": "completed",
  "progress_percent": 100,
  "progress_text": "AI分析完成",
  "started_at": "2026-05-11 10:00:00",
  "finished_at": "2026-05-11 10:03:10",
  "duration_ms": 190000,
  "stages": [
    {
      "stage_code": "queued",
      "stage_name": "任务入队",
      "stage_status": "completed",
      "agent_name": "工作流调度器",
      "started_at": "2026-05-11 10:00:00",
      "finished_at": "2026-05-11 10:00:01",
      "duration_ms": 1000,
      "summary": "任务已进入工作流队列",
      "detail": "任务优先级：高优先级",
      "output": {},
      "error": null,
      "sort_num": 10
    },
    {
      "stage_code": "preprocess",
      "stage_name": "报告预处理",
      "stage_status": "completed",
      "agent_name": "预处理智能体",
      "started_at": "2026-05-11 10:00:10",
      "finished_at": "2026-05-11 10:00:35",
      "duration_ms": 25000,
      "summary": "完成报告文本抽取和结构化解析",
      "detail": "抽取文本 12000 字；识别核心字段 18 个",
      "output": {
        "text_length": 12000,
        "field_count": 18
      },
      "error": null,
      "sort_num": 20
    },
    {
      "stage_code": "retrieval",
      "stage_name": "依据检索",
      "stage_status": "completed",
      "agent_name": "知识库检索智能体",
      "started_at": "2026-05-11 10:00:36",
      "finished_at": "2026-05-11 10:01:20",
      "duration_ms": 44000,
      "summary": "完成审核依据检索",
      "detail": "命中标准 3 份；命中依据片段 12 条",
      "output": {
        "hit_count": 12
      },
      "error": null,
      "sort_num": 30
    },
    {
      "stage_code": "compare",
      "stage_name": "依据比对",
      "stage_status": "completed",
      "agent_name": "比对智能体",
      "started_at": "2026-05-11 10:01:21",
      "finished_at": "2026-05-11 10:02:30",
      "duration_ms": 69000,
      "summary": "完成报告与依据文件比对",
      "detail": "完成 10 个字段比对；发现 2 处疑似不一致",
      "output": {
        "compare_count": 10,
        "mismatch_count": 2
      },
      "error": null,
      "sort_num": 40
    },
    {
      "stage_code": "generate",
      "stage_name": "结果生成",
      "stage_status": "completed",
      "agent_name": "结果生成智能体",
      "started_at": "2026-05-11 10:02:31",
      "finished_at": "2026-05-11 10:03:10",
      "duration_ms": 39000,
      "summary": "生成AI检测结果和整改建议",
      "detail": "生成 2 条问题；生成审核摘要 1 条",
      "output": {
        "finding_count": 2
      },
      "error": null,
      "sort_num": 50
    }
  ],
  "result": {
    "summary": "本次AI审核发现2类问题，建议补充检测依据说明并修正报告编号。",
    "review_opinion": "建议退回修改后重新提交。",
    "findings": [
      {
        "finding_type": "数据错误",
        "finding_title": "报告编号与任务单不一致",
        "finding_content": "报告编号为 A001，任务单编号为 B001，两者不一致。建议核对后修改。",
        "severity": "medium",
        "sort_num": 1,
        "evidence": [
          {
            "source": "报告正文",
            "quote": "报告编号：A001",
            "page": 1
          }
        ]
      }
    ],
    "raw_output": {}
  },
  "error": null,
  "callback_event_id": "evt-20260511100310-0001",
  "callback_time": "2026-05-11 10:03:10"
}
```

## 17. 完整失败回调示例

```json
{
  "biz_id": "audit-ai-1001",
  "workflow_task_id": "wf-task-202605110001",
  "workflow_task_no": "WF-202605110001",
  "workflow_code": "audit_report_review",
  "workflow_name": "报告AI审核工作流",
  "status": "failed",
  "progress_percent": 35,
  "progress_text": "报告预处理失败",
  "started_at": "2026-05-11 10:00:00",
  "finished_at": "2026-05-11 10:00:35",
  "duration_ms": 35000,
  "stages": [
    {
      "stage_code": "queued",
      "stage_name": "任务入队",
      "stage_status": "completed",
      "agent_name": "工作流调度器",
      "started_at": "2026-05-11 10:00:00",
      "finished_at": "2026-05-11 10:00:01",
      "duration_ms": 1000,
      "summary": "任务已进入工作流队列",
      "detail": "任务优先级：高优先级",
      "output": {},
      "error": null,
      "sort_num": 10
    },
    {
      "stage_code": "preprocess",
      "stage_name": "报告预处理",
      "stage_status": "failed",
      "agent_name": "预处理智能体",
      "started_at": "2026-05-11 10:00:10",
      "finished_at": "2026-05-11 10:00:35",
      "duration_ms": 25000,
      "summary": "报告预处理失败",
      "detail": "报告文件下载失败",
      "output": {},
      "error": {
        "code": "REPORT_DOWNLOAD_FAILED",
        "message": "报告文件下载失败",
        "detail": "HTTP 404: report file not found"
      },
      "sort_num": 20
    }
  ],
  "result": null,
  "error": {
    "code": "REPORT_DOWNLOAD_FAILED",
    "message": "报告文件下载失败",
    "detail": "HTTP 404: report file not found",
    "stage_code": "preprocess"
  },
  "callback_event_id": "evt-20260511100035-0001",
  "callback_time": "2026-05-11 10:00:35"
}
```

## 18. 联调验收清单

工作流系统侧完成配合后，双方按以下场景验收。

### 18.1 成功场景

- 业务系统创建 AI 任务并调用工作流。
- 工作流系统返回 `workflow_task_id`。
- 工作流完成后回调业务系统。
- 回调中包含完整 `stages`。
- 业务系统详情接口返回 `flowStageList`。
- `AI任务详情` 页面显示真实节点，而不是前端推导节点。

### 18.2 失败场景

- 工作流某节点失败。
- 回调顶层 `status` 为 `failed`。
- 失败节点 `stage_status` 为 `failed`。
- 顶层 `error` 和节点 `error` 均有明确错误码和错误说明。
- 业务系统页面显示失败节点和失败原因。

### 18.3 空结果场景

- 工作流成功完成，但没有发现问题。
- `result.findings` 返回空数组。
- `result.summary` 说明未发现异常或暂无结构化问题。
- 业务系统页面显示流程完成，检测结果为空。

### 18.4 重试场景

- 同一 AI 任务触发重新分析。
- 工作流系统创建新的 `workflow_task_id`。
- 回调中的 `workflow_task_id` 能区分新旧执行。
- 业务系统展示最新一次执行流程。

### 18.5 重复回调场景

- 工作流系统重复发送同一 `callback_event_id`。
- 业务系统不重复插入问题和节点。
- 页面结果保持一致。

## 19. 需要双方确认的问题

实施前建议双方确认：

1. `biz_id` 格式是否统一使用 `audit-ai-{aiTaskId}`。
2. 工作流系统能否在终态回调中返回完整 `stages`。
3. 工作流系统是否支持过程回调。
4. 回调鉴权方式使用固定 token、签名还是网关鉴权。
5. 文件 URL 是否需要临时签名。
6. 失败状态是否统一使用 `failed`。
7. 取消状态 `cancelled` 在业务系统中映射为 `paused` 还是 `failed`。
8. 是否需要保留多次分析历史。如果需要，应增加 `run_id` 或 `analysis_no`。
9. `evidence`、`severity` 等扩展字段是否首期落库。
10. 回调时间统一使用北京时间还是 ISO 8601 带时区。

## 20. 最小可交付版本

如果工作流系统侧希望先做最小改造，至少需要提供：

- `biz_id`
- `workflow_task_id`
- `status`
- `progress_percent`
- `progress_text`
- `stages`
- `result.summary`
- `result.findings`
- 失败时的 `error`
- `callback_event_id`
- `callback_time`

其中 `stages` 至少包含：

- `stage_code`
- `stage_name`
- `stage_status`
- `summary`
- `detail`
- `sort_num`

具备以上字段后，业务系统即可完成第二阶段的真实流转状态展示。
