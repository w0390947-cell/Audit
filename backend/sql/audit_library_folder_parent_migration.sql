SET NAMES utf8mb4;

ALTER TABLE `audit_library_folder`
  ADD COLUMN `parent_id` bigint DEFAULT NULL COMMENT '父级文件库主键' AFTER `folder_id`;

CREATE INDEX `idx_audit_library_folder_parent`
  ON `audit_library_folder` (`parent_id`);
