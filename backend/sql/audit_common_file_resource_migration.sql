SET NAMES utf8mb4;

ALTER TABLE `audit_library_folder`
  ADD COLUMN `library_type` varchar(20) NOT NULL DEFAULT 'audit' COMMENT '资源库类型：audit审核文件库 common常用文件资源' AFTER `parent_id`;

ALTER TABLE `audit_common_resource`
  ADD COLUMN `library_type` varchar(20) NOT NULL DEFAULT 'audit' COMMENT '资源库类型：audit审核文件库 common常用文件资源' AFTER `resource_id`;

UPDATE `audit_library_folder`
SET `library_type` = 'audit'
WHERE `library_type` IS NULL OR `library_type` = '';

UPDATE `audit_common_resource`
SET `library_type` = 'audit'
WHERE `library_type` IS NULL OR `library_type` = '';

CREATE INDEX `idx_audit_library_folder_type`
  ON `audit_library_folder` (`library_type`);

CREATE INDEX `idx_audit_common_resource_type`
  ON `audit_common_resource` (`library_type`);

UPDATE `sys_menu`
SET `visible` = '0',
    `component` = 'audit/library/common',
    `route_name` = 'AuditLibraryCommon',
    `status` = '0',
    `remark` = '常用文件资源'
WHERE `menu_id` = 2037;
