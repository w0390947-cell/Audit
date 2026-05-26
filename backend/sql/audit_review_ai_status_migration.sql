SET NAMES utf8mb4;

UPDATE `sys_dict_data`
SET `dict_sort` = 3,
    `dict_label` = '待人工审核',
    `list_class` = 'warning',
    `is_default` = 'Y',
    `status` = '0'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'reviewing';

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
SELECT 2304, 3, '待人工审核', 'reviewing', 'audit_review_status', 'warning', 'Y', '0', 'admin', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data`
  WHERE `dict_type` = 'audit_review_status'
    AND `dict_value` = 'reviewing'
);

UPDATE `sys_dict_data`
SET `dict_sort` = 4,
    `is_default` = 'N',
    `status` = '0'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'pending';

UPDATE `sys_dict_data`
SET `dict_sort` = 5,
    `status` = '0'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'returned';

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
SELECT 2305, 2, 'AI审核中', 'ai_reviewing', 'audit_review_status', 'primary', 'N', '0', 'admin', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data`
  WHERE `dict_type` = 'audit_review_status'
    AND `dict_value` = 'ai_reviewing'
);

UPDATE `sys_dict_data`
SET `dict_sort` = 2,
    `dict_label` = 'AI审核中',
    `list_class` = 'primary',
    `is_default` = 'N',
    `status` = '0'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'ai_reviewing';

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
SELECT 2306, 6, 'AI审核失败', 'ai_failed', 'audit_review_status', 'danger', 'N', '0', 'admin', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data`
  WHERE `dict_type` = 'audit_review_status'
    AND `dict_value` = 'ai_failed'
);

UPDATE `sys_dict_data`
SET `dict_sort` = 6,
    `dict_label` = 'AI审核失败',
    `list_class` = 'danger',
    `is_default` = 'N',
    `status` = '0'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'ai_failed';
