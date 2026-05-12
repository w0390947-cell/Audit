SET NAMES utf8mb4;

ALTER TABLE `audit_review_task`
MODIFY COLUMN `review_status` varchar(20) DEFAULT 'reviewing' COMMENT '审核状态';

ALTER TABLE `audit_ai_task`
MODIFY COLUMN `review_status` varchar(20) DEFAULT 'reviewing' COMMENT '审核状态';

UPDATE `sys_dict_data`
SET `dict_sort` = 3,
    `is_default` = 'N'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'pending';

UPDATE `sys_dict_data`
SET `dict_sort` = 4
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'returned';

UPDATE `sys_dict_data`
SET `dict_label` = '驳回'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'returned';

UPDATE `sys_dict_data`
SET `dict_sort` = 2,
    `dict_label` = '审核中',
    `list_class` = 'primary',
    `is_default` = 'Y',
    `status` = '0'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'reviewing';

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
SELECT 2304, 2, '审核中', 'reviewing', 'audit_review_status', 'primary', 'Y', '0', 'admin', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data`
  WHERE `dict_type` = 'audit_review_status'
    AND `dict_value` = 'reviewing'
);
