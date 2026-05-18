-- Gate audit file vectorization behind audit asset approval.

ALTER TABLE `audit_asset_record`
  ADD COLUMN `library_resource_id` bigint DEFAULT NULL COMMENT '审核文件库资源主键' AFTER `review_task_id`,
  ADD KEY `idx_audit_asset_library_resource` (`library_resource_id`);

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
SELECT 2408, 2, '待审核', 'reviewing', 'audit_file_storage_status', 'primary', 'N', '0', 'admin', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'audit_file_storage_status' AND `dict_value` = 'reviewing'
);

UPDATE `sys_dict_data`
SET `dict_sort` = 3
WHERE `dict_type` = 'audit_file_storage_status' AND `dict_value` = 'parsing';

UPDATE `sys_dict_data`
SET `dict_sort` = 4
WHERE `dict_type` = 'audit_file_storage_status' AND `dict_value` = 'embedding';

UPDATE `sys_dict_data`
SET `dict_sort` = 5
WHERE `dict_type` = 'audit_file_storage_status' AND `dict_value` = 'stored';

UPDATE `sys_dict_data`
SET `dict_sort` = 6
WHERE `dict_type` = 'audit_file_storage_status' AND `dict_value` = 'text_empty';

UPDATE `sys_dict_data`
SET `dict_sort` = 7
WHERE `dict_type` = 'audit_file_storage_status' AND `dict_value` = 'failed';

INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`,
 `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 2050, '审核资源审核', 2022, 9, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:review', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE `menu_id` = 2050
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT role_id, 2050
FROM `sys_role`
WHERE role_id IN (1, 2)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = `sys_role`.role_id AND rm.menu_id = 2050
  );
