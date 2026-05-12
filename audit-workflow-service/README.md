# AI Audit Workflow Service

This repository contains the phase 1 implementation of the AI audit workflow service.

## Phase 1 Scope

- Spring Boot backend service.
- MySQL schema for workflow definitions, tasks, and node logs.
- Workflow and task query APIs.
- Task creation API.
- Scheduled task executor.
- Lightweight workflow state machine.
- `INPUT_VALIDATE` real node.
- Mock executors for later phase nodes.

## Run Prerequisites

Install JDK 17+ and Maven. A Java runtime alone is not enough because Maven needs `javac`.

Create a MySQL database and apply:

```bash
mysql -u root -p audit_workflow < sql/phase1_schema.sql
mysql -u root -p audit_workflow < sql/phase1_seed.sql
mysql -u root -p audit_workflow < sql/phase2_schema.sql
mysql -u root -p audit_workflow < sql/phase2_seed.sql
mysql -u root -p audit_workflow < sql/phase3_schema.sql
mysql -u root -p audit_workflow < sql/phase3_seed.sql
mysql -u root -p audit_workflow < sql/phase4_schema.sql
mysql -u root -p audit_workflow < sql/phase4_seed.sql
mysql -u root -p audit_workflow < sql/phase5_schema.sql
mysql -u root -p audit_workflow < sql/phase5_seed.sql
mysql -u root -p audit_workflow < sql/phase6_business_report_workflow.sql
```

Configuration is read directly from `src/main/resources/application.yml`. The local datasource defaults are:

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/audit_workflow?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: audit_workflow
    password: AuditWorkflow_2026@Mysql!
```

To call Aliyun Bailian Qwen, set the API key in `src/main/resources/application.yml`:

```yaml
audit:
  model:
    provider: aliyun-bailian
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    api-key: your-dashscope-api-key
    default-chat-model: qwen3.5-plus
```

The `policy_document_audit` workflow requires the business knowledge service. The local default is:

```yaml
audit:
  knowledge:
    base-url: http://127.0.0.1:6039
    batch-search-endpoint: /audit/library/vector/workflow-batch-search
    required: true
```

Start:

```bash
mvn spring-boot:run
```

## Smoke Test

```bash
curl http://127.0.0.1:8080/api/audit/workflows
```

```bash
curl -X POST http://127.0.0.1:8080/api/audit/tasks \
  -H 'Content-Type: application/json' \
  -d '{
    "workflow_code": "policy_document_audit",
    "biz_id": "PROJECT-20260510-001",
    "input": {
      "text": "phase1 smoke test"
    }
  }'
```

The phase 2 implementation supports `text` and `file_url` inputs. For `file_url`, supported first-version file types are `pdf`, `doc`, `docx`, `txt`, and `md`.

The phase 3 implementation calls the business knowledge service through HTTP. For required knowledge workflows, a blank `audit.knowledge.base-url` returns `KB_SCOPE_REQUIRED` instead of silently using zero references.

The phase 4 implementation calls an OpenAI-compatible chat endpoint when `audit.model.base-url` and `audit.model.api-key` are configured. If either is blank, it returns a local structured no-issue result and still records a model call log.

The phase 5 implementation adds final callbacks, callback retry, task retry, review feedback write-back, and a basic stats endpoint.

Useful endpoints:

```bash
curl -X POST http://127.0.0.1:8080/api/audit/tasks/{taskId}/retry
curl -X POST http://127.0.0.1:8080/api/audit/tasks/{taskId}/review -H 'Content-Type: application/json' -d '{"feedbacks":[]}'
curl -X POST http://127.0.0.1:8080/api/audit/callbacks/{taskId}/retry
curl http://127.0.0.1:8080/api/audit/stats/overview
```
