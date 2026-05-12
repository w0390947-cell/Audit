SET NAMES utf8mb4;

UPDATE `sys_dict_data`
SET `dict_label` = '驳回'
WHERE `dict_type` = 'audit_review_status'
  AND `dict_value` = 'returned';
