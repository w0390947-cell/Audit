# 业务系统接入 AI 审核工作流系统说明

本文档面向将要接入 AI 审核工作流系统的业务系统，说明业务系统如何创建审核任务、传入待审核文件或文本、接收审核结果、查询任务状态、回写人工复核结论，以及如果业务系统同时负责知识库，需要提供哪些知识库检索接口。

## 1. 接入边界

AI 审核工作流系统定位为后端审核服务，不提供独立前端页面。

业务系统负责：

1. 用户登录、权限、菜单和页面。
2. 待审核文件来源。
3. 审核标准知识库建设。
4. 审核结果展示。
5. 人工复核和业务流转。
6. 必要时提供知识库检索 API。

工作流系统负责：

1. 接收审核任务。
2. 解析待审核文件或文本。
3. 调用业务系统知识库检索依据。
4. 调用 AI 模型生成结构化审核结果。
5. 保存审核任务、日志、检索快照、模型调用记录和审核结果。
6. 回调业务系统。
7. 提供任务状态、审核结果、复核回写和重试 API。

## 2. 接入方式

推荐接入方式：

```text
业务系统
  -> 调用工作流系统创建任务 API
  -> 工作流系统异步执行审核
  -> 工作流系统回调业务系统 callback_url
  -> 业务系统调用结果查询 API 获取完整结果
  -> 业务系统展示结果并回写人工复核结论
```

工作流系统默认地址示例：

```text
http://127.0.0.1:8080
```

生产环境应替换为实际部署地址。

## 3. 工作流编码

当前内置示例工作流：

```text
policy_document_audit
```

业务系统可以先调用工作流列表接口确认可用工作流。

### 3.1 查询可用工作流

```http
GET /api/audit/workflows
```

示例：

```bash
curl http://127.0.0.1:8080/api/audit/workflows
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "workflowCode": "policy_document_audit",
      "workflowName": "制度文件审核工作流",
      "description": "阶段5工作流：输入解析、知识库检索、AI审核、结构化结果保存、回调通知均已接入真实节点",
      "enabled": true
    }
  ]
}
```

## 4. 创建审核任务

```http
POST /api/audit/tasks
Content-Type: application/json
```

### 4.1 纯文本输入

如果业务系统已经解析好了文件文本，可以直接传 `text`。

```json
{
  "workflow_code": "policy_document_audit",
  "biz_id": "PROJECT-TEST-001",
  "callback_url": "http://业务系统地址/api/audit/callback",
  "input": {
    "text": "这是一个测试待审核文件内容。请检查是否符合项目审批要求。",
    "file_name": "测试文件.txt",
    "file_type": "txt",
    "metadata": {
      "business_type": "project",
      "department_id": "D001",
      "submitter_id": "U001"
    },
    "knowledge_scope": {
      "knowledge_base_codes": ["project_policy"],
      "effective_only": true
    },
    "caller_context": {
      "user_id": "U001",
      "dept_id": "D001",
      "tenant_id": "T001",
      "permission_mode": "caller_user"
    }
  }
}
```

### 4.2 文件 URL 输入

如果业务系统提供可访问的文件下载地址，可以传 `file_url`。

```json
{
  "workflow_code": "policy_document_audit",
  "biz_id": "PROJECT-TEST-002",
  "callback_url": "http://业务系统地址/api/audit/callback",
  "input": {
    "file_id": "FILE-001",
    "file_url": "http://业务系统地址/files/FILE-001/download",
    "file_name": "待审核文件.pdf",
    "file_type": "pdf",
    "metadata": {
      "business_type": "project"
    },
    "knowledge_scope": {
      "folder_ids": [4, 7],
      "resource_ids": [8, 9],
      "effective_only": true
    },
    "caller_context": {
      "user_id": "U001",
      "dept_id": "D001",
      "permission_mode": "explicit_scope"
    }
  }
}
```

### 4.3 字段说明

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| workflow_code | 是 | 工作流编码，例如 `policy_document_audit` |
| biz_id | 建议必填 | 业务系统侧 ID，用于业务追踪 |
| callback_url | 否 | 工作流结束后回调地址 |
| input.text | text/file_url 二选一 | 待审核文本 |
| input.file_url | text/file_url 二选一 | 待审核文件下载地址 |
| input.file_id | 否 | 业务系统文件 ID |
| input.file_name | 建议 | 文件名，用于结果展示 |
| input.file_type | 建议 | 文件类型：`pdf/doc/docx/txt/md` |
| input.metadata | 否 | 业务元数据 |
| input.knowledge_scope | 否 | 知识库检索范围 |
| input.caller_context | 否 | 调用用户上下文和权限模式 |

> 当前 `policy_document_audit` 工作流要求必须调用业务系统审核文件库。建议业务系统始终传入 `input.knowledge_scope.knowledge_base_codes`，例如 `["default"]`。如果不传，工作流会使用自身配置的默认范围 `default`；如果工作流侧未配置知识库服务地址，会返回 `KB_SCOPE_REQUIRED`。

### 4.4 响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 1,
    "taskNo": "AUDIT-20260510-000001",
    "taskStatus": "PENDING"
  }
}
```

业务系统应保存：

1. `taskId`
2. `taskNo`
3. `biz_id` 与 `taskId/taskNo` 的映射关系

## 5. 查询审核任务状态

```http
GET /api/audit/tasks/{taskId}
```

示例：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/1
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 1,
    "taskNo": "AUDIT-20260510-000001",
    "workflowCode": "policy_document_audit",
    "bizId": "PROJECT-TEST-001",
    "taskStatus": "SUCCESS",
    "currentNodeCode": "callback",
    "summary": "{\"overall_result\":\"通过\",\"risk_level\":\"low\",\"total_issues\":0}",
    "errorCode": "",
    "errorMsg": null,
    "retryCount": 0
  }
}
```

任务状态：

| 状态 | 说明 |
| --- | --- |
| PENDING | 已创建，等待执行 |
| RUNNING | 正在执行 |
| SUCCESS | 执行成功 |
| FAILED | 执行失败 |
| RETRYING | 等待重试 |
| CANCELED | 已取消，当前版本暂未开放取消 API |

## 6. 查询审核结果

```http
GET /api/audit/tasks/{taskId}/result
```

示例：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/1/result
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "summary": "本次审核发现1个问题",
    "totalIssues": 1,
    "findings": [
      {
        "type": "标准不符",
        "title": "缺少必要审批信息",
        "content": "文件中未体现项目负责人审批意见。",
        "severity": "high",
        "location": "第三章 项目审批",
        "suggestion": "补充项目负责人审批意见，并保留审批记录。"
      }
    ]
  }
}
```

业务系统展示时建议重点展示：

1. 总体结论：`summary`
2. 问题数量：`totalIssues`
3. 问题列表：`findings`
4. 问题类型：`findings[].type`
5. 问题标题、严重程度、位置、问题描述、修改建议

如果没有发现问题，`data` 为：

```json
{
  "success": true,
  "summary": "未发现关键问题",
  "totalIssues": 0,
  "findings": []
}
```

## 7. 回调接收

业务系统创建任务时如果传入 `callback_url`，工作流系统会在任务最终完成后调用该地址。

### 7.1 回调方式

```http
POST {callback_url}
Content-Type: application/json
X-Request-Id: CALLBACK-{callbackId}
X-Audit-Task-No: {taskNo}
Authorization: Bearer {AUDIT_CALLBACK_TOKEN}
```

如果工作流系统配置了 `AUDIT_CALLBACK_TOKEN`，会带上 `Authorization` 请求头。

### 7.2 成功回调示例

```json
{
  "task_id": 1,
  "task_no": "AUDIT-20260510-000001",
  "workflow_code": "policy_document_audit",
  "biz_id": "PROJECT-TEST-001",
  "task_status": "SUCCESS",
  "summary": "未发现关键问题",
  "result_url": "/api/audit/tasks/1/result",
  "finished_at": "2026-05-10T21:30:00",
  "error": null
}
```

### 7.3 失败回调示例

```json
{
  "task_id": 1,
  "task_no": "AUDIT-20260510-000001",
  "workflow_code": "policy_document_audit",
  "biz_id": "PROJECT-TEST-001",
  "task_status": "FAILED",
  "summary": null,
  "result_url": null,
  "finished_at": "2026-05-10T21:30:00",
  "error": {
    "error_code": "MODEL_TIMEOUT",
    "error_msg": "模型调用超时"
  }
}
```

### 7.4 回调响应要求

业务系统收到回调后应返回 HTTP 2xx。

如果返回非 2xx，或网络异常，工作流系统会记录 `audit_callback_log` 并自动重试。

默认重试策略：

| 次数 | 间隔 |
| --- | --- |
| 第 1 次 | 1 分钟 |
| 第 2 次 | 5 分钟 |
| 第 3 次 | 15 分钟 |

### 7.5 手动重试回调

```http
POST /api/audit/callbacks/{taskId}/retry
```

示例：

```bash
curl -X POST http://127.0.0.1:8080/api/audit/callbacks/1/retry
```

## 8. 人工复核回写

业务系统展示审核结果后，如果用户确认、驳回或修改 AI 问题，可以调用复核回写接口。

```http
POST /api/audit/tasks/{taskId}/review
Content-Type: application/json
```

请求示例：

```json
{
  "task_no": "AUDIT-20260510-000001",
  "reviewer_id": "U001",
  "reviewer_name": "张三",
  "source_system": "business-system",
  "feedbacks": [
    {
      "issue_id": 30001,
      "review_status": "confirmed",
      "feedback_content": "该问题属实"
    },
    {
      "issue_id": 30002,
      "review_status": "rejected",
      "feedback_content": "业务系统已有补充材料，不作为问题"
    }
  ]
}
```

字段说明：

| 字段 | 说明 |
| --- | --- |
| reviewer_id | 复核人 ID |
| reviewer_name | 复核人名称 |
| source_system | 来源系统 |
| issue_id | 工作流系统中的问题 ID |
| review_status | `confirmed/rejected/modified` |
| feedback_content | 复核意见 |
| corrected_issue | 修改后的问题内容，可选 |

响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "task_id": 1,
    "saved_feedbacks": 2
  }
}
```

## 9. 失败任务重试

如果任务状态为 `FAILED`，业务系统可以调用重试接口。

```http
POST /api/audit/tasks/{taskId}/retry
```

示例：

```bash
curl -X POST http://127.0.0.1:8080/api/audit/tasks/1/retry
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 1,
    "taskStatus": "RETRYING",
    "retryCount": 1
  }
}
```

说明：

1. 只有 `FAILED` 任务允许重试。
2. 默认最大重试次数为 3。
3. 重试后后台调度器会重新执行任务。

## 10. 统计接口

```http
GET /api/audit/stats/overview
```

示例：

```bash
curl http://127.0.0.1:8080/api/audit/stats/overview
```

响应示例：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "task_total": 10,
    "task_success": 8,
    "task_failed": 2,
    "model_call_total": 20,
    "retrieval_total": 30,
    "callback_success": 8,
    "callback_failed": 1,
    "review_feedback_total": 5
  }
}
```

## 11. 业务知识库检索接口要求

如果业务系统负责审核标准知识库，需要向工作流系统提供知识库检索接口。

当前本机联调配置：

```yaml
audit:
  knowledge:
    base-url: http://127.0.0.1:6039
    batch-search-endpoint: /audit/library/vector/workflow-batch-search
    search-endpoint: /audit/library/vector/workflow-search
    required: true
```

工作流系统优先调用：

```http
POST /audit/library/vector/workflow-batch-search
```

如果批量接口不可用，会降级到：

```http
POST /audit/library/vector/workflow-search
```

### 11.1 批量检索请求

```json
{
  "request_id": "AUDIT-20260510-000001-KB-BATCH-001",
  "workflow_code": "policy_document_audit",
  "task_id": "AUDIT-20260510-000001",
  "knowledge_scope": {
    "knowledge_base_codes": ["project_policy"],
    "effective_only": true
  },
  "caller_context": {
    "user_id": "U001",
    "dept_id": "D001",
    "permission_mode": "caller_user"
  },
  "retrieval_config": {
    "top_k": 8,
    "min_score": 0.3,
    "hybrid": true,
    "rerank": true
  },
  "queries": [
    {
      "query_id": "AUDIT-20260510-000001-KB-1001",
      "query": "请检索与以下待审核内容相关的审核依据、制度条款、标准要求：...",
      "source_chunk_id": 1001
    }
  ]
}
```

### 11.2 批量检索响应

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "groups": [
      {
        "source_chunk_id": 1001,
        "results": [
          {
            "chunk_id": "KB-CHUNK-10001",
            "document_id": "KB-DOC-20001",
            "resource_id": 8,
            "resource_type": "common",
            "file_name": "项目管理制度.pdf",
            "file_url": "/profile/upload/...",
            "version_no": "v1.2",
            "page_no": 3,
            "section_title": "第 4.2 条 审批要求",
            "section_path": "项目管理制度 / 第四章 / 第 4.2 条",
            "rule_code": "PM-4.2",
            "chunk_text": "项目立项材料应包含项目负责人审批意见，并保留审批记录。",
            "score": 0.82,
            "rank_score": 0.91,
            "effective_date": "2025-01-01",
            "status": "effective"
          }
        ]
      }
    ]
  }
}
```

工作流系统会保存：

1. 检索请求。
2. 检索条件。
3. 返回依据。
4. 依据文本快照。
5. 来源文件、版本、页码、章节。

## 12. 文件输入要求

当前工作流系统支持：

| 类型 | 说明 |
| --- | --- |
| text | 业务系统直接传文本 |
| file_url | 工作流系统通过 URL 下载文件 |
| pdf | 使用 PDFBox 解析 |
| doc/docx | 使用 Apache POI 解析 |
| txt/md | 直接读取文本 |

暂不支持：

1. OCR 扫描 PDF。
2. Excel 深度结构化解析。
3. 仅传 `file_id` 但不提供下载地址。

如果业务系统只传 `file_id`，需要额外提供文件下载接口，或在创建任务时同时传 `file_url`。

## 13. 错误码

常见错误码：

| 错误码 | 说明 |
| --- | --- |
| WORKFLOW_NOT_FOUND | 工作流不存在 |
| WORKFLOW_DISABLED | 工作流未启用 |
| TASK_INPUT_INVALID | 任务输入不合法 |
| INPUT_EMPTY | 输入为空 |
| INPUT_FILE_TOO_LARGE | 文件过大 |
| INPUT_FILE_TYPE_UNSUPPORTED | 文件类型不支持 |
| FILE_DOWNLOAD_FAILED | 文件下载失败 |
| PARSE_FAILED | 文件解析失败 |
| PARSE_NO_TEXT | 文件无可解析文本 |
| KB_UNAVAILABLE | 知识库服务不可用 |
| KB_PERMISSION_DENIED | 无知识库检索权限 |
| MODEL_UNAVAILABLE | 模型服务不可用 |
| MODEL_TIMEOUT | 模型调用超时 |
| MODEL_RESPONSE_INVALID | 模型输出不是合法 JSON |
| RESULT_SCHEMA_INVALID | 结果结构不合法 |
| RESULT_REFERENCE_INVALID | 结果引用了不存在的依据 |
| RESULT_NOT_FOUND | 审核结果不存在 |
| TASK_RETRY_NOT_ALLOWED | 当前任务不允许重试 |
| REVIEW_ISSUE_INVALID | 复核问题不属于当前任务 |

## 14. 推荐接入流程

业务系统建议按以下顺序接入：

1. 调用 `GET /api/audit/workflows`，确认工作流可用。
2. 调用 `POST /api/audit/tasks`，先用 `text` 方式跑通任务。
3. 调用 `GET /api/audit/tasks/{taskId}`，轮询任务状态。
4. 调用 `GET /api/audit/tasks/{taskId}/result`，展示审核结果。
5. 接入 `callback_url`，由轮询改为回调通知。
6. 接入 `POST /api/audit/tasks/{taskId}/review`，回写人工复核结论。
7. 如果业务系统负责知识库，提供 `workflow-batch-search` 接口。
8. 最后接入真实文件 URL、真实知识库和真实模型。

## 15. 最小联调样例

不接知识库、不接模型时，业务系统只需要调用创建任务：

```bash
curl -X POST http://127.0.0.1:8080/api/audit/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "workflow_code": "policy_document_audit",
    "biz_id": "PROJECT-TEST-001",
    "input": {
      "text": "这是一个测试待审核文件内容。请检查是否符合项目审批要求。",
      "file_name": "测试文件.txt",
      "file_type": "txt"
    }
  }'
```

然后查询结果：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/1/result
```

该模式可以用于先验证业务系统和工作流系统的基础连通性。
