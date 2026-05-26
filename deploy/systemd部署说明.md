# systemd 常驻部署说明

本文档用于在 Ubuntu 上把项目的 4 个业务服务交给 systemd 托管，使终端关闭后服务仍持续运行，并支持开机自启、异常重启和统一查看日志。

## 1. 服务清单

本目录提供以下文件：

```text
deploy/
  audit-ocr.service
  audit-backend.service
  audit-workflow.service
  audit-frontend.service
  audit-services.env.example
  systemd部署说明.md
```

四个服务对应关系：

| systemd 服务名 | 项目目录 | 端口 | 启动方式 |
|---|---|---:|---|
| `audit-ocr` | `/opt/audit/ocr-service` | `8866` | Python venv + uvicorn |
| `audit-backend` | `/opt/audit/backend` | `6039` | `java -jar ruoyi-admin.jar` |
| `audit-workflow` | `/opt/audit/audit-workflow-service` | `8080` | `java -jar audit-workflow-service-0.0.1-SNAPSHOT.jar` |
| `audit-frontend` | `/opt/audit/ruoyi-ui` | `5666` | `npm run dev` |

默认假设：

```text
项目部署路径：/opt/audit
运行用户：ubuntu
```

如果实际路径或用户不同，需要同步修改 4 个 `.service` 文件中的 `User`、`Group`、`WorkingDirectory`、`ExecStart`、`EnvironmentFile`。

## 2. 首次准备

确认项目在 `/opt/audit`：

```bash
ls /opt/audit
```

建议把项目目录归属给 `ubuntu`：

```bash
sudo chown -R ubuntu:ubuntu /opt/audit
```

创建后端上传目录：

```bash
mkdir -p /opt/audit/.runtime/uploadPath
```

确认基础服务已可用：

```bash
sudo systemctl status mysql
sudo systemctl status postgresql
ss -lntp | grep -E '3306|5432|16379'
```

如果 Redis 没有配置成 systemd 服务，至少要确认它已按项目要求监听 `16379`。

## 3. 准备环境变量

复制环境变量示例：

```bash
cd /opt/audit
cp deploy/audit-services.env.example deploy/audit-services.env
```

编辑真实配置：

```bash
nano /opt/audit/deploy/audit-services.env
```

至少关注以下配置：

```env
# DASHSCOPE_API_KEY=sk-your-real-key
# AUDIT_EMBEDDING_API_KEY=sk-your-real-key

AUDIT_VECTOR_JDBC_URL=jdbc:postgresql://127.0.0.1:5432/audit_vector
AUDIT_VECTOR_JDBC_USERNAME=audit_vector_user
AUDIT_VECTOR_JDBC_PASSWORD=301836

AUDIT_OCR_ENDPOINT=http://127.0.0.1:8866/ocr
AUDIT_OCR_PROVIDER=paddle
AUDIT_OCR_TIMEOUT_SECONDS=600

OCR_PROVIDER=paddle
```

如果使用阿里云 OCR，把 `OCR_PROVIDER` 改为 `aliyun`，并填写：

```env
ALIYUN_OCR_API=general
ALIYUN_OCR_ENDPOINT=ocr-api.cn-hangzhou.aliyuncs.com
ALIBABA_CLOUD_ACCESS_KEY_ID=你的AccessKeyId
ALIBABA_CLOUD_ACCESS_KEY_SECRET=你的AccessKeySecret
```

保护环境变量文件权限：

```bash
chmod 600 /opt/audit/deploy/audit-services.env
```

## 4. 准备 OCR 服务

安装 Python 依赖：

```bash
cd /opt/audit/ocr-service
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

如果下载慢，可以使用镜像：

```bash
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

如果需要 OCR 专属配置文件：

```bash
cd /opt/audit/ocr-service
cp -n config.example.env config.env
nano config.env
```

说明：`audit-ocr.service` 会读取 `/opt/audit/deploy/audit-services.env`，`server.py` 也会读取当前目录下的 `config.env`。环境变量文件中的值优先级更高。

## 5. 构建后端服务

后端 systemd 使用 jar 包启动，所以需要先打包：

```bash
cd /opt/audit/backend
mvn -DskipTests package
```

确认 jar 存在：

```bash
ls -lh /opt/audit/backend/ruoyi-admin/target/ruoyi-admin.jar
```

`audit-backend.service` 已在启动参数中覆盖上传目录：

```text
--ruoyi.profile=/opt/audit/.runtime/uploadPath
```

这样可以避免源码配置里的本机路径影响 Ubuntu 部署。

## 6. 构建 Workflow 服务

Workflow systemd 同样使用 jar 包启动：

```bash
cd /opt/audit/audit-workflow-service
mvn -DskipTests package
```

确认 jar 存在：

```bash
ls -lh /opt/audit/audit-workflow-service/target/audit-workflow-service-0.0.1-SNAPSHOT.jar
```

## 7. 准备前端服务

本方案按要求使用 `npm run dev` 作为 systemd 常驻服务。

安装依赖：

```bash
cd /opt/audit/ruoyi-ui
npm ci
```

如果下载慢：

```bash
npm ci --registry=https://registry.npmmirror.com
```

如果你使用 nvm，`audit-frontend.service` 会尝试加载：

```text
/home/ubuntu/.nvm/nvm.sh
```

并执行：

```bash
nvm use 16
```

如果你不用 nvm，需要确保 `ubuntu` 用户在 systemd 环境中能找到 `npm`。可用以下命令确认：

```bash
sudo -u ubuntu bash -lc 'command -v npm && npm -v'
```

## 8. 安装 systemd 服务

复制服务文件：

```bash
sudo install -m 0644 /opt/audit/deploy/audit-ocr.service /etc/systemd/system/audit-ocr.service
sudo install -m 0644 /opt/audit/deploy/audit-backend.service /etc/systemd/system/audit-backend.service
sudo install -m 0644 /opt/audit/deploy/audit-workflow.service /etc/systemd/system/audit-workflow.service
sudo install -m 0644 /opt/audit/deploy/audit-frontend.service /etc/systemd/system/audit-frontend.service
```

重新加载 systemd：

```bash
sudo systemctl daemon-reload
```

设置开机自启：

```bash
sudo systemctl enable audit-ocr
sudo systemctl enable audit-backend
sudo systemctl enable audit-workflow
sudo systemctl enable audit-frontend
```

## 9. 启动服务

建议按以下顺序启动：

```bash
sudo systemctl start audit-ocr
sudo systemctl start audit-backend
sudo systemctl start audit-workflow
sudo systemctl start audit-frontend
```

也可以一次启动：

```bash
sudo systemctl start audit-ocr audit-backend audit-workflow audit-frontend
```

查看状态：

```bash
sudo systemctl status audit-ocr
sudo systemctl status audit-backend
sudo systemctl status audit-workflow
sudo systemctl status audit-frontend
```

确认端口：

```bash
ss -lntp | grep -E '5666|6039|8080|8866'
```

## 10. 验证访问

OCR：

```bash
curl http://127.0.0.1:8866/health
```

后端：

```bash
curl -I http://127.0.0.1:6039
```

Workflow：

```bash
curl http://127.0.0.1:8080/api/audit/workflows
curl http://127.0.0.1:8080/api/audit/stats/overview
```

前端：

```text
http://服务器IP:5666
```

如果只在服务器本机验证：

```text
http://127.0.0.1:5666
```

## 11. 查看日志

实时查看：

```bash
journalctl -u audit-ocr -f
journalctl -u audit-backend -f
journalctl -u audit-workflow -f
journalctl -u audit-frontend -f
```

查看最近 200 行：

```bash
journalctl -u audit-ocr -n 200 --no-pager
journalctl -u audit-backend -n 200 --no-pager
journalctl -u audit-workflow -n 200 --no-pager
journalctl -u audit-frontend -n 200 --no-pager
```

查看本次启动以来的日志：

```bash
journalctl -u audit-backend -b --no-pager
```

## 12. 重启和停止

重启单个服务：

```bash
sudo systemctl restart audit-backend
```

重启全部业务服务：

```bash
sudo systemctl restart audit-ocr audit-backend audit-workflow audit-frontend
```

停止全部业务服务：

```bash
sudo systemctl stop audit-frontend audit-workflow audit-backend audit-ocr
```

禁用开机自启：

```bash
sudo systemctl disable audit-ocr audit-backend audit-workflow audit-frontend
```

## 13. 更新代码后的发布流程

拉取或替换代码后，按服务类型处理。

后端更新：

```bash
cd /opt/audit/backend
mvn -DskipTests package
sudo systemctl restart audit-backend
```

Workflow 更新：

```bash
cd /opt/audit/audit-workflow-service
mvn -DskipTests package
sudo systemctl restart audit-workflow
```

OCR 更新：

```bash
cd /opt/audit/ocr-service
source .venv/bin/activate
pip install -r requirements.txt
sudo systemctl restart audit-ocr
```

前端更新：

```bash
cd /opt/audit/ruoyi-ui
npm ci
sudo systemctl restart audit-frontend
```

修改 `.service` 文件后，需要执行：

```bash
sudo systemctl daemon-reload
sudo systemctl restart 服务名
```

## 14. 常见问题

### 14.1 服务启动后马上退出

查看日志：

```bash
journalctl -u 服务名 -n 200 --no-pager
```

重点检查：

```bash
ls -lh /opt/audit/backend/ruoyi-admin/target/ruoyi-admin.jar
ls -lh /opt/audit/audit-workflow-service/target/audit-workflow-service-0.0.1-SNAPSHOT.jar
ls -lh /opt/audit/ocr-service/.venv/bin/python
```

### 14.2 `/usr/bin/java` 不存在

查看 Java 路径：

```bash
command -v java
```

如果不是 `/usr/bin/java`，修改：

```bash
sudo nano /etc/systemd/system/audit-backend.service
sudo nano /etc/systemd/system/audit-workflow.service
sudo systemctl daemon-reload
```

把 `ExecStart=/usr/bin/java ...` 改成实际路径。

### 14.3 前端找不到 npm

确认 `ubuntu` 用户能加载 npm：

```bash
sudo -u ubuntu bash -lc 'command -v npm && npm -v'
```

如果使用 nvm，确认文件存在：

```bash
ls -lh /home/ubuntu/.nvm/nvm.sh
```

如果 Node 是系统级安装，可以把 `audit-frontend.service` 的 `ExecStart` 改成：

```ini
ExecStart=/usr/bin/npm run dev -- --host 0.0.0.0 --port 5666 --no-open
```

修改后执行：

```bash
sudo systemctl daemon-reload
sudo systemctl restart audit-frontend
```

### 14.4 端口被占用

查看占用：

```bash
ss -lntp | grep -E '5666|6039|8080|8866'
```

如果是手动启动的旧进程，先停止旧进程，再启动 systemd 服务。

### 14.5 配置环境变量后不生效

修改 `/opt/audit/deploy/audit-services.env` 后，需要重启对应服务：

```bash
sudo systemctl restart audit-ocr
sudo systemctl restart audit-workflow
sudo systemctl restart audit-backend
```

可以查看 systemd 实际读取的环境文件路径：

```bash
systemctl cat audit-ocr
systemctl cat audit-workflow
```

## 15. 服务文件卸载

如果不再使用 systemd 托管：

```bash
sudo systemctl stop audit-frontend audit-workflow audit-backend audit-ocr
sudo systemctl disable audit-frontend audit-workflow audit-backend audit-ocr
sudo rm -f /etc/systemd/system/audit-frontend.service
sudo rm -f /etc/systemd/system/audit-workflow.service
sudo rm -f /etc/systemd/system/audit-backend.service
sudo rm -f /etc/systemd/system/audit-ocr.service
sudo systemctl daemon-reload
```
