-- Add failed status to AI task status dictionary.
INSERT INTO `sys_dict_data` (
  `dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`,
  `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`
)
SELECT 2315, 5, '已失败', 'failed', 'audit_ai_task_status',
       'danger', 'N', '0', 'admin', NOW(), 'AI任务失败状态'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict_data`
  WHERE `dict_type` = 'audit_ai_task_status'
    AND `dict_value` = 'failed'
);
