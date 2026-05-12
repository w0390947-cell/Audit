# AI审核工作流未调用业务知识库问题反馈

## 1. 问题背景

业务系统侧要求：用户在“审核列表管理 > 审核列表”界面点击“新增”按钮，只在“文件上传区”上传待审核文件，不在“依据文件子区”上传任何文件时，AI 审核工作流必须调用业务系统侧“审核文件库”的向量知识库检索接口，基于业务知识库返回的依据完成审核。

本次用户测试任务：

| 项目 | 值 |
| --- | --- |
| 产品名称 | 本安-矿用本安型手机 |
| 审核任务编号 | SF-1778426139917 |
| 业务审核任务 ID | 22 |
| 业务 AI 任务 ID | 21 |
| 工作流任务 ID | 6 |
| 工作流任务编号 | AUDIT-20260510-000005 |
| 工作流 bizId | AI-TASK-21 |

## 2. 当前业务任务结果

业务系统 AI 任务查询结果：

```text
ai_task_id = 21
review_task_id = 22
task_no = SF-1778426139917
product_name = 本安-矿用本安型手机
task_status = completed
progress_text = AI分析完成，未发现关键问题
report_file_url = /profile/upload/2026/05/10/2025520398FB（批注本安部分）(0725)_20260510231537A001.docx
basis_file_urls = 空
```

工作流任务查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/6
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 6,
    "taskNo": "AUDIT-20260510-000005",
    "workflowCode": "policy_document_audit",
    "bizId": "AI-TASK-21",
    "taskStatus": "SUCCESS",
    "currentNodeCode": "callback",
    "summary": "{\"success\": true, \"summary\": \"未发现关键问题\", \"risk_level\": \"low\", \"totalIssues\": 0}",
    "errorCode": "",
    "retryCount": 0,
    "createTime": "2026-05-10T23:16:00",
    "startTime": "2026-05-10T23:16:04",
    "finishTime": "2026-05-10T23:16:07"
  }
}
```

工作流结果查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/6/result
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "summary": "未发现关键问题",
    "findings": [],
    "totalIssues": 0
  }
}
```

## 3. 发现的问题

虽然工作流任务 `taskId=6` 返回了 `SUCCESS`，但业务系统侧没有记录到该任务对应的知识库检索调用。

业务系统侧检索日志表：

```sql
select log_id, request_id, workflow_code, task_id, result_count, top_resource_ids, status, create_time
from audit_vector_search_log
order by log_id desc;
```

实际结果：

```text
log_id  request_id                task_id                  result_count  top_resource_ids  status
3       原因排查-KB-SMOKE-001      原因排查-KB-SMOKE-001    3             20,18             success
2       联调-KB-CODE-001           联调-KB-CODE-001         3             20,18             success
1       联调-KB-HIT-002            联调-KB-HIT-002          3             18                success
```

没有出现以下任一标识：

```text
AI-TASK-21
AUDIT-20260510-000005
taskId = 6
```

结论：业务系统侧没有收到工作流任务 `AI-TASK-21` / `AUDIT-20260510-000005` 对 `/audit/library/vector/workflow-batch-search` 或 `/audit/library/vector/workflow-search` 的调用。

## 4. 业务知识库接口可用性验证

为排除业务系统知识库接口不可用的问题，已手工调用业务系统知识库批量检索接口。

请求：

```bash
curl -X POST http://127.0.0.1:6039/audit/library/vector/workflow-batch-search \
  -H 'Content-Type: application/json' \
  -d '{
    "request_id": "原因排查-KB-SMOKE-001",
    "workflow_code": "policy_document_audit",
    "task_id": "原因排查-KB-SMOKE-001",
    "knowledge_scope": {
      "knowledge_base_codes": ["default"],
      "effective_only": true
    },
    "caller_context": {
      "permission_mode": "explicit_scope"
    },
    "retrieval_config": {
      "top_k": 3,
      "hybrid": true,
      "rerank": false
    },
    "queries": [
      {
        "query_id": "Q-001",
        "query": "矿用本安型手机 防爆 本安 检验报告 委托编号",
        "source_chunk_id": "SRC-001",
        "query_type": "audit_basis"
      }
    ]
  }'
```

响应状态：

```text
HTTP/1.1 200
```

响应摘要：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total_queries": 1,
    "total_hits": 3
  },
  "error_code": null,
  "request_id": "原因排查-KB-SMOKE-001",
  "workflow_code": "policy_document_audit",
  "task_id": "原因排查-KB-SMOKE-001"
}
```

业务检索日志已成功落库：

```text
request_id = 原因排查-KB-SMOKE-001
status = success
result_count = 3
top_resource_ids = 20,18
```

结论：业务系统侧知识库接口、PostgreSQL 向量库、MySQL 检索日志均可用。

## 5. 隔离复现：直接创建工作流任务也未调用业务知识库

为排除业务系统创建工作流任务时未传 `knowledge_scope` 的可能性，已直接调用工作流系统创建任务接口，并显式传入 `knowledge_scope.knowledge_base_codes = ["default"]`。

请求：

```bash
curl -X POST http://127.0.0.1:8080/api/audit/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "workflow_code": "policy_document_audit",
    "biz_id": "DIRECT-KB-CHECK-20260510-2327",
    "input": {
      "file_id": "DIRECT-KB-CHECK-20260510-2327",
      "file_url": "http://127.0.0.1:6039/profile/upload/2026/05/10/2025520398FB（批注本安部分）(0725)_20260510231537A001.docx",
      "file_name": "2025520398FB（批注本安部分）(0725)_20260510231537A001.docx",
      "file_type": "docx",
      "metadata": {
        "business_type": "audit_review",
        "product_name": "本安-矿用本安型手机"
      },
      "knowledge_scope": {
        "knowledge_base_codes": ["default"],
        "effective_only": true
      },
      "caller_context": {
        "user_id": "codex",
        "permission_mode": "explicit_scope"
      }
    }
  }'
```

工作流返回创建成功：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 7,
    "taskNo": "AUDIT-20260510-000006",
    "taskStatus": "PENDING"
  }
}
```

查询任务：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/7
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 7,
    "taskNo": "AUDIT-20260510-000006",
    "workflowCode": "policy_document_audit",
    "bizId": "DIRECT-KB-CHECK-20260510-2327",
    "taskStatus": "SUCCESS",
    "currentNodeCode": "callback"
  }
}
```

但是业务系统检索日志中仍没有出现：

```text
DIRECT-KB-CHECK-20260510-2327
AUDIT-20260510-000006
taskId = 7
```

结论：即使直接显式传入 `knowledge_scope.default`，工作流当前执行路径仍未调用业务系统侧知识库检索接口。

## 6. 业务系统侧当前配置

业务系统当前配置已设置默认知识库编码：

```yaml
audit-workflow:
  enabled: true
  base-url: http://127.0.0.1:8080
  workflow-code: policy_document_audit
  public-file-base-url: http://127.0.0.1:6039
  knowledge-base-codes:
    - default
  permission-mode: explicit_scope
```

业务后端启动时间为 2026-05-10 23:14，测试任务 `AI-TASK-21` 创建和执行时间为 2026-05-10 23:15 至 23:16，因此当前任务执行时业务后端理论上已加载上述配置。

业务系统创建工作流任务时会传入：

```json
{
  "workflow_code": "policy_document_audit",
  "biz_id": "AI-TASK-{aiTaskId}",
  "input": {
    "file_url": "http://127.0.0.1:6039/profile/...",
    "file_name": "...",
    "file_type": "docx",
    "knowledge_scope": {
      "knowledge_base_codes": ["default"],
      "effective_only": true
    },
    "caller_context": {
      "permission_mode": "explicit_scope"
    }
  }
}
```

另外，隔离复现中已经直接绕过业务系统创建逻辑，手动把同样的 `knowledge_scope` 传给工作流，仍未触发业务知识库检索。

## 7. 初步判断

当前问题更可能在工作流系统侧，而不是业务系统知识库接口不可用。

可能原因包括：

1. 工作流当前版本没有在该执行路径中调用业务系统的 `/audit/library/vector/workflow-batch-search`。
2. 工作流内部存在条件分支，例如模型或解析节点先给出“未发现关键问题”后，跳过了知识库检索节点。
3. 工作流虽然统计了 `retrieval_total`，但该统计可能来自工作流内部检索或内部节点，并非调用业务系统知识库接口。
4. 工作流知识库服务地址配置未指向业务系统 `http://127.0.0.1:6039`。
5. 工作流调用的是其它知识库接口或其它端口，没有调用业务系统提供的正式接口。
6. 工作流没有把创建任务入参中的 `input.knowledge_scope` 传递到知识库检索节点。
7. 工作流在 `findings=[]` 或 `totalIssues=0` 场景下省略了依据检索，但业务需求要求无论是否发现问题，都必须先检索业务知识库。

## 8. 请求工作流系统侧确认的问题

请工作流系统侧确认以下问题：

1. 对于 `policy_document_audit` 工作流，是否保证每个真实文件审核任务都会调用业务系统知识库接口？
2. 对于只上传待审核文件、不上传依据文件的场景，工作流是否应该强制调用：

```text
POST http://127.0.0.1:6039/audit/library/vector/workflow-batch-search
```

3. 工作流任务 `taskId=6` / `AUDIT-20260510-000005` 是否有知识库检索节点执行记录？
4. 工作流任务 `taskId=7` / `AUDIT-20260510-000006` 是否有知识库检索节点执行记录？
5. 如果有检索节点执行记录，请提供该节点实际请求的 URL、request body 和响应摘要。
6. 如果没有检索节点执行记录，请确认为什么带有 `knowledge_scope.default` 的任务没有进入知识库检索节点。
7. 工作流统计接口中的 `retrieval_total` 统计口径是什么？它是否代表调用了业务系统知识库接口？
8. 工作流是否配置了业务知识库接口地址？当前值是否为：

```text
http://127.0.0.1:6039
```

9. 工作流是否使用了业务系统文档中约定的接口路径：

```text
/audit/library/vector/workflow-batch-search
/audit/library/vector/workflow-search
```

10. 工作流是否把任务入参中的 `knowledge_scope` 原样传递给检索请求？

## 9. 建议工作流侧调整

建议工作流侧按以下方式调整：

1. 对 `policy_document_audit` 文件审核主流程，将“业务知识库检索”设为必经节点。
2. 即使模型初步判断未发现问题，也应先完成知识库检索，再基于待审文件内容与检索依据生成审核结论。
3. 当 `input.knowledge_scope` 存在时，必须调用业务系统知识库接口。
4. 当 `input.knowledge_scope` 不存在或为空时，如该工作流被定义为必须使用业务知识库，应返回明确错误，而不是静默跳过：

```text
KB_SCOPE_REQUIRED
```

5. 工作流任务详情中建议暴露知识库检索节点信息，例如：

```json
{
  "currentNodeCode": "kb_retrieve",
  "retrievalRequestCount": 1,
  "retrievalHitCount": 3,
  "retrievalRequestId": "AUDIT-20260510-000005-KB-BATCH-001"
}
```

6. 工作流结果中建议保留依据引用，便于业务系统确认结论是否基于知识库：

```json
{
  "issues": [
    {
      "title": "...",
      "basis": [
        {
          "chunk_id": "KB-CHUNK-3-1",
          "resource_id": 20,
          "file_name": "...",
          "quote": "..."
        }
      ]
    }
  ]
}
```

7. 如果审核通过且无问题，也建议返回本次使用过的依据摘要，例如：

```json
{
  "success": true,
  "summary": "未发现关键问题",
  "basis_used": [
    {
      "chunk_id": "KB-CHUNK-3-1",
      "resource_id": 20,
      "file_name": "..."
    }
  ]
}
```

## 10. 业务系统侧结论

业务系统侧已确认：

1. 审核文件库向量库中存在可检索数据。
2. `/audit/library/vector/workflow-batch-search` 可正常返回命中结果。
3. 检索日志可正常落库。
4. 手工调用业务知识库接口可命中 `resource_id=20,18`。
5. 真实工作流任务 `AI-TASK-21` 没有触发业务系统知识库检索日志。
6. 直接创建带 `knowledge_scope.default` 的工作流任务 `taskId=7` 也没有触发业务系统知识库检索日志。

因此，请工作流系统侧重点排查：为什么 `policy_document_audit` 在文件审核流程中没有调用业务系统侧审核文件库向量知识库接口。

## 11. 工作流侧处理结果

处理时间：2026-05-10 晚间

### 11.1 根因确认

已确认根因在工作流系统侧配置与保护逻辑：

1. 工作流系统配置文件中 `audit.knowledge.base-url` 为空字符串。
2. 当 `base-url` 为空时，知识库客户端会直接返回空检索结果，用于早期本机无知识库服务时的烟测。
3. 因此 `policy_document_audit` 虽然执行了知识库检索节点，但没有向业务系统发起 HTTP 请求。
4. 阶段 3 历史种子数据中曾配置过 `knowledge_base_codes = ["project_policy", "compliance_policy"]`，存在覆盖业务系统传入 `["default"]` 的风险。

### 11.2 修复内容

已修复。

#### 11.2.1 配置业务知识库地址

已在工作流系统配置文件中配置业务系统知识库地址：

```yaml
audit:
  knowledge:
    base-url: http://127.0.0.1:6039
    batch-search-endpoint: /audit/library/vector/workflow-batch-search
    search-endpoint: /audit/library/vector/workflow-search
    required: true
```

涉及文件：

```text
src/main/resources/application.yml
```

#### 11.2.2 增加强制知识库保护

已新增保护逻辑：

1. 如果 `audit.knowledge.required = true` 且 `audit.knowledge.base-url` 为空，工作流不再静默返回零召回结果。
2. 这种情况下会返回明确错误：

```text
KB_SCOPE_REQUIRED: audit.knowledge.base-url is required
```

涉及文件：

```text
src/main/java/com/audit/workflow/retrieval/HttpKnowledgeRetrievalClient.java
```

#### 11.2.3 业务入参 knowledge_scope 优先

已调整知识库范围合并逻辑：

1. 工作流默认范围只作为兜底。
2. 如果业务系统创建任务时传入 `input.knowledge_scope.knowledge_base_codes`，工作流会优先使用业务系统传入值。
3. 阶段 6 默认知识库范围已改为：

```json
{
  "knowledge_base_codes": ["default"],
  "effective_only": true
}
```

涉及文件：

```text
src/main/java/com/audit/workflow/node/KnowledgeRetrieveNodeExecutor.java
sql/phase6_business_report_workflow.sql
```

#### 11.2.4 增强批量检索响应解析

已增强工作流对业务知识库批量检索响应的兼容性：

1. 支持按 `groups[].source_chunk_id` 解析。
2. 支持按 `results[].query_id` 反查原始查询。
3. 支持 `results/references/hits/items` 多种命中字段。
4. 批量检索请求中的每个 query 已补充：

```json
{
  "query_type": "audit_basis"
}
```

涉及文件：

```text
src/main/java/com/audit/workflow/retrieval/HttpKnowledgeRetrievalClient.java
```

### 11.3 已应用的数据库配置

已重新执行：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase6_business_report_workflow.sql
```

该脚本会将 `policy_document_audit` 的默认知识库范围设置为 `default`，并保持 AI 审核输出为业务系统要求的 `findings` JSON 结构。

### 11.4 验证结果

已执行编译验证：

```bash
mvn -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

### 11.5 需要业务系统侧重新验证

本次修复需要重启工作流服务后生效。

请业务系统侧重新触发一次审核任务，重点检查：

1. 业务系统 `audit_vector_search_log` 是否出现新的请求记录。
2. 请求标识中是否包含类似：

```text
AUDIT-20260510-xxxxxx-KB-BATCH-...
```

3. 工作流任务是否仍能正常完成。
4. 工作流结果是否继续返回：

```json
{
  "success": true,
  "summary": "...",
  "totalIssues": 0,
  "findings": []
}
```

### 11.6 当前不要求业务系统调整的事项

业务系统当前传入的以下内容符合工作流要求，不需要调整：

```json
{
  "knowledge_scope": {
    "knowledge_base_codes": ["default"],
    "effective_only": true
  },
  "caller_context": {
    "permission_mode": "explicit_scope"
  }
}
```

业务系统只需要确认知识库接口地址仍为：

```text
http://127.0.0.1:6039/audit/library/vector/workflow-batch-search
```

## 12. 业务系统侧二次复测结果

复测时间：2026-05-10 23:42 至 23:47

复测任务：

| 项目 | 值 |
| --- | --- |
| 产品名称 | 本安-矿用本安型手机 |
| 审核任务编号 | SF-1778427730863 |
| 业务审核任务 ID | 23 |
| 业务 AI 任务 ID | 22 |
| 工作流任务 ID | 8 |
| 工作流任务编号 | AUDIT-20260510-000007 |
| 工作流 bizId | AI-TASK-22 |

### 12.1 业务任务结果

业务系统 AI 任务状态：

```text
ai_task_id = 22
review_task_id = 23
task_no = SF-1778427730863
product_name = 本安-矿用本安型手机
task_status = completed
progress_percent = 100
progress_text = AI分析完成，未发现关键问题
ai_analysis_count = 1
report_file_url = /profile/upload/2026/05/10/2025520398FB（批注本安部分）(0725)_20260510234207A002.docx
ai_summary = "未发现关键问题"
```

工作流任务查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/8
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId": 8,
    "taskNo": "AUDIT-20260510-000007",
    "workflowCode": "policy_document_audit",
    "bizId": "AI-TASK-22",
    "taskStatus": "SUCCESS",
    "currentNodeCode": "callback",
    "summary": "{\"success\": true, \"summary\": \"未发现关键问题\", \"risk_level\": \"low\", \"totalIssues\": 0}",
    "errorCode": "",
    "retryCount": 0,
    "createTime": "2026-05-10T23:43:00",
    "startTime": "2026-05-10T23:43:02",
    "finishTime": "2026-05-10T23:43:05"
  }
}
```

工作流结果查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/8/result
```

返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "success": true,
    "summary": "未发现关键问题",
    "findings": [],
    "totalIssues": 0
  }
}
```

### 12.2 知识库调用情况

本次复测与上一次不同：工作流已经开始调用业务系统知识库接口。

业务系统检索日志新增两条记录：

```text
log_id = 5
request_id = AUDIT-20260510-000007-KB-BATCH-7175814046412
workflow_code = policy_document_audit
task_id = AUDIT-20260510-000007
result_count = 0
status = failed
error_code = KB_BAD_REQUEST
error_msg = queries[1].query 长度不能超过 1000 个字符
create_time = 2026-05-10 23:43:02
```

```text
log_id = 4
request_id = AUDIT-20260510-000007-KB-BATCH-7175758233379
workflow_code = policy_document_audit
task_id = AUDIT-20260510-000007
result_count = 0
status = failed
error_code = KB_BAD_REQUEST
error_msg = queries[1].query 长度不能超过 1000 个字符
create_time = 2026-05-10 23:43:02
```

结论：

1. 工作流侧配置 `audit.knowledge.base-url` 已生效。
2. 工作流已经调用了业务系统的 `/audit/library/vector/workflow-batch-search`。
3. 当前未能使用知识库完成有效检索，是因为工作流传入的 `queries[1].query` 超过业务系统原先限制的 1000 个字符。
4. 业务系统返回 `KB_BAD_REQUEST` 后，工作流仍将任务标记为 `SUCCESS` 并返回“未发现关键问题”。

### 12.3 业务系统侧已做兼容修正

业务系统侧已调整工作流知识库检索接口的 query 长度处理逻辑：

1. 工作流接口层允许最长 8000 字符 query。
2. 真正进入向量检索前，将 query 截取到 1000 字符，避免 embedding 和向量检索层接收过长文本。
3. 这样可以兼容工作流按待审文件分片原文发起检索的场景。

涉及文件：

```text
backend/ruoyi-system/src/main/java/com/ruoyi/system/service/audit/vector/impl/AuditWorkflowSearchServiceImpl.java
```

已执行编译验证：

```bash
mvn -pl ruoyi-admin -am compiler:compile -DskipTests \
  -Dmaven.compiler.outputDirectory=/tmp/audit-workflow-query-check \
  -Dmaven.compiler.useIncrementalCompilation=false
```

结果：

```text
BUILD SUCCESS
```

说明：由于本机 `target` 目录存在 root 权限文件，常规 `mvn compile` 写入 target 失败；本次使用 `/tmp` 输出目录完成了编译验证。

### 12.4 需要业务系统侧执行的操作

业务系统侧源码已修正，但当前运行中的业务后端尚未加载该修正。

需要重启业务系统后端后，再重新触发审核任务验证。

### 12.5 仍需工作流侧确认的问题

虽然业务系统侧已兼容长 query，但工作流侧仍建议确认以下行为：

1. 当 `audit.knowledge.required = true` 时，如果业务知识库返回 `KB_BAD_REQUEST`、`KB_UNAVAILABLE`、`KB_PERMISSION_DENIED` 等错误，工作流是否应该继续生成“未发现关键问题”。
2. 本次任务 `AUDIT-20260510-000007` 的知识库检索实际是失败的，但工作流任务状态仍为 `SUCCESS`，这可能导致业务系统误以为审核已基于知识库完成。
3. 建议工作流侧在 required=true 时，将知识库检索失败视为任务失败，或至少在结果中显式返回检索失败告警。
4. 建议工作流侧控制发送给知识库接口的单条 `query` 长度，例如先截取到 1000 至 2000 字符，避免把完整大段文件内容直接作为检索 query。

### 12.6 当前结论

当前问题已经从“工作流未调用业务知识库”推进为“工作流已调用业务知识库，但检索请求因 query 过长被业务系统拒绝”。

业务系统侧已完成兼容修正，等待重启后验证。

工作流侧仍建议补充 required=true 下的失败处理，避免知识库检索失败时仍返回成功审核结论。

## 13. 业务系统侧三次复测结果

复测时间：2026-05-10 23:55 至 23:56

复测任务：

| 项目 | 值 |
| --- | --- |
| 产品名称 | 本安-矿用本安型手机 |
| 审核任务编号 | SF-1778428550707 |
| 业务审核任务 ID | 24 |
| 业务 AI 任务 ID | 23 |
| 工作流任务 ID | 9 |
| 工作流任务编号 | AUDIT-20260510-000008 |
| 工作流 bizId | AI-TASK-23 |

### 13.1 工作流返回情况

工作流任务查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/9
```

返回摘要：

```json
{
  "taskId": 9,
  "taskNo": "AUDIT-20260510-000008",
  "workflowCode": "policy_document_audit",
  "bizId": "AI-TASK-23",
  "taskStatus": "SUCCESS",
  "currentNodeCode": "callback",
  "summary": "{\"success\": true, \"summary\": \"本次审核发现2个问题\", \"risk_level\": \"medium\", \"totalIssues\": 2}"
}
```

工作流结果查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/9/result
```

返回摘要：

```json
{
  "success": true,
  "summary": "本次审核发现2个问题",
  "totalIssues": 2,
  "findings": [
    {
      "type": "标准不符",
      "title": "委托编号格式错误",
      "severity": "medium"
    },
    {
      "type": "内容缺失",
      "title": "主要零(元)受控部件及重要原材料明细表信息不完整",
      "severity": "medium"
    }
  ]
}
```

结论：工作流本次不是“未发现问题”，而是返回了 2 个发现项。

### 13.2 知识库调用情况

业务系统检索日志新增两条成功记录：

```text
log_id = 7
request_id = AUDIT-20260510-000008-KB-BATCH-7955378451895
task_id = AUDIT-20260510-000008
query_count = 5
result_count = 20
top_resource_ids = 18,20
status = success
```

```text
log_id = 6
request_id = AUDIT-20260510-000008-KB-BATCH-7953916062632
task_id = AUDIT-20260510-000008
query_count = 10
result_count = 40
top_resource_ids = 20,18
status = success
```

结论：业务知识库已被工作流成功调用，并命中 `resource_id=18,20`。

### 13.3 业务系统展示异常原因

业务系统侧当前数据：

```text
ai_task_id = 23
review_task_id = 24
task_no = SF-1778428550707
task_status = completed
progress_text = AI分析完成，未发现关键问题
ai_summary = "本次审核发现2个问题"
audit_ai_finding = 空
```

根因在业务系统侧结果解析逻辑：

1. 工作流实际返回字段为 `findings`。
2. 业务系统 `AuditWorkflowAuditServiceImpl.fetchAndMapResult` 只读取 `data.issues`。
3. 因此业务系统把工作流返回的 2 个发现项解析成空列表。
4. 持久化逻辑看到发现项为空后，将 `progress_text` 更新为“AI分析完成，未发现关键问题”，并未写入 `audit_ai_finding`。
5. 摘要字段此前使用 JSON 序列化文本节点，导致 `ai_summary` 带有额外双引号。

### 13.4 业务系统侧修复内容

已修复业务系统解析逻辑：

1. 优先解析工作流返回的 `data.findings`。
2. 兼容历史结构 `data.issues`。
3. 兼容字段差异：
   - `type` / `finding_type`
   - `content` / `problem`
   - `severity` / `risk_level`
4. 文本型 `summary` 和 `location` 直接按文本保存，不再保存为带引号的 JSON 字符串。

涉及文件：

```text
backend/ruoyi-system/src/main/java/com/ruoyi/system/service/audit/impl/AuditWorkflowAuditServiceImpl.java
```

已执行编译验证：

```bash
mvn -pl ruoyi-admin -am compiler:compile -DskipTests \
  -Dmaven.compiler.outputDirectory=/tmp/audit-workflow-result-check \
  -Dmaven.compiler.useIncrementalCompilation=false
```

结果：

```text
BUILD SUCCESS
```

说明：本次问题不需要工作流系统侧处理。需要重启业务系统后端后重新触发审核任务，验证发现项是否正常落库并在页面展示。

## 14. 业务系统侧四次复测结果

复测时间：2026-05-11 00:05 至 00:06

复测任务：

| 项目 | 值 |
| --- | --- |
| 产品名称 | 本安-矿用本安型手机 |
| 审核任务编号 | SF-1778429105328 |
| 业务审核任务 ID | 25 |
| 业务 AI 任务 ID | 24 |
| 工作流任务 ID | 10 |
| 工作流任务编号 | AUDIT-20260511-000001 |
| 工作流 bizId | AI-TASK-24 |

### 14.1 工作流返回情况

工作流任务查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/10
```

返回摘要：

```json
{
  "taskId": 10,
  "taskNo": "AUDIT-20260511-000001",
  "workflowCode": "policy_document_audit",
  "bizId": "AI-TASK-24",
  "taskStatus": "SUCCESS",
  "currentNodeCode": "callback",
  "summary": "{\"success\": true, \"summary\": \"本次审核发现2个问题\", \"risk_level\": \"high\", \"totalIssues\": 2}"
}
```

工作流结果查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/10/result
```

返回摘要：

```json
{
  "success": true,
  "summary": "本次审核发现2个问题",
  "totalIssues": 2,
  "findings": [
    {
      "type": "标准不符",
      "title": "委托编号页眉不一致",
      "severity": "high"
    },
    {
      "type": "内容缺失",
      "title": "主要零(元)受控部件及重要原材料明细表缺少安标编号或认证编号",
      "severity": "medium"
    }
  ]
}
```

结论：工作流本次仍然返回了 2 个发现项，不是“未发现问题”。

### 14.2 知识库调用情况

业务系统检索日志新增两条成功记录：

```text
log_id = 9
request_id = AUDIT-20260511-000001-KB-BATCH-8557449790212
task_id = AUDIT-20260511-000001
query_count = 5
result_count = 20
top_resource_ids = 18,20
status = success
```

```text
log_id = 8
request_id = AUDIT-20260511-000001-KB-BATCH-8556063473717
task_id = AUDIT-20260511-000001
query_count = 10
result_count = 40
top_resource_ids = 20,18
status = success
```

结论：业务知识库已被工作流成功调用，并命中 `resource_id=18,20`。

### 14.3 本次仍显示“未发现问题”的原因

业务系统侧当前数据：

```text
ai_task_id = 24
review_task_id = 25
task_no = SF-1778429105328
task_status = completed
progress_text = AI分析完成，未发现关键问题
ai_summary = "本次审核发现2个问题"
audit_ai_finding = 空
```

根因仍在业务系统侧运行版本：

1. 源码已经在第 13 节修复 `findings` 解析问题。
2. 但本次任务仍写入 `ai_summary = "本次审核发现2个问题"`，说明当前运行中的业务后端仍是修复前旧逻辑。
3. 修复后的代码会将文本型 `summary` 直接保存为 `本次审核发现2个问题`，不会带额外双引号。
4. 因此本次复测没有验证到新代码，原因是业务后端未重启，或重启时没有加载最新多模块源码。

### 14.4 需要业务系统侧执行的操作

需要重新启动业务后端，并确保使用多模块源码启动，而不是加载旧 jar：

```bash
cd backend
mvn -pl ruoyi-admin -am spring-boot:run
```

注意：不要在 `backend/ruoyi-admin` 目录单独执行 `mvn spring-boot:run`。该方式可能加载本机 Maven 仓库中的旧 `ruoyi-system`、`ruoyi-framework` 等 jar，导致 `AuditWorkflowAuditServiceImpl` 的最新源码改动不生效。

重启后重新触发任务，预期结果：

```text
progress_text = AI分析完成，待人工审核
ai_summary = 本次审核发现2个问题
audit_ai_finding 至少写入 2 条记录
```

本次问题仍不需要工作流系统侧处理。

## 15. 工作流全文检索与最终审核依据截断问题

排查时间：2026-05-11 00:20 左右

排查对象：

| 项目 | 值 |
| --- | --- |
| 审核任务编号 | SF-1778429506737 |
| 业务 AI 任务 ID | 25 |
| 工作流任务 ID | 11 |
| 工作流任务编号 | AUDIT-20260511-000002 |
| 工作流 bizId | AI-TASK-25 |

### 15.1 已确认：工作流检索阶段覆盖了整份报告

工作流任务节点记录：

```text
file_parse:
  char_count = 14465
  block_count = 522

text_split:
  chunk_count = 15
  source_chunk_ids = 108..122

knowledge_retrieve:
  source_chunk_count = 15
  retrieval_count = 15
  reference_count = 60
```

工作流内容片段记录：

```text
chunk_no = 1..15
source_chunk_id = 108..122
total_chunk_chars = 16894
min_chars = 576
max_chars = 1200
```

说明：

1. `file_parse` 已解析整份报告文本，文本长度 `14465` 字符。
2. `text_split` 将报告切为 `15` 个片段。
3. `knowledge_retrieve` 对 `15` 个片段均发起了检索。
4. 每个 `source_chunk_id` 都有 1 条检索记录，每条返回 4 条依据。
5. 业务系统侧也记录到两批请求：

```text
query_count = 10, result_count = 40
query_count = 5, result_count = 20
```

结论：工作流不是只截取报告开头做检索，检索阶段已覆盖整份报告。

### 15.2 新问题：最终 AI 审核只使用了前 8 条知识库引用

工作流最终模型调用记录：

```text
audit_model_call_log.call_id = 38
prompt_chars = 22565
input_tokens = 16358
output_tokens = 559
call_status = SUCCESS
```

模型 prompt 中包含完整报告正文。证据：

```text
【待审阅报告】位置 = 914
【知识库搜索结果】位置 = 15390
主要零 位置 = 14149
第21页 位置 = 14805
№：201854833 位置 = 14651
```

说明模型 prompt 的“待审阅报告”部分包含了报告后段内容，例如：

```text
第 16 页 共 22 页
№：201854833 第 9 页 共 11 页
样品描述
主要零（元）部件（重要原材料）明细表见第21页
```

但模型 prompt 中的“知识库搜索结果”只包含 8 条引用：

```text
kb_item_count = 8
```

源码原因在工作流侧：

```java
for (RetrievalReference reference : references.stream().limit(maxReferenceCount).collect(Collectors.toList())) {
    ...
}
```

当前配置：

```yaml
audit:
  audit:
    max-reference-count: 8
    max-reference-chars: 8000
```

引用查询顺序：

```sql
ORDER BY source_chunk_id ASC, reference_id ASC
```

因此最终进入模型的 8 条依据来自：

```text
source_chunk_id = 108 的 4 条引用
source_chunk_id = 109 的 4 条引用
```

而 `source_chunk_id = 110..122` 的检索结果虽然已经落库，但没有进入最终 AI 审核 prompt。

### 15.3 当前影响

当前流程实际效果是：

1. 报告全文进入了模型 prompt。
2. 报告全文也被切片并逐片检索。
3. 但最终给模型看的知识库依据只取了按 `source_chunk_id` 排序后的前 8 条。
4. 后半报告片段对应的检索依据没有进入模型，模型无法基于这些依据充分审查后半报告。
5. 这会导致发现问题数量偏少，尤其是后半报告中的问题容易漏检。

因此，当前问题已经不是“工作流是否检索完整报告”，而是：

```text
工作流检索了完整报告，但最终 AI 审核阶段只使用了少量靠前片段的检索依据。
```

### 15.4 建议工作流侧调整

建议工作流侧调整最终审核阶段的依据组织方式：

1. 不要简单按 `source_chunk_id ASC, reference_id ASC` 截取前 `maxReferenceCount` 条。
2. 至少应保证每个待审报告片段都有代表性依据进入最终审核，例如：

```text
每个 source_chunk_id 取 top 1 或 top 2
再按分数、去重、字符预算做全局压缩
```

3. 可以按 `kb_chunk_id/resource_id` 去重，避免相同依据反复占用 prompt。
4. 可以将 `max-reference-count` 从 8 调大，或改为按 token/字符预算动态选择。
5. 对 `business_report_findings` 模式，建议改为分片审核后汇总：

```text
每个报告片段 + 该片段检索依据 -> 片段级 findings
所有片段 findings -> 去重合并 -> 最终 findings
```

6. 如果仍采用“整份报告一次性审核”，也应将依据覆盖情况写入节点输出，例如：

```json
{
  "source_chunk_count": 15,
  "retrieval_count": 15,
  "reference_count": 60,
  "references_used_in_prompt": 8,
  "covered_source_chunk_count": 2,
  "uncovered_source_chunk_ids": [110,111,112,113,114,115,116,117,118,119,120,121,122]
}
```

7. 建议在工作流结果中返回 `retrieval_used_summary`，方便业务系统判断本次审核是否充分覆盖报告全文。

### 15.5 需要工作流侧确认的问题

请工作流侧确认：

1. `business_report_findings` 模式下，最终模型是否只调用一次。
2. 当前是否有意将最终依据限制为 `max-reference-count = 8`。
3. 是否认可当前 8 条依据只覆盖前 2 个报告片段的判断。
4. 是否可以调整为“每个 source_chunk_id 至少选取若干依据”。
5. 是否可以在节点输出或结果接口中暴露：

```text
source_chunk_count
retrieval_count
reference_count
references_used_in_prompt
covered_source_chunk_count
uncovered_source_chunk_ids
```

6. 是否可以改为“分片审核 + 汇总去重”的模式，以降低全文一次性审核漏检风险。

## 16. 工作流侧针对第 15 节问题的处理结果

处理时间：2026-05-11 凌晨

### 16.1 结论确认

工作流侧确认第 15 节判断成立：

1. `business_report_findings` 模式下，最终 AI 审核目前是单次模型调用。
2. 此前确实受 `audit.audit.max-reference-count = 8` 限制。
3. 由于检索依据按 `source_chunk_id ASC, reference_id ASC` 查询，最终进入模型的 8 条依据主要覆盖靠前片段。
4. 这会导致“检索覆盖全文，但最终审核依据未覆盖全文”的问题。

### 16.2 工作流侧修复内容

已修复。

#### 16.2.1 调整最终 AI 审核的依据选择策略

`business_report_findings` 模式下，最终 AI 审核不再简单取前 `maxReferenceCount` 条依据。

新策略：

1. 按 `source_chunk_id` 对检索依据分组。
2. 采用轮询方式选取依据。
3. 第一轮优先从每个报告片段选择第 1 条依据，尽量保证每个片段都有代表性依据进入模型 prompt。
4. 在数量和字符预算允许的情况下，再继续从每个片段补充第 2 条、第 3 条依据。
5. 选择时仍受以下预算控制：

```yaml
audit:
  audit:
    max-reference-count: 30
    max-reference-chars: 16000
```

涉及文件：

```text
src/main/java/com/audit/workflow/node/AiAuditNodeExecutor.java
src/main/resources/application.yml
```

#### 16.2.2 增大依据预算

已将本机默认配置从：

```yaml
max-reference-count: 8
max-reference-chars: 8000
```

调整为：

```yaml
max-reference-count: 30
max-reference-chars: 16000
```

这能覆盖当前 15 个报告片段“每片段至少 1 条依据”的场景，并保留部分额外依据预算。

#### 16.2.3 输出依据覆盖统计

已新增 `retrieval_used_summary`，写入 AI 审核节点输出，并在最终结果中返回。

示例：

```json
{
  "retrieval_used_summary": {
    "source_chunk_count": 15,
    "retrieval_count": 15,
    "reference_count": 60,
    "references_used_in_prompt": 15,
    "covered_source_chunk_count": 15,
    "uncovered_source_chunk_ids": [],
    "max_reference_count": 30,
    "max_reference_chars": 16000
  }
}
```

业务系统可用该字段判断本次 AI 审核是否充分覆盖报告全文。

涉及文件：

```text
src/main/java/com/audit/workflow/node/AiAuditNodeExecutor.java
src/main/java/com/audit/workflow/node/ResultValidateNodeExecutor.java
```

### 16.3 对第 15.5 节问题的逐项答复

1. `business_report_findings` 模式下，当前最终模型是单次调用。
2. 此前是有意设置 `max-reference-count = 8` 作为早期 token 控制，但对长报告不够。
3. 认可第 15 节中“8 条依据只覆盖前 2 个报告片段”的判断。
4. 已调整为按 `source_chunk_id` 均衡选取依据，优先保证每个片段至少有代表性依据进入 prompt。
5. 已在节点输出和结果接口中暴露：

```text
source_chunk_count
retrieval_count
reference_count
references_used_in_prompt
covered_source_chunk_count
uncovered_source_chunk_ids
```

6. “分片审核 + 汇总去重”是更稳的长期方案，建议作为下一阶段优化。目前先保留单次全文审核，并通过均衡依据选择降低漏检风险。

### 16.4 验证结果

已执行：

```bash
mvn -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

### 16.5 需要继续联调确认

本次修复需要重启工作流服务后生效。

请业务系统侧重新触发一个新的审核任务，重点确认：

1. 工作流任务结果中是否包含 `retrieval_used_summary`。
2. `covered_source_chunk_count` 是否接近或等于 `source_chunk_count`。
3. `uncovered_source_chunk_ids` 是否为空或数量显著减少。
4. 模型最终 findings 是否比此前更充分。

如果后续仍出现长报告漏检，建议启动下一阶段方案：

```text
每个报告片段 + 该片段检索依据 -> 片段级 findings
所有片段 findings -> 汇总去重 -> 最终 findings
```

## 17. 工作流侧继续放大最终审核依据数量

处理时间：2026-05-11 凌晨

### 17.1 背景

业务侧再次复测后反馈：虽然第 16 节已经将最终审核依据从 8 条提升到按片段均衡选择，但模型仍只识别出 2 个问题。业务侧希望继续放大传递给最终 AI 审核的依据数量，最好将知识库检索得到的依据全部传递给大模型。

### 17.2 工作流侧调整

已调整。

#### 17.2.1 新增全部依据模式

新增配置：

```yaml
audit:
  audit:
    include-all-references: true
```

开启后，`business_report_findings` 模式下不再按 `max-reference-count` 截断依据数量，而是尽量将检索得到的全部依据传入最终 AI 审核 prompt。

#### 17.2.2 提高安全字符预算

当前配置调整为：

```yaml
audit:
  model:
    default-chat-model: qwen3.5-plus
    timeout-seconds: 300
  audit:
    include-all-references: true
    max-reference-count: 500
    max-reference-chars: 700000
```

说明：

1. `include-all-references: true` 开启后，`max-reference-count` 不再作为截断条件。
2. `max-reference-count: 500` 作为关闭全部依据模式时的兜底值。
3. 阿里云百炼官方模型列表显示 `qwen3.5-plus` 最大上下文长度为 `1,000,000 token`；中国内地北京地域下最大输入约为 `983,616 token`（思考模式）或 `991,808 token`（非思考模式）。
4. `max-reference-chars: 700000` 作为安全阈值，给待审报告全文、Prompt、模型思考与输出预留余量，同时足够容纳当前测试任务的全部知识库依据。
5. 以当前测试任务 `reference_count = 60` 的规模，预期 60 条依据都会进入最终 AI 审核 prompt。

涉及文件：

```text
src/main/resources/application.yml
src/main/java/com/audit/workflow/node/AiAuditNodeExecutor.java
```

### 17.3 结果覆盖统计

最终结果中的 `retrieval_used_summary` 会继续返回依据使用情况。

业务系统侧下次复测时重点看：

```json
{
  "retrieval_used_summary": {
    "reference_count": 60,
    "references_used_in_prompt": 60,
    "reference_truncated": false
  }
}
```

如果 `references_used_in_prompt < reference_count` 或 `reference_truncated = true`，说明仍因字符预算等原因发生了截断。

### 17.4 验证结果

已执行：

```bash
mvn -DskipTests compile
```

结果：

```text
BUILD SUCCESS
```

### 17.5 需要继续联调确认

本次修复需要重启工作流服务后生效。

请业务侧重新触发一个新的审核任务，重点确认：

1. `retrieval_used_summary.reference_count`。
2. `retrieval_used_summary.references_used_in_prompt`。
3. `retrieval_used_summary.reference_truncated`。
4. findings 数量是否比此前更充分。

如果全部依据进入 prompt 后仍然漏检，下一步建议切换为“分片审核 + 汇总去重”模式，而不是继续扩大单次全文审核 prompt。
