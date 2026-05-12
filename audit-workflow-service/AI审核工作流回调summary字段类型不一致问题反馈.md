# AI审核工作流回调 summary 字段类型不一致问题反馈

## 1. 问题背景

业务系统在“不上传依据文件”的审核场景中，创建 `policy_document_audit` 工作流任务后，工作流实际已经完成审核并生成结果，但业务系统页面长时间停留在：

```text
AI审核工作流分析中
```

本次问题不是工作流未执行完成，也不是知识库未调用，而是工作流完成后的 callback 数据结构与业务系统约定不一致，导致业务系统未能回写结果。

## 2. 复测任务

复测时间：2026-05-11 20:37 至 20:53

| 项目 | 值 |
| --- | --- |
| 产品名称 | 本安-矿用本安型手机 |
| 审核任务编号 | SF-1778503037007 |
| 业务审核任务 ID | 37 |
| 业务 AI 任务 ID | 37 |
| 工作流任务 ID | 22 |
| 工作流任务编号 | AUDIT-20260511-000013 |
| 工作流 bizId | AI-TASK-37 |
| 工作流编码 | policy_document_audit |

业务系统当前状态：

```text
ai_task_id = 37
task_status = executing
progress_percent = 35
progress_text = AI审核工作流分析中
ai_summary = 空
audit_ai_finding = 0 条
```

工作流任务实际状态：

```text
task_id = 22
task_no = AUDIT-20260511-000013
task_status = SUCCESS
finish_time = 2026-05-11 20:43:19
summary = 本次审核发现17个问题，1个片段需人工复核
```

工作流结果接口可正常返回：

```http
GET http://127.0.0.1:8080/api/audit/tasks/22/result
```

结果摘要：

```json
{
  "success": true,
  "summary": "本次审核发现17个问题，1个片段需人工复核",
  "totalIssues": 17,
  "partial_success": true,
  "chunk_success_count": 14,
  "chunk_failed_count": 1
}
```

## 3. 知识库调用与分片审核情况

本次知识库调用正常：

```text
AUDIT-20260511-000013-KB-BATCH-82476450817525
query_count = 10
result_count = 40
status = success

AUDIT-20260511-000013-KB-BATCH-82477943852409
query_count = 5
result_count = 20
status = success
```

分片模型调用情况：

```text
SUCCESS = 14
FAILED / MODEL_TIMEOUT = 1
```

失败分片：

```text
source_chunk_id = 362
source_chunk_no = 14
error_code = MODEL_TIMEOUT
duration_ms = 300001
```

工作流已经按部分成功方式返回：

```json
{
  "partial_success": true,
  "failed_chunks": [
    {
      "source_chunk_id": 362,
      "source_chunk_no": 14,
      "error_code": "MODEL_TIMEOUT"
    }
  ]
}
```

这一点说明此前“单个分片超时导致工作流长期 RUNNING”的问题在该任务中已有改善。

## 4. 回调失败证据

工作流 callback 记录：

```text
callback_id = 5
task_id = 22
task_no = AUDIT-20260511-000013
callback_url = http://127.0.0.1:6039/audit/ai/workflow/callback
callback_status = SUCCESS
response_status = 200
```

但业务系统响应 body 实际为：

```json
{
  "code": 500,
  "msg": "JSON parse error: Cannot construct instance of `java.util.LinkedHashMap` ... no String-argument constructor/factory method to deserialize from String value ('本次审核发现17个问题，1个片段需人工复核')"
}
```

也就是说，HTTP 状态码是 200，但业务响应体是失败：

```text
code = 500
```

工作流侧把该回调记录为 `callback_status = SUCCESS`，但业务系统实际没有完成结果落库。

## 5. 根因

工作流本次 callback payload 中的 `summary` 字段为字符串：

```json
{
  "summary": "本次审核发现17个问题，1个片段需人工复核"
}
```

而业务系统此前按对象结构接收：

```json
{
  "summary": {
    "success": true,
    "summary": "本次审核发现17个问题，1个片段需人工复核",
    "risk_level": "high",
    "totalIssues": 17
  }
}
```

因此请求进入 Controller 前就被 JSON 反序列化拦截，业务系统没有执行 `fetchAndMapResult`，也没有把工作流结果回写到 `audit_ai_task` 和 `audit_ai_finding`。

## 6. 业务系统侧兼容修复

业务系统侧已做兼容调整：

```text
AuditWorkflowCallback.summary
```

由固定对象类型改为可接收字符串或对象：

```java
private Object summary;
```

涉及文件：

```text
backend/ruoyi-system/src/main/java/com/ruoyi/system/domain/audit/workflow/AuditWorkflowCallback.java
```

该修复可避免工作流传字符串 `summary` 时 callback 请求直接反序列化失败。

## 7. 仍需工作流侧确认与调整

请工作流系统侧确认并调整以下问题：

1. callback 顶层 `summary` 字段类型是否应保持对象结构。
2. 如果需要返回纯文本摘要，建议使用单独字段：

```json
{
  "summary_text": "本次审核发现17个问题，1个片段需人工复核"
}
```

3. 终态成功 callback 建议直接返回结构化摘要：

```json
{
  "summary": {
    "success": true,
    "summary": "本次审核发现17个问题，1个片段需人工复核",
    "risk_level": "high",
    "totalIssues": 17,
    "partial_success": true,
    "chunk_failed_count": 1
  }
}
```

4. 工作流侧 callback 判定成功时，不应只看 HTTP 状态码 `200`，还应识别业务响应体：

```json
{
  "code": 200
}
```

如果响应体为：

```json
{
  "code": 500
}
```

应判定 callback 失败并进入重试，而不是记录为 `callback_status = SUCCESS`。

5. 对本次任务 `AUDIT-20260511-000013`，工作流侧 callback 实际未被业务系统成功处理，建议支持重新触发 callback。

## 8. 期望结果

工作流 callback 应满足：

```text
HTTP 200 且 body.code = 200 才算回调成功
```

业务系统收到 callback 后应能完成：

```text
task_status = completed
progress_percent = 100
progress_text = AI分析完成，待人工审核
ai_summary = 本次审核发现17个问题，1个片段需人工复核
audit_ai_finding = 17 条
```

当前业务系统侧已经增强兼容，但建议工作流侧仍按稳定协议返回结构化 `summary`，并修正 callback 成功判定逻辑。

## 9. 工作流侧处理结果

处理时间：2026-05-11 晚间

### 9.1 已修复 callback 顶层 summary 类型

已将工作流 callback payload 顶层 `summary` 从纯字符串调整为结构化对象。

调整后成功回调中的结构类似：

```json
{
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
  "summary_text": "本次审核发现17个问题，1个片段需人工复核"
}
```

说明：

1. `summary` 保持对象结构，兼容业务系统历史接收方式。
2. `summary_text` 提供纯文本摘要，后续如果业务系统只需要展示文案，可以直接使用该字段。
3. `result.summary` 仍保留为结果内部摘要文本，不影响 `/api/audit/tasks/{taskId}/result` 的现有结构。

### 9.2 已修复 callback 成功判定

工作流侧 callback 成功判定已从：

```text
HTTP 状态码为 2xx 即成功
```

调整为：

```text
HTTP 状态码为 2xx，且当响应体存在 code 字段时 code 必须等于 200
```

如果业务系统返回：

```json
{
  "code": 500,
  "msg": "JSON parse error ..."
}
```

工作流侧会将该 callback 记录为 `FAILED`，错误信息类似：

```text
business callback code 500: JSON parse error ...
```

随后 callback 重试调度会按现有机制重试，不会再误记为 `SUCCESS`。

### 9.3 已增强手动重触发 callback

`POST /api/audit/callbacks/{taskId}/retry` 已增强：

1. 如果存在失败 callback，继续重试失败 callback。
2. 如果没有失败 callback，但任务已经是终态 `SUCCESS`、`FAILED` 或 `CANCELED`，会基于当前任务结果重新生成一条新的 callback 并发送。

因此，对于本次历史任务：

```text
task_id = 22
task_no = AUDIT-20260511-000013
```

工作流服务重启后，可以手动调用：

```bash
curl -X POST http://127.0.0.1:8080/api/audit/callbacks/22/retry
```

用于重新向业务系统发送修复后的 callback payload。

### 9.4 涉及代码

```text
src/main/java/com/audit/workflow/service/CallbackService.java
```

### 9.5 验证结果

已执行编译验证：

```bash
mvn -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

### 9.6 需要业务系统侧重新确认

请业务系统侧在工作流服务重启后确认：

1. 新任务 callback 顶层 `summary` 为对象。
2. `summary_text` 为纯文本摘要。
3. 业务系统 callback 返回 `code=200` 时，工作流记录为 `SUCCESS`。
4. 业务系统 callback 返回 `code!=200` 时，工作流记录为 `FAILED` 并进入重试。
5. 对历史任务 `task_id=22` 可通过 `/api/audit/callbacks/22/retry` 重新触发回调补偿。
