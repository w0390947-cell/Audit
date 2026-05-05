SET NAMES utf8mb4;

DELETE FROM `audit_common_resource_version`;
DELETE FROM `audit_common_resource`;
DELETE FROM `audit_task_resource`;
DELETE FROM `audit_library_folder`;

ALTER TABLE `audit_library_folder` AUTO_INCREMENT = 1;
ALTER TABLE `audit_common_resource` AUTO_INCREMENT = 1;
ALTER TABLE `audit_common_resource_version` AUTO_INCREMENT = 1;
ALTER TABLE `audit_task_resource` AUTO_INCREMENT = 1;
