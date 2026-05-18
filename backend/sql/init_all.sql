-- Full initialization script for a fresh ry-vue database.
-- Run from the backend directory:
-- mysql -h 127.0.0.1 -u ruoyi -p ry-vue < sql/init_all.sql

SET NAMES utf8mb4;

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
