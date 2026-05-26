# Ubuntu 开发环境部署指南

本文档用于在一台全新的 Ubuntu 系统中，从 0 部署并启动当前项目。目标是开发/测试环境，不包含生产环境的 Nginx、HTTPS、systemd 守护、日志轮转和安全加固。

## 1. 项目组成

本项目由 4 个主要服务组成：

| 模块 | 目录 | 技术栈 | 默认端口 | 作用 |
| --- | --- | --- | --- | --- |
| 业务后端 | `backend` | Spring Boot 3 / RuoYi / MySQL / Redis / PostgreSQL pgvector | `6039` | 登录、权限、审核业务、文件库、向量检索、AI 工作流回调接收 |
| 前端 | `ruoyi-ui` | Vue 2 / Vue CLI / Element UI | `5666` | 管理后台页面 |
| AI 审核工作流服务 | `audit-workflow-service` | Spring Boot 3 / MySQL / PostgreSQL pgvector | `8080` | 独立工作流编排、文件解析、检索、模型审核、回调 |
| OCR 服务 | `ocr-service` | Python / FastAPI / PaddleOCR 或阿里云 OCR | `8866` | 扫描件 PDF 或图片 OCR |

服务调用关系：

```text
浏览器 -> ruoyi-ui:5666 -> backend:6039
backend:6039 -> audit-workflow-service:8080
audit-workflow-service:8080 -> backend:6039 的知识库检索接口
audit-workflow-service:8080 -> backend:6039 的回调接口
backend:6039 -> ocr-service:8866
backend:6039 和 audit-workflow-service:8080 -> PostgreSQL/pgvector:5432
backend:6039 -> Redis:16379
backend:6039 -> MySQL ry-vue
audit-workflow-service:8080 -> MySQL audit_workflow
```

## 2. 约定路径和账号

下面统一假设项目放在：

```bash
/opt/audit
```

如果你使用其他目录，请把命令中的 `/opt/audit` 替换为实际路径。

开发环境默认数据库和账号：

| 用途 | 类型 | 数据库 | 用户名 | 密码 |
| --- | --- | --- | --- | --- |
| 业务系统 | MySQL | `ry-vue` | `ruoyi` | `301836` |
| 工作流系统 | MySQL | `audit_workflow` | `audit_workflow` | `AuditWorkflow_2026@Mysql!` |
| 向量库 | PostgreSQL | `audit_vector` | `audit_vector_user` | `301836` |

> 说明：以上密码来自当前项目配置，只适合本机开发环境。生产环境不要使用这些密码。

## 3. 安装基础依赖

### 3.1 更新系统并安装通用工具

```bash
sudo apt update
sudo apt install -y curl wget git unzip build-essential ca-certificates lsb-release
```

### 3.2 安装 JDK 和 Maven

两个 Java 服务都要求 JDK 17 或更高版本。Ubuntu 仓库中 JDK 17 最稳定，JDK 21 也可以。

```bash
sudo apt install -y openjdk-17-jdk maven

java -version
javac -version
mvn -version
```

确认 `java -version` 至少是 17。

### 3.3 安装 Node.js

前端是 Vue 2 + Vue CLI 4，建议使用 Node.js 16 LTS。Node 18/20 在老 Webpack 项目中可能遇到 OpenSSL 兼容问题。

推荐用 `nvm` 安装：

```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.7/install.sh | bash
source ~/.bashrc

nvm install 16
nvm use 16
nvm alias default 16

node -v
npm -v
```

### 3.4 安装 Python

```bash
sudo apt install -y python3 python3-venv python3-pip

python3 --version
pip3 --version
```

### 3.5 安装 MySQL

```bash
sudo apt install -y mysql-server mysql-client
sudo systemctl enable --now mysql
sudo systemctl status mysql
```

能看到 `active (running)` 即可。

### 3.6 安装 Redis

业务后端配置的 Redis 端口是 `16379`，不是默认 `6379`。

```bash
sudo apt install -y redis-server
redis-server --port 16379 --daemonize yes
redis-cli -p 16379 ping
```

返回：

```text
PONG
```

如果你希望 Redis 随系统启动，需要把 `/etc/redis/redis.conf` 中的 `port` 改为 `16379`，然后执行：

```bash
sudo systemctl restart redis-server
redis-cli -p 16379 ping
```

### 3.7 安装 PostgreSQL 和 pgvector

业务后端和工作流服务都使用 PostgreSQL `audit_vector` 数据库做向量检索，且 SQL 中需要 `vector` 和 `pg_trgm` 扩展。

先安装 PostgreSQL：

```bash
sudo apt install -y postgresql postgresql-contrib
sudo systemctl enable --now postgresql
sudo systemctl status postgresql
```

安装 pgvector。不同 Ubuntu/PostgreSQL 版本包名可能不同，先搜索：

```bash
apt-cache search pgvector
```

如果能看到类似 `postgresql-16-pgvector`、`postgresql-15-pgvector`、`postgresql-14-pgvector` 的包，安装对应版本：

```bash
sudo apt install -y postgresql-16-pgvector
```

如果系统仓库没有 pgvector 包，可以源码安装：

```bash
sudo apt install -y git build-essential postgresql-server-dev-all
cd /tmp
git clone https://github.com/pgvector/pgvector.git
cd pgvector
make
sudo make install
```

验证 PostgreSQL 可用：

```bash
sudo -u postgres psql -c "SELECT version();"
```

## 4. 准备项目目录

如果你是从 Git 仓库拉取项目：

```bash
sudo mkdir -p /opt/audit
sudo chown -R "$USER":"$USER" /opt/audit
cd /opt/audit
git clone <你的项目仓库地址> .
```

如果你是从已有机器拷贝项目，把完整项目目录拷贝到 `/opt/audit` 后执行：

```bash
cd /opt/audit
```

确认目录结构：

```bash
ls
```

应至少看到：

```text
backend
ruoyi-ui
audit-workflow-service
ocr-service
```

## 5. 初始化 MySQL

### 5.1 创建业务系统数据库

进入 MySQL：

```bash
sudo mysql
```

执行：

```sql
CREATE DATABASE IF NOT EXISTS `ry-vue`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'ruoyi'@'127.0.0.1' IDENTIFIED BY '301836';
CREATE USER IF NOT EXISTS 'ruoyi'@'localhost' IDENTIFIED BY '301836';
GRANT ALL PRIVILEGES ON `ry-vue`.* TO 'ruoyi'@'127.0.0.1';
GRANT ALL PRIVILEGES ON `ry-vue`.* TO 'ruoyi'@'localhost';

CREATE DATABASE IF NOT EXISTS audit_workflow
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'audit_workflow'@'127.0.0.1' IDENTIFIED BY 'AuditWorkflow_2026@Mysql!';
CREATE USER IF NOT EXISTS 'audit_workflow'@'localhost' IDENTIFIED BY 'AuditWorkflow_2026@Mysql!';
GRANT ALL PRIVILEGES ON audit_workflow.* TO 'audit_workflow'@'127.0.0.1';
GRANT ALL PRIVILEGES ON audit_workflow.* TO 'audit_workflow'@'localhost';

FLUSH PRIVILEGES;
EXIT;
```

验证连接：

```bash
mysql -h 127.0.0.1 -u ruoyi -p ry-vue
```

输入密码 `301836`，能进入后执行：

```sql
EXIT;
```

再验证工作流数据库：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow
```

输入密码 `AuditWorkflow_2026@Mysql!`，能进入后执行：

```sql
EXIT;
```

### 5.2 导入业务系统 MySQL 脚本

进入业务后端目录：

```bash
cd /opt/audit/backend
```

全新部署时，直接执行总初始化脚本：

```bash
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/init_all.sql
```

提示密码时输入：

```text
301836
```

`sql/init_all.sql` 内部会按顺序执行：

```sql
SOURCE sql/ry_20260320.sql;
SOURCE sql/quartz.sql;
SOURCE sql/audit_review_init.sql;
SOURCE sql/audit_asset_library_init.sql;
SOURCE sql/audit_ai_init.sql;
SOURCE sql/audit_ai_flow_stage_migration.sql;
SOURCE sql/audit_ai_queue_position_rebuild.sql;
SOURCE sql/auditor_role_permissions.sql;
SOURCE sql/system_admin_role_permissions.sql;
SOURCE sql/super_admin_full_permissions.sql;
```

> 说明：`backend/sql/init_all.sql` 只用于全新空库初始化。`backend/sql` 中还有若干 `*_migration.sql`，多数用于旧库升级。当前全新部署时，`audit_review_init.sql`、`audit_asset_library_init.sql`、`audit_ai_init.sql` 已经包含了部分迁移后的字段和字典数据；新库不要把所有迁移脚本无脑重复导入，否则可能遇到 `Duplicate column` 或 `Duplicate key name`。

如果总脚本导入失败，需要定位具体阶段，可按下面的排错明细逐条执行：

```bash
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/ry_20260320.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/quartz.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/audit_review_init.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/audit_asset_library_init.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/audit_ai_init.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/audit_ai_flow_stage_migration.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/audit_ai_queue_position_rebuild.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/auditor_role_permissions.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/system_admin_role_permissions.sql
mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/super_admin_full_permissions.sql
```

检查业务库：

```bash
mysql -h 127.0.0.1 -u ruoyi -p ry-vue -e "SHOW TABLES;"
mysql -h 127.0.0.1 -u ruoyi -p ry-vue -e "SELECT user_name, nick_name FROM sys_user;"
```

默认登录账号：

```text
admin / admin123
ry / admin123
```

### 5.3 导入工作流 MySQL 脚本

```bash
cd /opt/audit/audit-workflow-service
```

全新部署时，直接执行总初始化脚本：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/init_all.sql
```

提示密码时输入：

```text
AuditWorkflow_2026@Mysql!
```

`sql/init_all.sql` 内部会按顺序执行：

```sql
SOURCE sql/phase1_schema.sql;
SOURCE sql/phase1_seed.sql;
SOURCE sql/phase2_schema.sql;
SOURCE sql/phase2_seed.sql;
SOURCE sql/phase3_schema.sql;
SOURCE sql/phase3_seed.sql;
SOURCE sql/phase4_schema.sql;
SOURCE sql/phase4_seed.sql;
SOURCE sql/phase5_schema.sql;
SOURCE sql/phase5_seed.sql;
SOURCE sql/phase6_business_report_workflow.sql;
SOURCE sql/phase7_uploaded_basis_workflow.sql;
```

如果总脚本导入失败，需要定位具体阶段，可按下面的排错明细逐条执行：

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
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow < sql/phase7_uploaded_basis_workflow.sql
```

检查工作流数据：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SHOW TABLES;"
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT workflow_code, workflow_name, enabled FROM audit_workflow;"
```

应至少看到：

```text
policy_document_audit
uploaded_basis_document_audit
```

## 6. 初始化 PostgreSQL 向量库

### 6.1 创建数据库和用户

```bash
sudo -u postgres psql
```

执行：

```sql
CREATE USER audit_vector_user WITH PASSWORD '301836';
CREATE DATABASE audit_vector OWNER audit_vector_user;
\c audit_vector
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
GRANT ALL PRIVILEGES ON DATABASE audit_vector TO audit_vector_user;
\q
```

验证连接：

```bash
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -c "SELECT current_database();"
```

### 6.2 导入向量库结构

```bash
cd /opt/audit/backend
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -f sql/audit_vector_init.sql
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -f sql/audit_vector_hybrid_search_migration.sql
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -f sql/audit_vector_workflow_metadata_migration.sql
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -f sql/audit_vector_temp_basis_namespace_migration.sql
```

检查扩展和表：

```bash
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -c "\dx"
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -c "\dt"
```

应看到 `vector`、`pg_trgm` 扩展，以及 `audit_vector_document`、`audit_vector_chunk` 等表。

## 7. 配置项目

### 7.1 业务后端配置

配置文件：

```text
/opt/audit/backend/ruoyi-admin/src/main/resources/application.yml
/opt/audit/backend/ruoyi-admin/src/main/resources/application-druid.yml
```

当前项目默认配置已经指向：

```yaml
server:
  port: 6039

spring:
  data:
    redis:
      host: 127.0.0.1
      port: 16379

audit-workflow:
  enabled: true
  base-url: http://127.0.0.1:8080
  callback-url: http://127.0.0.1:6039/audit/ai/workflow/callback
  public-file-base-url: http://127.0.0.1:6039

vector:
  enabled: true
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/audit_vector
    username: audit_vector_user
    password: 301836
  ocr:
    endpoint: http://127.0.0.1:8866/ocr
```

需要特别修改 `ruoyi.profile`，否则新 Ubuntu 会继续使用当前文件中的 macOS 路径：

```yaml
ruoyi:
  profile: /opt/audit/.runtime/uploadPath
```

创建目录：

```bash
mkdir -p /opt/audit/.runtime/uploadPath
```

MySQL 配置在 `application-druid.yml`：

```yaml
spring:
  datasource:
    druid:
      master:
        url: jdbc:mysql://127.0.0.1:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia%2FShanghai
        username: ruoyi
        password: 301836
```

### 7.2 工作流服务配置

配置文件：

```text
/opt/audit/audit-workflow-service/src/main/resources/application.yml
```

当前默认配置：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/audit_workflow?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: audit_workflow
    password: AuditWorkflow_2026@Mysql!

audit:
  knowledge:
    base-url: http://127.0.0.1:6039
    search-endpoint: /audit/library/vector/workflow-search
    batch-search-endpoint: /audit/library/vector/workflow-batch-search
    required: true
  temp-vector:
    datasource:
      url: ${AUDIT_VECTOR_JDBC_URL:jdbc:postgresql://127.0.0.1:5432/audit_vector}
      username: ${AUDIT_VECTOR_JDBC_USERNAME:audit_vector_user}
      password: ${AUDIT_VECTOR_JDBC_PASSWORD:301836}
```

如需使用环境变量覆盖向量库连接：

```bash
export AUDIT_VECTOR_JDBC_URL='jdbc:postgresql://127.0.0.1:5432/audit_vector'
export AUDIT_VECTOR_JDBC_USERNAME='audit_vector_user'
export AUDIT_VECTOR_JDBC_PASSWORD='301836'
```

### 7.3 模型和密钥配置

当前配置中存在示例 API Key。全新环境建议不要依赖仓库里的示例密钥，而是使用自己的阿里云百炼 DashScope Key：

```bash
export DASHSCOPE_API_KEY='sk-你的真实key'
export AUDIT_EMBEDDING_API_KEY="$DASHSCOPE_API_KEY"
```

业务后端的 `vector.chat.api-key` 已支持 `${DASHSCOPE_API_KEY:}`。工作流服务的临时向量检索也支持 `AUDIT_EMBEDDING_API_KEY` 或 `DASHSCOPE_API_KEY`。

如果 `audit-workflow-service` 中 `audit.model.api-key` 为空，工作流服务会走本地降级逻辑，不会真实调用 Qwen；如果要真实审核，需要配置有效的模型 API Key。

### 7.4 OCR 配置

OCR 服务配置文件：

```text
/opt/audit/ocr-service/config.env
```

如果文件不存在，可以复制示例：

```bash
cd /opt/audit/ocr-service
cp config.example.env config.env
```

使用本地 PaddleOCR：

```env
OCR_PROVIDER=paddle
```

使用阿里云 OCR：

```env
OCR_PROVIDER=aliyun
ALIYUN_OCR_API=general
ALIYUN_OCR_ENDPOINT=ocr-api.cn-hangzhou.aliyuncs.com
ALIBABA_CLOUD_ACCESS_KEY_ID=你的AccessKeyId
ALIBABA_CLOUD_ACCESS_KEY_SECRET=你的AccessKeySecret
```

本地开发建议优先使用 `paddle`，不需要云 OCR 密钥，但首次启动或首次识别会下载模型，耗时较长。

## 8. 安装项目依赖

### 8.1 安装 OCR 服务依赖

```bash
cd /opt/audit/ocr-service
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

如果 PaddleOCR 依赖下载较慢，可以使用国内镜像：

```bash
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

### 8.2 编译业务后端

```bash
cd /opt/audit/backend
mvn -DskipTests install
```

成功时应看到：

```text
BUILD SUCCESS
```

### 8.3 编译工作流服务

```bash
cd /opt/audit/audit-workflow-service
mvn -DskipTests compile
```

成功时应看到：

```text
BUILD SUCCESS
```

### 8.4 安装前端依赖

```bash
cd /opt/audit/ruoyi-ui
nvm use 16
npm ci
```

如果下载慢：

```bash
npm ci --registry=https://registry.npmmirror.com
```

## 9. 启动顺序

建议按以下顺序启动：

1. MySQL
2. PostgreSQL
3. Redis `16379`
4. OCR 服务 `8866`
5. 业务后端 `6039`
6. 工作流服务 `8080`
7. 前端 `5666`

### 9.1 启动基础服务

```bash
sudo systemctl start mysql
sudo systemctl start postgresql
redis-server --port 16379 --daemonize yes
```

检查端口：

```bash
ss -lntp | grep -E '3306|5432|16379'
```

### 9.2 启动 OCR 服务

```bash
cd /opt/audit/ocr-service
source .venv/bin/activate
python -m uvicorn server:app --host 127.0.0.1 --port 8866
```

另开终端验证：

```bash
curl http://127.0.0.1:8866/health
```

预期返回类似：

```json
{"status":"ok"}
```

### 9.3 启动业务后端

```bash
cd /opt/audit/backend/ruoyi-admin
export DASHSCOPE_API_KEY='sk-你的真实key'
mvn spring-boot:run
```

看到类似日志表示启动成功：

```text
Tomcat started on port 6039
```

另开终端验证：

```bash
curl http://127.0.0.1:6039
```

如果返回 404、HTML 或重定向信息，说明服务已经在响应。也可以访问：

```text
http://127.0.0.1:6039/swagger-ui.html
```

### 9.4 启动工作流服务

```bash
cd /opt/audit/audit-workflow-service
export DASHSCOPE_API_KEY='sk-你的真实key'
export AUDIT_EMBEDDING_API_KEY="$DASHSCOPE_API_KEY"
mvn spring-boot:run
```

看到类似日志表示启动成功：

```text
Tomcat started on port 8080
```

另开终端验证：

```bash
curl http://127.0.0.1:8080/api/audit/workflows
curl http://127.0.0.1:8080/api/audit/stats/overview
```

### 9.5 启动前端

```bash
cd /opt/audit/ruoyi-ui
nvm use 16
npm run dev
```

前端开发服务默认端口来自 `vue.config.js`：

```text
5666
```

浏览器访问：

```text
http://127.0.0.1:5666
```

默认账号：

```text
admin / admin123
```

## 10. 一键日常启动命令汇总

初始化完成后，日常开发通常只需要开 4 个终端。

终端 1：OCR

```bash
cd /opt/audit/ocr-service
source .venv/bin/activate
python -m uvicorn server:app --host 127.0.0.1 --port 8866
```

终端 2：业务后端

```bash
cd /opt/audit/backend/ruoyi-admin
export DASHSCOPE_API_KEY='sk-你的真实key'
mvn spring-boot:run
```

终端 3：工作流服务

```bash
cd /opt/audit/audit-workflow-service
export DASHSCOPE_API_KEY='sk-你的真实key'
export AUDIT_EMBEDDING_API_KEY="$DASHSCOPE_API_KEY"
mvn spring-boot:run
```

终端 4：前端

```bash
cd /opt/audit/ruoyi-ui
nvm use 16
npm run dev
```

访问：

```text
http://127.0.0.1:5666
```

## 11. 验证完整链路

### 11.1 检查端口

```bash
ss -lntp | grep -E '5666|6039|8080|8866|3306|5432|16379'
```

应看到：

```text
3306   MySQL
5432   PostgreSQL
16379  Redis
8866   OCR
6039   业务后端
8080   工作流服务
5666   前端
```

### 11.2 检查工作流列表

```bash
curl http://127.0.0.1:8080/api/audit/workflows
```

应包含：

```text
policy_document_audit
uploaded_basis_document_audit
```

### 11.3 检查业务系统登录

访问：

```text
http://127.0.0.1:5666
```

使用：

```text
admin / admin123
```

登录后应能看到 AI 审核、审核列表、审核资源库等菜单。

### 11.4 检查 OCR 服务

```bash
curl http://127.0.0.1:8866/health
```

返回 `status=ok` 即可。

### 11.5 检查数据库

业务库：

```bash
mysql -h 127.0.0.1 -u ruoyi -p ry-vue -e "SELECT COUNT(*) FROM sys_menu;"
mysql -h 127.0.0.1 -u ruoyi -p ry-vue -e "SELECT COUNT(*) FROM audit_review_task;"
```

工作流库：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT workflow_code, enabled FROM audit_workflow;"
```

向量库：

```bash
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -c "SELECT COUNT(*) FROM audit_vector_document;"
```

## 12. 可选：打包启动

开发环境优先用 `mvn spring-boot:run`。如果想测试 jar 包启动：

### 12.1 打包业务后端

```bash
cd /opt/audit/backend
mvn -DskipTests package
```

jar 通常生成在：

```text
backend/ruoyi-admin/target/ruoyi-admin.jar
```

启动：

```bash
cd /opt/audit/backend/ruoyi-admin/target
java -jar ruoyi-admin.jar
```

### 12.2 打包工作流服务

```bash
cd /opt/audit/audit-workflow-service
mvn -DskipTests package
java -jar target/audit-workflow-service-0.0.1-SNAPSHOT.jar
```

### 12.3 构建前端

```bash
cd /opt/audit/ruoyi-ui
npm run build:prod
```

构建产物在：

```text
ruoyi-ui/dist
```

## 13. 常见问题

### 13.1 业务后端启动失败，提示连接 Redis 失败

原因通常是 Redis 没有跑在 `16379`：

```bash
redis-cli -p 16379 ping
```

如果失败：

```bash
redis-server --port 16379 --daemonize yes
```

### 13.2 业务后端启动失败，提示 MySQL 连接失败

检查数据库、用户和密码：

```bash
mysql -h 127.0.0.1 -u ruoyi -p ry-vue -e "SELECT 1;"
```

如果失败，重新执行第 5.1 节授权 SQL。

### 13.3 工作流服务启动失败，提示 `audit_workflow` 连接失败

检查：

```bash
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT 1;"
```

确认 `audit-workflow-service/src/main/resources/application.yml` 中数据库配置与本文一致。

### 13.4 提示 `relation "audit_vector_document" does not exist`

说明 PostgreSQL 向量库没有初始化：

```bash
cd /opt/audit/backend
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -f sql/audit_vector_init.sql
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -f sql/audit_vector_hybrid_search_migration.sql
```

### 13.5 提示 `extension "vector" is not available`

说明 pgvector 没安装成功。重新执行第 3.7 节的 pgvector 安装步骤，安装后再执行：

```bash
sudo -u postgres psql -d audit_vector -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

### 13.6 OCR 服务启动很慢

使用 PaddleOCR 时，首次启动或首次识别可能会下载模型。等待下载完成即可。若网络慢，可以切换 pip 镜像，或改用阿里云 OCR。

### 13.7 前端启动报 OpenSSL 错误

老 Vue CLI/Webpack 项目在 Node 17+ 可能报 OpenSSL 相关错误。优先切换 Node 16：

```bash
nvm install 16
nvm use 16
npm ci
npm run dev
```

临时方案：

```bash
export NODE_OPTIONS=--openssl-legacy-provider
npm run dev
```

### 13.8 前端页面访问不到后端

前端开发代理在 `ruoyi-ui/vue.config.js` 中：

```js
const baseUrl = 'http://localhost:6039'
const port = process.env.port || process.env.npm_config_port || 5666
```

确认业务后端 `6039` 正常：

```bash
ss -lntp | grep 6039
```

### 13.9 工作流检索阶段失败

工作流服务会调用业务后端知识库检索接口：

```text
http://127.0.0.1:6039/audit/library/vector/workflow-search
http://127.0.0.1:6039/audit/library/vector/workflow-batch-search
```

因此必须保证：

1. 业务后端 `6039` 已启动。
2. PostgreSQL `audit_vector` 已初始化。
3. 业务后端 `vector.enabled=true`。
4. `DASHSCOPE_API_KEY` 或嵌入模型 API Key 可用。

### 13.10 文件访问路径异常

新 Ubuntu 必须把业务后端配置中的：

```yaml
ruoyi:
  profile: /Users/wanghaotian/Downloads/ruoyi-ai-mvp/.runtime/uploadPath
```

改为：

```yaml
ruoyi:
  profile: /opt/audit/.runtime/uploadPath
```

并创建目录：

```bash
mkdir -p /opt/audit/.runtime/uploadPath
```

### 13.11 Nginx 反向代理上传限制

本部署指南默认不强制使用 Nginx。如果你在前端或后端前面额外配置了 Nginx 反向代理，需要显式放开请求体大小限制，否则大文件上传可能会返回 `413 Payload Too Large`。

在对应的 `http`、`server` 或 `location` 配置块中加入：

```nginx
client_max_body_size 0;
```

修改后检查并重载 Nginx：

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## 14. 部署完成后的最小检查清单

部署完成后，逐项确认：

```bash
java -version
mvn -version
node -v
python3 --version
mysql -h 127.0.0.1 -u ruoyi -p ry-vue -e "SELECT 1;"
mysql -h 127.0.0.1 -u audit_workflow -p audit_workflow -e "SELECT 1;"
PGPASSWORD=301836 psql -h 127.0.0.1 -U audit_vector_user -d audit_vector -c "SELECT 1;"
redis-cli -p 16379 ping
curl http://127.0.0.1:8866/health
curl http://127.0.0.1:8080/api/audit/workflows
```

最终浏览器访问：

```text
http://127.0.0.1:5666
```

使用：

```text
admin / admin123
```

能登录并看到审核相关菜单，说明开发环境基础部署完成。
