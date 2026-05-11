# Ubuntu 数据库迁移操作说明

本文说明如何在 Ubuntu 服务器上迁移本项目的数据库变更。当前项目主业务库使用 MySQL，连接配置位于：

- `backend/ruoyi-admin/src/main/resources/application-druid.yml`
- 默认库名示例：`ry-vue`

> 注意：`backend/sql/*_init.sql` 多数是初始化脚本，包含 `DROP TABLE`、`DELETE FROM sys_dict_data` 等语句，通常只用于全新环境初始化。线上或已有数据环境应优先执行明确的迁移脚本，例如本次新增的 `backend/sql/audit_review_status_reviewing_migration.sql`。

## 1. 迁移前准备

确认服务器具备 MySQL 客户端：

```bash
mysql --version
mysqldump --version
```

如果未安装：

```bash
sudo apt update
sudo apt install -y mysql-client
```

确认数据库连接信息。优先以实际部署环境为准：

```bash
grep -n "jdbc:mysql\|username\|password" backend/ruoyi-admin/src/main/resources/application-druid.yml
```

建议将连接信息设置为环境变量，避免命令历史中出现密码：

```bash
export DB_HOST="127.0.0.1"
export DB_PORT="3306"
export DB_NAME="ry-vue"
export DB_USER="ruoyi"
```

后续命令会通过 `-p` 交互式输入密码。

## 2. 备份数据库

迁移前必须备份完整业务库：

```bash
mkdir -p ~/db-backup
mysqldump \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --user="$DB_USER" \
  -p \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  --default-character-set=utf8mb4 \
  "$DB_NAME" > ~/db-backup/${DB_NAME}_$(date +%Y%m%d_%H%M%S).sql
```

确认备份文件非空：

```bash
ls -lh ~/db-backup
```

## 3. 上传或定位迁移脚本

如果代码已经部署在 Ubuntu 服务器上，进入项目目录：

```bash
cd /path/to/Audit
```

确认迁移脚本存在：

```bash
ls -lh backend/sql/audit_review_status_reviewing_migration.sql
```

如果脚本在本地，需要先上传到服务器，例如：

```bash
scp backend/sql/audit_review_status_reviewing_migration.sql user@server:/tmp/
```

服务器上执行时将路径替换为实际路径。

## 4. 执行迁移脚本

以本次“审核中”状态迁移为例：

```bash
mysql \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --user="$DB_USER" \
  -p \
  --default-character-set=utf8mb4 \
  --show-warnings \
  "$DB_NAME" < backend/sql/audit_review_status_reviewing_migration.sql
```

如果脚本上传到了 `/tmp`：

```bash
mysql \
  --host="$DB_HOST" \
  --port="$DB_PORT" \
  --user="$DB_USER" \
  -p \
  --default-character-set=utf8mb4 \
  --show-warnings \
  "$DB_NAME" < /tmp/audit_review_status_reviewing_migration.sql
```

## 5. 验证迁移结果

进入 MySQL：

```bash
mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" -p "$DB_NAME"
```

检查 `audit_review_task.review_status` 默认值：

```sql
SHOW COLUMNS FROM audit_review_task LIKE 'review_status';
```

应看到默认值为 `reviewing`。

检查 `audit_ai_task.review_status` 默认值：

```sql
SHOW COLUMNS FROM audit_ai_task LIKE 'review_status';
```

应看到默认值为 `reviewing`。

检查审核状态字典：

```sql
SELECT dict_sort, dict_label, dict_value, list_class, is_default, status
FROM sys_dict_data
WHERE dict_type = 'audit_review_status'
ORDER BY dict_sort;
```

应包含：

| dict_label | dict_value |
| --- | --- |
| 审核通过 | approved |
| 审核中 | reviewing |
| 待修改 | pending |
| 驳回归档 | returned |

## 6. 重启或刷新缓存

RuoYi 字典通常会被缓存。执行字典类迁移后，需要刷新缓存。

可选方式：

1. 在系统管理中清理字典缓存。
2. 清理 Redis 中的字典缓存。
3. 直接重启后端服务。

如果使用 systemd 管理后端服务：

```bash
sudo systemctl restart ruoyi
sudo systemctl status ruoyi --no-pager
```

如果是手动运行 jar，先停止旧进程，再重新启动：

```bash
ps -ef | grep ruoyi-admin | grep -v grep
```

按实际部署方式停止后重新启动。

## 7. 功能验证

在浏览器中验证：

1. 进入“审核列表管理 -> 审核列表”。
2. 新增一条审核任务。
3. 确认该任务的“审核状态”为“审核中”。
4. 进入详情页，等待或查看 AI 审核结果。
5. 未点击人工审核按钮前，审核列表中应仍显示“审核中”。
6. 在详情页点击“审核通过”，审核列表应变为“审核通过”。
7. 新增另一条任务，在详情页点击“待修改”，审核列表应变为“待修改”。
8. 顶部“审核状态”筛选器应能筛选“审核中”。
9. 导出“审核列表数据”时，审核状态应能正确显示“审核中”。

## 8. 回滚方案

如果迁移后需要回滚，优先使用迁移前备份恢复：

```bash
mysql --host="$DB_HOST" --port="$DB_PORT" --user="$DB_USER" -p "$DB_NAME" < ~/db-backup/你的备份文件.sql
```

如果只想撤销本次字典和默认值变更，可执行：

```sql
ALTER TABLE audit_review_task
MODIFY COLUMN review_status varchar(20) DEFAULT 'pending' COMMENT '审核状态';

ALTER TABLE audit_ai_task
MODIFY COLUMN review_status varchar(20) DEFAULT 'pending' COMMENT '审核状态';

DELETE FROM sys_dict_data
WHERE dict_type = 'audit_review_status'
  AND dict_value = 'reviewing';

UPDATE sys_dict_data
SET dict_sort = 2,
    is_default = 'Y'
WHERE dict_type = 'audit_review_status'
  AND dict_value = 'pending';

UPDATE sys_dict_data
SET dict_sort = 3
WHERE dict_type = 'audit_review_status'
  AND dict_value = 'returned';
```

回滚后同样需要刷新字典缓存或重启后端。

## 9. 常见问题

### 9.1 执行 SQL 后页面仍没有“审核中”

通常是字典缓存未刷新。清理字典缓存或重启后端。

### 9.2 `Access denied`

检查用户名、密码、来源主机授权：

```sql
SELECT user, host FROM mysql.user;
```

必要时由 DBA 授权对应用户访问目标库。

### 9.3 `Unknown database`

检查 `DB_NAME` 是否与 `application-druid.yml` 中 JDBC URL 的库名一致。

### 9.4 脚本执行到一半失败

不要立即重复执行初始化脚本。先查看错误信息，确认失败点。若已经影响数据且无法确认状态，使用迁移前备份恢复后再重新执行迁移脚本。
