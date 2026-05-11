SET NAMES utf8mb4;

ALTER TABLE `audit_common_resource`
MODIFY COLUMN `storage_status` varchar(20) DEFAULT 'pending' COMMENT '向量化状态';

UPDATE `audit_common_resource`
SET `storage_status` = 'pending',
    `progress_text` = CASE
        WHEN `progress_text` IS NULL OR `progress_text` = '' OR `progress_text` = '文本解析智能体解析中'
            THEN '等待向量化任务执行'
        ELSE `progress_text`
    END
WHERE `storage_status` = 'processing';

UPDATE `sys_dict_type`
SET `dict_name` = '向量化状态',
    `remark` = '审核资源库'
WHERE `dict_type` = 'audit_file_storage_status';

DELETE FROM `sys_dict_data`
WHERE `dict_type` = 'audit_file_storage_status';

INSERT INTO `sys_dict_data`
(`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`)
VALUES
(2404, 1, '等待向量化', 'pending', 'audit_file_storage_status', 'info', 'Y', '0', 'admin', NOW()),
(2405, 2, '解析中', 'parsing', 'audit_file_storage_status', 'primary', 'N', '0', 'admin', NOW()),
(2406, 3, '向量生成中', 'embedding', 'audit_file_storage_status', 'warning', 'N', '0', 'admin', NOW()),
(2402, 4, '已向量化', 'stored', 'audit_file_storage_status', 'success', 'N', '0', 'admin', NOW()),
(2407, 5, '未识别文本', 'text_empty', 'audit_file_storage_status', 'danger', 'N', '0', 'admin', NOW()),
(2403, 6, '向量化失败', 'failed', 'audit_file_storage_status', 'danger', 'N', '0', 'admin', NOW());
