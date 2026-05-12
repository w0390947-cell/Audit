# 本机启动 AI 审核工作流系统指南

本文档用于在当前本机从 0 启动 `audit-workflow-service`。内容包含 JDK/Maven 检查、MySQL 建库建用户、SQL 初始化、配置文件调整、项目启动和接口验证。

> 说明：本文档中的数据库密码仅用于本机开发环境，不建议用于生产环境。

## 1. 项目信息

项目根目录：

```bash
/home/anjou/PythonENV/Test_2
```

服务默认端口：

```text
8080
```

本机 MySQL 数据库：

```text
数据库名：audit_workflow
用户名：audit_workflow
密码：AuditWorkflow_2026@Mysql!
连接地址：127.0.0.1:3306
```

## 2. 检查基础环境

进入项目目录：

```bash
cd /home/anjou/PythonENV/Test_2
```

检查 Java：

```bash
java -version
javac -version
```

本机当前已验证版本：

```text
openjdk 21.0.10
javac 21.0.10
```

检查 Maven：

```bash
mvn -version
```

本机当前已验证版本：

```text
Apache Maven 3.8.7
Java version: 21.0.10
```

如果 `javac` 不存在，需要安装 JDK：

```bash
sudo apt-get update
sudo apt-get install -y openjdk-21-jdk
```

## 3. 创建数据库

进入 MySQL：

```bash
sudo mysql
```

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS audit_workflow
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

确认数据库存在：

```sql
SHOW DATABASES;
```

创建本机开发用户：

```sql
CREATE USER IF NOT EXISTS 'audit_workflow'@'127.0.0.1' IDENTIFIED BY 'AuditWorkflow_2026@Mysql!';
GRANT ALL PRIVILEGES ON audit_workflow.* TO 'audit_workflow'@'127.0.0.1';
FLUSH PRIVILEGES;
EXIT;
```

验证用户连接：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow
```

输入密码：

```text
AuditWorkflow_2026@Mysql!
```

能进入 MySQL monitor 后执行：

```sql
EXIT;
```

## 4. 初始化数据库表和种子数据

在项目根目录执行：

```bash
cd /home/anjou/PythonENV/Test_2
```

依次执行全部阶段 SQL：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase1_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase1_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase2_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase2_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase3_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase3_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase4_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase4_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase5_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase5_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase6_business_report_workflow.sql
```

每条命令提示输入密码时，输入：

```text
AuditWorkflow_2026@Mysql!
```

检查核心表：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SHOW TABLES;"
```

检查示例工作流：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT workflow_code, workflow_name, enabled FROM audit_workflow;"
```

应看到：

```text
policy_document_audit
```

## 5. 检查配置文件

本项目当前使用配置文件直接配置，不需要在启动前执行 `export`。配置文件位置：

```bash
src/main/resources/application.yml
```

本机数据库配置已经写入：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/audit_workflow?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: audit_workflow
    password: AuditWorkflow_2026@Mysql!
```

当前 `policy_document_audit` 是必须接业务知识库的工作流，`audit.knowledge.base-url` 已配置为本机业务系统地址：

```yaml
audit:
  knowledge:
    base-url: http://127.0.0.1:6039
    batch-search-endpoint: /audit/library/vector/workflow-batch-search
    search-endpoint: /audit/library/vector/workflow-search
    required: true
```

如果业务系统没有启动，或该地址不可访问，文件审核任务会在知识库检索阶段失败。

如果要接业务系统知识库，修改：

```yaml
audit:
  knowledge:
    base-url: http://业务系统地址
    service-token: 可选服务token
```

阿里云百炼 Qwen 模型配置在：

```yaml
audit:
  model:
    provider: aliyun-bailian
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    api-key: ""
    default-chat-model: qwen3.5-plus
    timeout-seconds: 600
  audit:
    include-all-references: true
    max-reference-count: 500
    max-reference-chars: 700000
```

要真实调用 Qwen，需要把 `api-key` 改成你的百炼 API Key：

```yaml
audit:
  model:
    api-key: sk-xxxxxxxx
```

如果 `audit.model.api-key` 为空，系统不会真实调用 Qwen，会返回本地结构化“无问题”结果，并记录模型调用日志。

可选回调 token：

```yaml
audit:
  callback:
    token: optional-callback-token
```

> 说明：百炼 Qwen 使用 OpenAI 兼容接口时，北京地域的 `base-url` 是 `https://dashscope.aliyuncs.com/compatible-mode/v1`，本系统代码会请求 `${base-url}/chat/completions`。


## 6. 编译项目

```bash
cd /home/anjou/PythonENV/Test_2
mvn -DskipTests compile
```

成功时应看到：

```text
BUILD SUCCESS
```

## 7. 启动项目

前台启动：

```bash
mvn spring-boot:run
```

看到类似以下日志表示启动成功：

```text
Tomcat started on port 8080
Started AuditWorkflowApplication
```

前台启动时不要关闭当前终端。另开一个终端执行接口验证。

如果要后台启动：

```bash
nohup mvn spring-boot:run > audit-workflow.log 2>&1 &
```

查看日志：

```bash
tail -f audit-workflow.log
```

## 8. 接口验证

### 8.1 查询工作流

```bash
curl http://127.0.0.1:8080/api/audit/workflows
```

期望返回：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "workflowCode": "policy_document_audit"
    }
  ]
}
```

### 8.2 创建测试审核任务

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

返回示例：

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

记下 `taskId`。

### 8.3 查询任务状态

等待几秒钟后查询：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/1
```

如果 `taskStatus` 是 `SUCCESS`，说明工作流执行完成。

如果是 `FAILED`，查看返回中的 `errorCode` 和 `errorMsg`。

### 8.4 查询审核结果

```bash
curl http://127.0.0.1:8080/api/audit/tasks/1/result
```

未配置真实知识库和模型时，结果通常是本地结构化“无问题”结果。

### 8.5 查询统计

```bash
curl http://127.0.0.1:8080/api/audit/stats/overview
```

## 9. 常用运维命令

查看 8080 端口占用：

```bash
ss -lntp | grep 8080
```

后台启动后查看日志：

```bash
tail -f audit-workflow.log
```

停止后台启动的服务：

```bash
ps -ef | grep audit-workflow-service
```

找到进程号后：

```bash
kill <pid>
```

## 10. 常见问题

### 10.1 curl 连接 8080 失败

现象：

```text
curl: (7) Failed to connect to 127.0.0.1 port 8080
```

原因：服务没有启动，或者启动失败。

处理：

```bash
mvn spring-boot:run
```

查看启动日志中的 `ERROR` 或 `Caused by`。

### 10.2 Maven 编译失败 release version 17 not supported

原因：只有 Java Runtime，没有 JDK 编译器。

检查：

```bash
javac -version
```

处理：

```bash
sudo apt-get install -y openjdk-21-jdk
```

### 10.3 MySQL 密码策略不通过

现象：

```text
ERROR 1819 (HY000): Your password does not satisfy the current policy requirements
```

处理：使用强密码，例如本文档中的：

```text
AuditWorkflow_2026@Mysql!
```

### 10.4 任务一直是 PENDING

可能原因：

1. 服务没有启动。
2. 后台调度器没有执行。
3. 数据库连接异常。

处理：

1. 查看服务日志。
2. 查询任务表：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT task_id, task_status, current_node_code, error_code, error_msg FROM audit_task ORDER BY task_id DESC LIMIT 5;"
```

### 10.5 任务 FAILED

查询任务详情：

```bash
curl http://127.0.0.1:8080/api/audit/tasks/<taskId>
```

查看节点日志：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT node_code, node_type, node_status, error_code, error_msg FROM audit_task_node_log WHERE task_id = <taskId> ORDER BY log_id;"
```

## 11. 本机最小启动命令汇总

如果数据库和用户已经创建过，日常启动只需要：

```bash
cd /home/anjou/PythonENV/Test_2
export AUDIT_WORKFLOW_DB_URL='jdbc:mysql://127.0.0.1:3306/audit_workflow?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
export AUDIT_WORKFLOW_DB_USERNAME='audit_workflow'
export AUDIT_WORKFLOW_DB_PASSWORD='AuditWorkflow_2026@Mysql!'
mvn spring-boot:run
```
