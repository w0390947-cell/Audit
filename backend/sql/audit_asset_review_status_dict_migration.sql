SET NAMES utf8mb4;

ALTER TABLE `audit_asset_record`
  MODIFY COLUMN `review_status` varchar(20) DEFAULT 'reviewing' COMMENT '审核状态';

UPDATE `audit_asset_record`
SET `review_status` = 'reviewing'
WHERE `review_status` = 'pending';

UPDATE `sys_dict_type`
SET `dict_name` = '审核资产状态',
    `status` = '0',
    `remark` = '审核资产库'
WHERE `dict_type` = 'audit_asset_review_status';

INSERT INTO `sys_dict_type`
(`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT '审核资产状态', 'audit_asset_review_status', '0', 'admin', NOW(), '审核资产库'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_type`
  WHERE `dict_type` = 'audit_asset_review_status'
);

UPDATE `sys_dict_data`
SET `dict_sort` = 1,
    `dict_label` = '驳回',
    `list_class` = 'danger',
    `is_default` = 'N',
    `status` = '0'
WHERE `dict_type` = 'audit_asset_review_status'
  AND `dict_value` = 'returned';

INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
SELECT 1, '驳回', 'returned', 'audit_asset_review_status', 'danger', 'N', '0', 'admin', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data`
  WHERE `dict_type` = 'audit_asset_review_status'
    AND `dict_value` = 'returned'
);

UPDATE `sys_dict_data`
SET `dict_sort` = 2,
    `dict_label` = '审核通过',
    `list_class` = 'success',
    `is_default` = 'N',
    `status` = '0'
WHERE `dict_type` = 'audit_asset_review_status'
  AND `dict_value` = 'approved';

INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
SELECT 2, '审核通过', 'approved', 'audit_asset_review_status', 'success', 'N', '0', 'admin', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data`
  WHERE `dict_type` = 'audit_asset_review_status'
    AND `dict_value` = 'approved'
);

UPDATE `sys_dict_data`
SET `dict_sort` = 3,
    `dict_label` = '待人工审核',
    `list_class` = 'warning',
    `is_default` = 'Y',
    `status` = '0'
WHERE `dict_type` = 'audit_asset_review_status'
  AND `dict_value` = 'reviewing';

INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
SELECT 3, '待人工审核', 'reviewing', 'audit_asset_review_status', 'warning', 'Y', '0', 'admin', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data`
  WHERE `dict_type` = 'audit_asset_review_status'
    AND `dict_value` = 'reviewing'
);
