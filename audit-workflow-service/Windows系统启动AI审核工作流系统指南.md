# Windows 系统启动 AI 审核工作流系统指南

本文档用于在 Windows 系统从数据库创建开始启动 `audit-workflow-service`。数据库名、用户名、密码、端口等关键配置与《本机启动AI审核工作流系统指南.md》保持一致。

> 说明：本文档中的数据库密码仅用于本机开发环境，不建议用于生产环境。

## 1. 项目信息

Windows 项目目录：

```powershell
D:\TestEnvironment\PythonENV2\WorkflowforJava
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

## 2. 基础环境要求

建议提前安装：

```text
JDK 17 或更高版本
Maven 3.8 或更高版本
MySQL 8.x
```

在 PowerShell 中检查 Java：

```powershell
java -version
javac -version
```

检查 Maven：

```powershell
mvn -version
```

检查 MySQL 客户端是否可用：

```powershell
mysql --version
```

如果提示 `mysql`、`java` 或 `mvn` 不是可识别命令，需要先把对应安装目录加入 Windows `Path` 环境变量。

常见路径示例：

```text
JDK bin：C:\Program Files\Java\jdk-21\bin
Maven bin：D:\apache-maven-3.9.9\bin
MySQL bin：C:\Program Files\MySQL\MySQL Server 8.0\bin
```

## 3. 创建数据库和用户

以 MySQL 管理员账号进入 MySQL。打开 PowerShell，执行：

```powershell
mysql -u root -p
```

输入 root 密码后，创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS audit_workflow
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
```

确认数据库存在：

```sql
SHOW DATABASES;
```

创建本机开发用户并授权：

```sql
CREATE USER IF NOT EXISTS 'audit_workflow'@'127.0.0.1' IDENTIFIED BY 'AuditWorkflow_2026@Mysql!';
GRANT ALL PRIVILEGES ON audit_workflow.* TO 'audit_workflow'@'127.0.0.1';
FLUSH PRIVILEGES;
EXIT;
```

验证用户连接：

```powershell
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow
```

提示输入密码时输入：

```text
AuditWorkflow_2026@Mysql!
```

能进入 MySQL 后执行：

```sql
EXIT;
```

如果 Windows 本机连接时被解析为 `localhost` 用户，也可以额外创建 `localhost` 授权：

```sql
CREATE USER IF NOT EXISTS 'audit_workflow'@'localhost' IDENTIFIED BY 'AuditWorkflow_2026@Mysql!';
GRANT ALL PRIVILEGES ON audit_workflow.* TO 'audit_workflow'@'localhost';
FLUSH PRIVILEGES;
```

本项目配置使用 `127.0.0.1`，正常情况下只创建 `'audit_workflow'@'127.0.0.1'` 即可。

## 4. 初始化数据库表和种子数据

SQL 导入建议使用传统命令提示符 `cmd.exe` 执行，因为 PowerShell 对 `<` 输入重定向的兼容性依版本不同可能不一致。

打开 `cmd.exe`，进入项目根目录：

```bat
cd /d D:\TestEnvironment\PythonENV2\WorkflowforJava
```

如果使用 PowerShell 进入项目目录，命令是：

```powershell
Set-Location D:\TestEnvironment\PythonENV2\WorkflowforJava
```

在 `cmd.exe` 中依次执行全部阶段 SQL：

```bat
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase1_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase1_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase2_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase2_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase3_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase3_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase4_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase4_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase5_schema.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase5_seed.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase6_business_report_workflow.sql
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase7_uploaded_basis_workflow.sql
```

每条命令提示输入密码时，输入：

```text
AuditWorkflow_2026@Mysql!
```

如果 PowerShell 对 `<` 重定向兼容性异常，可以改用 `cmd` 执行同样命令：

```powershell
cmd /c "mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase1_schema.sql"
```

检查核心表：

```powershell
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SHOW TABLES;"
```

检查工作流数据：

```powershell
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT workflow_code, workflow_name, enabled FROM audit_workflow;"
```

应至少能看到：

```text
policy_document_audit
```

## 5. 检查配置文件

配置文件位置：

```text
src\main\resources\application.yml
```

数据库配置应与本文档一致：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/audit_workflow?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: audit_workflow
    password: AuditWorkflow_2026@Mysql!
```

当前工作流会访问业务知识库，配置项为：

```yaml
audit:
  knowledge:
    base-url: http://127.0.0.1:6039
    batch-search-endpoint: /audit/library/vector/workflow-batch-search
    search-endpoint: /audit/library/vector/workflow-search
    required: true
```

如果业务知识库服务没有启动，或 `http://127.0.0.1:6039` 不可访问，文件审核任务可能会在知识库检索阶段失败。

如果要连接其他业务系统知识库，修改：

```yaml
audit:
  knowledge:
    base-url: http://业务系统地址
    service-token: 可选服务token
```

模型配置在：

```yaml
audit:
  model:
    provider: aliyun-bailian
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    default-chat-model: qwen3.5-plus
```

如需真实调用阿里云百炼 Qwen，需要在 `audit.model.api-key` 中配置自己的 API Key。不要把生产密钥提交到公共仓库。

## 6. 编译项目

在项目根目录执行：

```powershell
Set-Location D:\TestEnvironment\PythonENV2\WorkflowforJava
mvn -DskipTests compile
```

成功时应看到：

```text
BUILD SUCCESS
```

## 7. 启动项目

前台启动：

```powershell
mvn spring-boot:run
```

看到类似以下日志表示启动成功：

```text
Tomcat started on port 8080
Started AuditWorkflowApplication
```

前台启动时不要关闭当前窗口。另开一个 PowerShell 窗口执行接口验证。

如果要把日志保存到文件，可以执行：

```powershell
mvn spring-boot:run *> audit-workflow.log
```

查看日志：

```powershell
Get-Content .\audit-workflow.log -Wait
```

## 8. 接口验证

### 8.1 查询工作流

```powershell
curl.exe http://127.0.0.1:8080/api/audit/workflows
```

期望返回中包含：

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

PowerShell 推荐使用 `curl.exe`，避免调用到 PowerShell 自带的 `Invoke-WebRequest` 别名：

```powershell
curl.exe -X POST http://127.0.0.1:8080/api/audit/tasks `
  -H "Content-Type: application/json" `
  -d "{\"workflow_code\":\"policy_document_audit\",\"biz_id\":\"PROJECT-TEST-001\",\"input\":{\"text\":\"这是一个测试待审核文件内容。请检查是否符合项目审批要求。\",\"file_name\":\"测试文件.txt\",\"file_type\":\"txt\"}}"
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

记下返回中的 `taskId`。

### 8.3 查询任务状态

等待几秒钟后查询，把 `1` 替换成实际 `taskId`：

```powershell
curl.exe http://127.0.0.1:8080/api/audit/tasks/1
```

如果 `taskStatus` 是 `SUCCESS`，说明工作流执行完成。

如果是 `FAILED`，查看返回中的 `errorCode` 和 `errorMsg`。

### 8.4 查询审核结果

```powershell
curl.exe http://127.0.0.1:8080/api/audit/tasks/1/result
```

### 8.5 查询统计

```powershell
curl.exe http://127.0.0.1:8080/api/audit/stats/overview
```

## 9. Windows 常用运维命令

查看 8080 端口占用：

```powershell
netstat -ano | findstr :8080
```

根据 PID 查看进程：

```powershell
tasklist /FI "PID eq <pid>"
```

停止进程：

```powershell
taskkill /PID <pid> /F
```

查看 MySQL 服务：

```powershell
Get-Service *mysql*
```

启动 MySQL 服务，服务名以本机实际显示为准：

```powershell
Start-Service MySQL80
```

停止 MySQL 服务：

```powershell
Stop-Service MySQL80
```

## 10. 常见问题

### 10.1 curl 连接 8080 失败

现象：

```text
Failed to connect to 127.0.0.1 port 8080
```

原因：服务没有启动，或启动失败。

处理：

```powershell
mvn spring-boot:run
```

查看启动日志中的 `ERROR` 或 `Caused by`。

### 10.2 Maven 编译失败：release version 17 not supported

原因：当前 Java 版本低于项目要求，或只安装了 JRE 没有安装 JDK。

检查：

```powershell
javac -version
```

处理：安装 JDK 17 或更高版本，并确认 `JAVA_HOME` 和 `Path` 指向 JDK。

### 10.3 MySQL 密码策略不通过

现象：

```text
ERROR 1819 (HY000): Your password does not satisfy the current policy requirements
```

处理：使用本文档中的强密码：

```text
AuditWorkflow_2026@Mysql!
```

### 10.4 SQL 导入时报 `<` 相关错误

PowerShell 某些环境下对重定向表现不一致。可以改用 `cmd /c`：

```powershell
cmd /c "mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase1_schema.sql"
```

也可以进入传统命令提示符 `cmd.exe` 后，在项目根目录执行 SQL 初始化命令。

### 10.5 任务一直是 PENDING

可能原因：

1. 服务没有正常启动。
2. 后台调度器没有执行。
3. 数据库连接异常。

查询任务表：

```powershell
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT task_id, task_status, current_node_code, error_code, error_msg FROM audit_task ORDER BY task_id DESC LIMIT 5;"
```

### 10.6 任务 FAILED

查询任务详情：

```powershell
curl.exe http://127.0.0.1:8080/api/audit/tasks/<taskId>
```

查看节点日志：

```powershell
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT node_code, node_type, node_status, error_code, error_msg FROM audit_task_node_log WHERE task_id = <taskId> ORDER BY log_id;"
```

如果失败节点与知识库检索有关，优先检查 `audit.knowledge.base-url` 指向的业务知识库服务是否已启动。

### 10.7 导入 seed 时报 Data too long for column 'workflow_name'

现象：

```text
ERROR 1406 (22001) at line 1: Data too long for column 'workflow_name' at row 1
```

原因：当前数据库中已经存在旧版 `audit_workflow` 表，`workflow_name` 字段长度小于当前脚本要求。`phase1_schema.sql` 使用 `CREATE TABLE IF NOT EXISTS`，如果旧表已经存在，不会自动修改字段长度。

如果是从 0 初始化本机开发数据库，推荐直接重建数据库：

```sql
DROP DATABASE IF EXISTS audit_workflow;
CREATE DATABASE audit_workflow
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;
GRANT ALL PRIVILEGES ON audit_workflow.* TO 'audit_workflow'@'127.0.0.1';
FLUSH PRIVILEGES;
```

然后重新执行第 4 节全部 SQL 初始化脚本。

如果要保留已有数据，可以只修正字段长度：

```sql
ALTER TABLE audit_workflow
  MODIFY workflow_name VARCHAR(128) NOT NULL COMMENT '工作流名称';
```

修正后重新执行失败的 seed 脚本：

```bat
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql\phase1_seed.sql
```

## 11. 日常最小启动命令汇总

如果数据库、用户和 SQL 已经初始化过，日常启动只需要：

```powershell
Set-Location D:\TestEnvironment\PythonENV2\WorkflowforJava
mvn spring-boot:run
```

启动后访问：

```text
http://127.0.0.1:8080
```
