SET NAMES utf8mb4;

DROP TABLE IF EXISTS `audit_task_resource`;
DROP TABLE IF EXISTS `audit_vector_eval_case`;
DROP TABLE IF EXISTS `audit_vector_search_log`;
DROP TABLE IF EXISTS `audit_vector_task`;
DROP TABLE IF EXISTS `audit_common_resource_version`;
DROP TABLE IF EXISTS `audit_common_resource`;
DROP TABLE IF EXISTS `audit_library_folder`;
DROP TABLE IF EXISTS `audit_asset_resubmit_record`;
DROP TABLE IF EXISTS `audit_asset_ai_step`;
DROP TABLE IF EXISTS `audit_asset_ai_version`;
DROP TABLE IF EXISTS `audit_asset_record`;

CREATE TABLE `audit_asset_record` (
  `asset_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资产主键',
  `review_task_id` bigint DEFAULT NULL COMMENT '审核任务主键',
  `library_resource_id` bigint DEFAULT NULL COMMENT '审核文件库资源主键',
  `task_no` varchar(64) NOT NULL COMMENT '任务编号',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `delivery_unit` varchar(100) DEFAULT '' COMMENT '送检单位',
  `submitter` varchar(64) DEFAULT '' COMMENT '提交人',
  `reviewer` varchar(64) DEFAULT '' COMMENT '审核人',
  `permission_owner` varchar(64) DEFAULT '' COMMENT '权限分配对象',
  `ai_analysis_count` int DEFAULT '0' COMMENT 'AI分析次数',
  `current_ai_version` varchar(20) DEFAULT '' COMMENT '当前AI版本',
  `review_status` varchar(20) DEFAULT 'pending' COMMENT '审核状态',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `report_file_name` varchar(255) DEFAULT '' COMMENT '报告文件名称',
  `report_file_url` varchar(500) DEFAULT '' COMMENT '报告文件地址',
  `ai_opinion` text COMMENT 'AI观点',
  `final_opinion` text COMMENT '最终审核意见',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`asset_id`),
  KEY `idx_audit_asset_task_no` (`task_no`),
  KEY `idx_audit_asset_library_resource` (`library_resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核资产记录表';

CREATE TABLE `audit_asset_ai_version` (
  `version_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI版本主键',
  `asset_id` bigint NOT NULL COMMENT '资产主键',
  `version_no` varchar(20) NOT NULL COMMENT '版本号',
  `word_count_text` varchar(32) DEFAULT '' COMMENT '字数描述',
  `current_flag` char(1) DEFAULT '0' COMMENT '是否当前版本（0否 1是）',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  KEY `idx_audit_asset_ai_version_asset` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核资产AI分析版本表';

CREATE TABLE `audit_asset_ai_step` (
  `step_id` bigint NOT NULL AUTO_INCREMENT COMMENT '步骤主键',
  `version_id` bigint NOT NULL COMMENT '版本主键',
  `step_no` int DEFAULT '1' COMMENT '步骤序号',
  `step_title` varchar(100) DEFAULT '' COMMENT '步骤标题',
  `step_content` varchar(1000) DEFAULT '' COMMENT '步骤内容',
  `step_time` datetime DEFAULT NULL COMMENT '步骤时间',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`step_id`),
  KEY `idx_audit_asset_ai_step_version` (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核资产AI分析步骤表';

CREATE TABLE `audit_asset_resubmit_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '重提记录主键',
  `asset_id` bigint NOT NULL COMMENT '资产主键',
  `version_no` varchar(20) DEFAULT '' COMMENT '版本号',
  `submitter` varchar(64) DEFAULT '' COMMENT '提交人',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `file_name` varchar(255) DEFAULT '' COMMENT '文件名称',
  `file_url` varchar(500) DEFAULT '' COMMENT '文件地址',
  `image_urls` text COMMENT '修改图片',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`record_id`),
  KEY `idx_audit_asset_resubmit_asset` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核资产修改与重提记录表';

CREATE TABLE `audit_library_folder` (
  `folder_id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件库主键',
  `parent_id` bigint DEFAULT NULL COMMENT '父级文件库主键',
  `library_type` varchar(20) NOT NULL DEFAULT 'audit' COMMENT '资源库类型：audit审核文件库 common常用文件资源',
  `folder_name` varchar(100) NOT NULL COMMENT '文件库名称',
  `intro` varchar(500) DEFAULT '' COMMENT '简介',
  `visible_scope` varchar(20) DEFAULT 'all' COMMENT '可见范围',
  `top_flag` char(1) DEFAULT '0' COMMENT '是否置顶（0否 1是）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`folder_id`),
  KEY `idx_audit_library_folder_type` (`library_type`),
  KEY `idx_audit_library_folder_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核文件库';

CREATE TABLE `audit_common_resource` (
  `resource_id` bigint NOT NULL AUTO_INCREMENT COMMENT '常用资源主键',
  `library_type` varchar(20) NOT NULL DEFAULT 'audit' COMMENT '资源库类型：audit审核文件库 common常用文件资源',
  `document_name` varchar(100) NOT NULL COMMENT '文档名称',
  `folder_id` bigint DEFAULT NULL COMMENT '归属文件库主键',
  `folder_name` varchar(100) DEFAULT '' COMMENT '归属文件库名称',
  `storage_status` varchar(20) DEFAULT 'pending' COMMENT '向量化状态',
  `progress_text` varchar(255) DEFAULT '' COMMENT '文件进度',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `latest_modify_time` datetime DEFAULT NULL COMMENT '最新修改时间',
  `file_size` varchar(32) DEFAULT '' COMMENT '文件大小',
  `file_name` varchar(255) DEFAULT '' COMMENT '文件名称',
  `file_url` varchar(500) DEFAULT '' COMMENT '文件地址',
  `current_version_no` varchar(20) DEFAULT 'v1.0' COMMENT '当前版本号',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`resource_id`),
  KEY `idx_audit_common_resource_type` (`library_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='常用文件资源表';

CREATE TABLE `audit_common_resource_version` (
  `version_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源版本主键',
  `resource_id` bigint NOT NULL COMMENT '资源主键',
  `version_no` varchar(20) NOT NULL COMMENT '版本号',
  `file_name` varchar(255) DEFAULT '' COMMENT '文件名称',
  `file_url` varchar(500) DEFAULT '' COMMENT '文件地址',
  `file_size` varchar(32) DEFAULT '' COMMENT '文件大小',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  KEY `idx_audit_common_resource_version_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='常用文件资源历史版本表';

CREATE TABLE `audit_task_resource` (
  `resource_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务资源主键',
  `file_no` varchar(64) NOT NULL COMMENT '文件编号',
  `file_name` varchar(100) NOT NULL COMMENT '文件名称',
  `archive_time` datetime DEFAULT NULL COMMENT '归档时间',
  `folder_id` bigint DEFAULT NULL COMMENT '归属文件库主键',
  `folder_name` varchar(100) DEFAULT '' COMMENT '归属文件库名称',
  `collect_status` varchar(20) DEFAULT 'processing' COMMENT '文件采集状态',
  `preview_file_url` varchar(500) DEFAULT '' COMMENT '预览文件地址',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='任务文件资源表';

CREATE TABLE IF NOT EXISTS `audit_vector_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务主键',
  `resource_id` bigint NOT NULL COMMENT '文件资源主键',
  `task_type` varchar(20) NOT NULL COMMENT '任务类型 index/reindex/delete',
  `task_status` varchar(20) DEFAULT 'pending' COMMENT 'pending/running/success/failed/skipped/cancelled',
  `retry_count` int DEFAULT 0 COMMENT '重试次数',
  `max_retry_count` int DEFAULT 3 COMMENT '最大重试次数',
  `progress_text` varchar(255) DEFAULT '' COMMENT '任务进度',
  `error_msg` text COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_audit_vector_task_status` (`task_status`),
  KEY `idx_audit_vector_task_resource` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核文件向量化任务表';

CREATE TABLE IF NOT EXISTS `audit_vector_search_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `request_id` varchar(100) DEFAULT '' COMMENT '请求ID',
  `workflow_code` varchar(100) DEFAULT '' COMMENT '工作流编码',
  `task_id` varchar(100) DEFAULT '' COMMENT '工作流任务ID',
  `query_count` int DEFAULT 1 COMMENT '查询数量',
  `permission_mode` varchar(32) DEFAULT '' COMMENT '权限模式',
  `scope_summary` varchar(1000) DEFAULT '' COMMENT '范围摘要',
  `retrieval_config` varchar(1000) DEFAULT '' COMMENT '检索配置摘要',
  `result_count` int DEFAULT 0 COMMENT '总召回数量',
  `top_resource_ids` varchar(1000) DEFAULT '' COMMENT '召回资源摘要',
  `status` varchar(20) DEFAULT 'success' COMMENT 'success/failed',
  `error_code` varchar(64) DEFAULT '' COMMENT '错误码',
  `error_msg` varchar(500) DEFAULT '' COMMENT '错误信息',
  `cost_ms` bigint DEFAULT 0 COMMENT '耗时毫秒',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `idx_audit_vector_search_log_request` (`request_id`),
  KEY `idx_audit_vector_search_log_workflow` (`workflow_code`, `task_id`),
  KEY `idx_audit_vector_search_log_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核知识库检索日志表';

CREATE TABLE IF NOT EXISTS `audit_vector_eval_case` (
  `case_id` bigint NOT NULL AUTO_INCREMENT COMMENT '评估样例主键',
  `workflow_code` varchar(100) DEFAULT '' COMMENT '工作流编码',
  `query_text` varchar(1000) NOT NULL COMMENT '检索问题',
  `expected_resource_id` bigint DEFAULT NULL COMMENT '期望资源ID',
  `expected_chunk_uid` varchar(100) DEFAULT '' COMMENT '期望分片UID',
  `expected_rule_code` varchar(100) DEFAULT '' COMMENT '期望规则编号',
  `enabled` char(1) DEFAULT '1' COMMENT '是否启用（1启用 0停用）',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`case_id`),
  KEY `idx_audit_vector_eval_case_workflow` (`workflow_code`),
  KEY `idx_audit_vector_eval_case_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核知识库召回评估样例表';

DELETE FROM `sys_dict_data` WHERE `dict_type` IN ('audit_file_storage_status', 'audit_task_collect_status');
DELETE FROM `sys_dict_type` WHERE `dict_type` IN ('audit_file_storage_status', 'audit_task_collect_status');

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
(2012, '向量化状态', 'audit_file_storage_status', '0', 'admin', NOW(), '审核资源库'),
(2013, '任务文件采集状态', 'audit_task_collect_status', '0', 'admin', NOW(), '审核资源库');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(2404, 1, '等待向量化', 'pending', 'audit_file_storage_status', 'info', 'Y', '0', 'admin', NOW()),
(2408, 2, '待审核', 'reviewing', 'audit_file_storage_status', 'primary', 'N', '0', 'admin', NOW()),
(2405, 3, '解析中', 'parsing', 'audit_file_storage_status', 'primary', 'N', '0', 'admin', NOW()),
(2406, 4, '向量生成中', 'embedding', 'audit_file_storage_status', 'warning', 'N', '0', 'admin', NOW()),
(2402, 5, '已向量化', 'stored', 'audit_file_storage_status', 'success', 'N', '0', 'admin', NOW()),
(2407, 6, '未识别文本', 'text_empty', 'audit_file_storage_status', 'danger', 'N', '0', 'admin', NOW()),
(2403, 7, '向量化失败', 'failed', 'audit_file_storage_status', 'danger', 'N', '0', 'admin', NOW()),
(2411, 1, '归集处理中', 'processing', 'audit_task_collect_status', 'primary', 'N', '0', 'admin', NOW()),
(2412, 2, '已归集', 'archived', 'audit_task_collect_status', 'success', 'Y', '0', 'admin', NOW()),
(2413, 3, '归集失败', 'failed', 'audit_task_collect_status', 'danger', 'N', '0', 'admin', NOW());

DELETE FROM `sys_role_menu` WHERE `menu_id` BETWEEN 2021 AND 2050;
DELETE FROM `sys_menu` WHERE `menu_id` BETWEEN 2021 AND 2050;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(2021, '审核资产库', 0, 10, 'audit-asset', 'Layout', 'AuditAssetRoot', 1, 0, 'M', '0', '0', '', 'audit-asset-root', 'admin', NOW(), '审核资产业务'),
(2022, '审核资源列表', 2021, 1, 'list', 'audit/asset/index', 'AuditAssetList', 1, 0, 'C', '0', '0', 'audit:asset:list', 'table', 'admin', NOW(), '审核资源列表'),
(2023, '审核资源查询', 2022, 1, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:query', '#', 'admin', NOW(), ''),
(2024, '审核资源详情', 2022, 2, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:detail', '#', 'admin', NOW(), ''),
(2025, '审核资源导出', 2022, 3, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:export', '#', 'admin', NOW(), ''),
(2026, '审核资源权限分配', 2022, 4, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:assign', '#', 'admin', NOW(), ''),
(2027, '审核资源删除', 2022, 5, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:remove', '#', 'admin', NOW(), ''),
(2028, '审核资源批量下载', 2022, 6, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:batchDownload', '#', 'admin', NOW(), ''),
(2029, '审核资源一键打包', 2022, 7, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:batchPackage', '#', 'admin', NOW(), ''),
(2030, '审核资源重新上传', 2022, 8, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:reupload', '#', 'admin', NOW(), ''),
(2050, '审核资源审核', 2022, 9, '#', '', '', 1, 0, 'F', '0', '0', 'audit:asset:review', '#', 'admin', NOW(), ''),
(2031, '审核资源库', 0, 11, 'audit-library', 'Layout', 'AuditLibraryRoot', 1, 0, 'M', '0', '0', '', 'audit-library-root', 'admin', NOW(), '审核资源库'),
(2032, '审核文件库', 2031, 1, 'folder', 'audit/library/folder', 'AuditLibraryFolder', 1, 0, 'C', '0', '0', 'audit:library:folder:list', 'tree', 'admin', NOW(), '审核文件库'),
(2033, '审核文件库查询', 2032, 1, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:folder:query', '#', 'admin', NOW(), ''),
(2034, '审核文件库新增', 2032, 2, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:folder:add', '#', 'admin', NOW(), ''),
(2035, '审核文件库修改', 2032, 3, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:folder:edit', '#', 'admin', NOW(), ''),
(2036, '审核文件库删除', 2032, 4, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:folder:remove', '#', 'admin', NOW(), ''),
(2037, '常用文件资源', 2031, 2, 'common', 'audit/library/common', 'AuditLibraryCommon', 1, 0, 'C', '0', '0', 'audit:library:common:list', 'documentation', 'admin', NOW(), '常用文件资源'),
(2038, '常用文件资源查询', 2037, 1, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:common:query', '#', 'admin', NOW(), ''),
(2039, '常用文件资源新增', 2037, 2, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:common:add', '#', 'admin', NOW(), ''),
(2040, '常用文件资源修改', 2037, 3, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:common:edit', '#', 'admin', NOW(), ''),
(2041, '常用文件资源删除', 2037, 4, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:common:remove', '#', 'admin', NOW(), ''),
(2042, '常用文件资源导出', 2037, 5, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:common:export', '#', 'admin', NOW(), ''),
(2043, '常用文件资源归类', 2037, 6, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:common:assignFolder', '#', 'admin', NOW(), ''),
(2044, '任务文件资源', 2031, 3, 'task', 'audit/library/task', 'AuditLibraryTask', 1, 0, 'C', '0', '0', 'audit:library:task:list', 'guide', 'admin', NOW(), '任务文件资源'),
(2045, '任务文件资源查询', 2044, 1, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:task:query', '#', 'admin', NOW(), ''),
(2046, '任务文件资源修改', 2044, 2, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:task:edit', '#', 'admin', NOW(), ''),
(2047, '任务文件资源删除', 2044, 3, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:task:remove', '#', 'admin', NOW(), ''),
(2048, '任务文件资源导出', 2044, 4, '#', '', '', 1, 0, 'F', '0', '0', 'audit:library:task:export', '#', 'admin', NOW(), '');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, t.menu_id FROM (
  SELECT 2021 AS menu_id UNION ALL SELECT 2022 UNION ALL SELECT 2023 UNION ALL SELECT 2024 UNION ALL
  SELECT 2025 UNION ALL SELECT 2026 UNION ALL SELECT 2027 UNION ALL SELECT 2028 UNION ALL
  SELECT 2029 UNION ALL SELECT 2030 UNION ALL SELECT 2050 UNION ALL SELECT 2031 UNION ALL SELECT 2032 UNION ALL
  SELECT 2033 UNION ALL SELECT 2034 UNION ALL SELECT 2035 UNION ALL SELECT 2036 UNION ALL
  SELECT 2037 UNION ALL SELECT 2038 UNION ALL SELECT 2039 UNION ALL SELECT 2040 UNION ALL
  SELECT 2041 UNION ALL SELECT 2042 UNION ALL SELECT 2043 UNION ALL SELECT 2044 UNION ALL
  SELECT 2045 UNION ALL SELECT 2046 UNION ALL SELECT 2047 UNION ALL SELECT 2048
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = t.menu_id
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, t.menu_id FROM (
  SELECT 2021 AS menu_id UNION ALL SELECT 2022 UNION ALL SELECT 2023 UNION ALL SELECT 2024 UNION ALL
  SELECT 2025 UNION ALL SELECT 2026 UNION ALL SELECT 2027 UNION ALL SELECT 2028 UNION ALL
  SELECT 2029 UNION ALL SELECT 2030 UNION ALL SELECT 2050 UNION ALL SELECT 2031 UNION ALL SELECT 2032 UNION ALL
  SELECT 2033 UNION ALL SELECT 2034 UNION ALL SELECT 2035 UNION ALL SELECT 2036 UNION ALL
  SELECT 2037 UNION ALL SELECT 2038 UNION ALL SELECT 2039 UNION ALL SELECT 2040 UNION ALL
  SELECT 2041 UNION ALL SELECT 2042 UNION ALL SELECT 2043 UNION ALL SELECT 2044 UNION ALL
  SELECT 2045 UNION ALL SELECT 2046 UNION ALL SELECT 2047 UNION ALL SELECT 2048
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 2 AND rm.menu_id = t.menu_id
);

-- 审核文件库初始化数据：与 backend/ruoyi-admin/src/main/resources/profile/upload/2026/05/05 下的内置文件对应
INSERT INTO `audit_library_folder`
(`folder_id`, `parent_id`, `folder_name`, `intro`, `visible_scope`, `top_flag`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(2, 0, '文件库一', '', 'all', '0', '0', 'admin', '2026-05-05 20:38:14', 'admin', '2026-05-05 20:38:14', NULL),
(3, 0, '文件库二', '', 'all', '0', '0', 'admin', '2026-05-05 20:38:23', 'admin', '2026-05-05 20:38:23', NULL),
(4, 0, '审核标准库', '', 'all', '0', '0', 'admin', '2026-05-05 20:38:36', 'admin', '2026-05-05 20:38:36', NULL),
(5, 2, '中心资料', '', 'all', '0', '0', 'admin', '2026-05-05 20:55:41', 'admin', '2026-05-05 20:55:41', NULL),
(6, 3, '客户资料', '', 'all', '0', '0', 'admin', '2026-05-05 21:01:02', 'admin', '2026-05-05 21:01:02', NULL),
(7, 3, '中心资料', '', 'all', '0', '0', 'admin', '2026-05-05 21:01:08', 'admin', '2026-05-05 21:01:08', NULL);

INSERT INTO `audit_common_resource`
(`resource_id`, `document_name`, `folder_id`, `folder_name`, `storage_status`, `progress_text`, `creator`, `latest_modify_time`, `file_size`, `file_name`, `file_url`, `current_version_no`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(1, 'GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求', 0, '', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:39:11', '1.31MB', 'GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求_20260505203909A001.docx', '/profile/upload/2026/05/05/GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求_20260505203909A001.docx', 'v1.0', '0', 'admin', '2026-05-05 20:39:10', 'admin', '2026-05-05 20:39:10', NULL),
(2, 'GBT3836.9-2021', 0, '', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:40:13', '456.47KB', 'GBT3836.9-2021_20260505204011A002.docx', '/profile/upload/2026/05/05/GBT3836.9-2021_20260505204011A002.docx', 'v1.0', '0', 'admin', '2026-05-05 20:40:12', 'admin', '2026-05-05 20:40:12', NULL),
(3, 'GBT3836.2-2021', 0, '', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:50:02', '23.90MB', 'GBT3836.2-2021_20260505205001A001.pdf', '/profile/upload/2026/05/05/GBT3836.2-2021_20260505205001A001.pdf', 'v1.0', '0', 'admin', '2026-05-05 20:50:02', 'admin', '2026-05-05 20:50:02', NULL),
(4, '2025520398+其它+技术审查', 5, '中心资料', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:58:31', '58.82KB', '2025520398+其它+技术审查_20260505205830A002.doc', '/profile/upload/2026/05/05/2025520398+其它+技术审查_20260505205830A002.doc', 'v1.0', '0', 'admin', '2026-05-05 20:58:31', 'admin', '2026-05-05 20:58:31', NULL),
(5, '2025520398+其它+技术审查', 5, '中心资料', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:58:41', '39.74KB', '2025520398+其它+技术审查_20260505205839A003.docx', '/profile/upload/2026/05/05/2025520398+其它+技术审查_20260505205839A003.docx', 'v1.0', '0', 'admin', '2026-05-05 20:58:40', 'admin', '2026-05-05 20:58:40', NULL),
(6, '2025520398＋合格证＋CCRI25.2513', 5, '中心资料', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:59:04', '270.13KB', '2025520398＋合格证＋CCRI25.2513_20260505205903A004.doc', '/profile/upload/2026/05/05/2025520398＋合格证＋CCRI25.2513_20260505205903A004.doc', 'v1.0', '0', 'admin', '2026-05-05 20:59:04', 'admin', '2026-05-05 20:59:04', NULL),
(7, '任务单【2025520398】', 2, '文件库一', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:59:17', '363.78KB', '任务单【2025520398】_20260505205916A005.pdf', '/profile/upload/2026/05/05/任务单【2025520398】_20260505205916A005.pdf', 'v1.0', '0', 'admin', '2026-05-05 20:59:17', 'admin', '2026-05-05 20:59:17', NULL),
(8, 'ZDYZ127-Z矿用隔爆兼本安型监控主机企标', 4, '审核标准库', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:59:45', '265.41KB', 'ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205943A006.doc', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205943A006.doc', 'v1.0', '0', 'admin', '2026-05-05 20:59:44', 'admin', '2026-05-05 20:59:44', NULL),
(9, 'ZDYZ127-Z矿用隔爆兼本安型监控主机企标', 4, '审核标准库', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 20:59:54', '136.46KB', 'ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205952A007.docx', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205952A007.docx', 'v1.0', '0', 'admin', '2026-05-05 20:59:53', 'admin', '2026-05-05 20:59:53', NULL),
(10, 'ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表', 4, '审核标准库', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 21:00:05', '22.50KB', 'ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210004A008.doc', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210004A008.doc', 'v1.0', '0', 'admin', '2026-05-05 21:00:05', 'admin', '2026-05-05 21:00:05', NULL),
(11, 'ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表', 4, '审核标准库', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 21:00:15', '15.16KB', 'ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210013A009.docx', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210013A009.docx', 'v1.0', '0', 'admin', '2026-05-05 21:00:15', 'admin', '2026-05-05 21:00:15', NULL),
(12, 'ZDYZ127-Z矿用隔爆兼本安型监控主机说明书', 4, '审核标准库', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 21:00:29', '134.00KB', 'ZDYZ127-Z矿用隔爆兼本安型监控主机说明书_20260505210026A010.doc', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机说明书_20260505210026A010.doc', 'v1.0', '0', 'admin', '2026-05-05 21:00:29', 'admin', '2026-05-05 21:00:29', NULL),
(13, 'KXJ127矿用隔爆兼本安型PLC控制箱企业标准', 6, '客户资料', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 21:01:20', '86.22KB', 'KXJ127矿用隔爆兼本安型PLC控制箱企业标准_20260505210118A011.docx', '/profile/upload/2026/05/05/KXJ127矿用隔爆兼本安型PLC控制箱企业标准_20260505210118A011.docx', 'v1.0', '0', 'admin', '2026-05-05 21:01:19', 'admin', '2026-05-05 21:01:19', NULL),
(14, 'KXJ127矿用隔爆兼本安型PLC控制箱说明书', 6, '客户资料', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 21:01:28', '65.17KB', 'KXJ127矿用隔爆兼本安型PLC控制箱说明书_20260505210126A012.doc', '/profile/upload/2026/05/05/KXJ127矿用隔爆兼本安型PLC控制箱说明书_20260505210126A012.doc', 'v1.0', '0', 'admin', '2026-05-05 21:01:28', 'admin', '2026-05-05 21:01:28', NULL),
(15, '安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）', 7, '中心资料', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 21:01:44', '154.15KB', '安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）_20260505210143A013.pdf', '/profile/upload/2026/05/05/安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）_20260505210143A013.pdf', 'v1.0', '0', 'admin', '2026-05-05 21:01:44', 'admin', '2026-05-05 21:01:44', NULL),
(16, '检测分院实验室业务管理系统', 7, '中心资料', 'pending', '等待向量化任务执行', 'admin', '2026-05-05 21:01:56', '187.58KB', '检测分院实验室业务管理系统_20260505210152A014.pdf', '/profile/upload/2026/05/05/检测分院实验室业务管理系统_20260505210152A014.pdf', 'v1.0', '0', 'admin', '2026-05-05 21:01:55', 'admin', '2026-05-05 21:01:55', NULL);

INSERT INTO `audit_common_resource_version`
(`version_id`, `resource_id`, `version_no`, `file_name`, `file_url`, `file_size`, `creator`, `create_time`)
VALUES
(1, 1, 'v1.0', 'GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求_20260505203909A001.docx', '/profile/upload/2026/05/05/GBT3836.1-2021 爆炸性环境 第1部分： 设备 通用要求_20260505203909A001.docx', '1.31MB', 'admin', '2026-05-05 20:39:11'),
(2, 2, 'v1.0', 'GBT3836.9-2021_20260505204011A002.docx', '/profile/upload/2026/05/05/GBT3836.9-2021_20260505204011A002.docx', '456.47KB', 'admin', '2026-05-05 20:40:13'),
(3, 3, 'v1.0', 'GBT3836.2-2021_20260505205001A001.pdf', '/profile/upload/2026/05/05/GBT3836.2-2021_20260505205001A001.pdf', '23.90MB', 'admin', '2026-05-05 20:50:02'),
(4, 4, 'v1.0', '2025520398+其它+技术审查_20260505205830A002.doc', '/profile/upload/2026/05/05/2025520398+其它+技术审查_20260505205830A002.doc', '58.82KB', 'admin', '2026-05-05 20:58:31'),
(5, 5, 'v1.0', '2025520398+其它+技术审查_20260505205839A003.docx', '/profile/upload/2026/05/05/2025520398+其它+技术审查_20260505205839A003.docx', '39.74KB', 'admin', '2026-05-05 20:58:41'),
(6, 6, 'v1.0', '2025520398＋合格证＋CCRI25.2513_20260505205903A004.doc', '/profile/upload/2026/05/05/2025520398＋合格证＋CCRI25.2513_20260505205903A004.doc', '270.13KB', 'admin', '2026-05-05 20:59:04'),
(7, 7, 'v1.0', '任务单【2025520398】_20260505205916A005.pdf', '/profile/upload/2026/05/05/任务单【2025520398】_20260505205916A005.pdf', '363.78KB', 'admin', '2026-05-05 20:59:17'),
(8, 8, 'v1.0', 'ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205943A006.doc', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205943A006.doc', '265.41KB', 'admin', '2026-05-05 20:59:45'),
(9, 9, 'v1.0', 'ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205952A007.docx', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机企标_20260505205952A007.docx', '136.46KB', 'admin', '2026-05-05 20:59:54'),
(10, 10, 'v1.0', 'ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210004A008.doc', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210004A008.doc', '22.50KB', 'admin', '2026-05-05 21:00:05'),
(11, 11, 'v1.0', 'ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210013A009.docx', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机受控元件明细表_20260505210013A009.docx', '15.16KB', 'admin', '2026-05-05 21:00:15'),
(12, 12, 'v1.0', 'ZDYZ127-Z矿用隔爆兼本安型监控主机说明书_20260505210026A010.doc', '/profile/upload/2026/05/05/ZDYZ127-Z矿用隔爆兼本安型监控主机说明书_20260505210026A010.doc', '134.00KB', 'admin', '2026-05-05 21:00:29'),
(13, 13, 'v1.0', 'KXJ127矿用隔爆兼本安型PLC控制箱企业标准_20260505210118A011.docx', '/profile/upload/2026/05/05/KXJ127矿用隔爆兼本安型PLC控制箱企业标准_20260505210118A011.docx', '86.22KB', 'admin', '2026-05-05 21:01:20'),
(14, 14, 'v1.0', 'KXJ127矿用隔爆兼本安型PLC控制箱说明书_20260505210126A012.doc', '/profile/upload/2026/05/05/KXJ127矿用隔爆兼本安型PLC控制箱说明书_20260505210126A012.doc', '65.17KB', 'admin', '2026-05-05 21:01:28'),
(15, 15, 'v1.0', '安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）_20260505210143A013.pdf', '/profile/upload/2026/05/05/安标国家矿用产品安全标志中心（矿用产品安全标志技术审查与产品检验委托书）_20260505210143A013.pdf', '154.15KB', 'admin', '2026-05-05 21:01:44'),
(16, 16, 'v1.0', '检测分院实验室业务管理系统_20260505210152A014.pdf', '/profile/upload/2026/05/05/检测分院实验室业务管理系统_20260505210152A014.pdf', '187.58KB', 'admin', '2026-05-05 21:01:56');

INSERT INTO `audit_asset_record`
(`asset_id`, `review_task_id`, `task_no`, `product_name`, `delivery_unit`, `submitter`, `reviewer`, `permission_owner`,
 `ai_analysis_count`, `current_ai_version`, `review_status`, `review_time`, `report_file_name`, `report_file_url`,
 `ai_opinion`, `final_opinion`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(1, 1, 'SF-16542598454', '产品名称1', '送检单位1', '提交人1', '审核人1', '审核人1', 3, '版本3', 'approved',
 '2025-06-18 15:23:23', '煤科院煤炭产品质量检测报告.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf',
 '本次报告经AI核查，发现存在内容缺失、格式错误两类问题，具体已汇总如下，对应报告中相关标注位置，便于整改完善。',
 '格式正确，给予审核通过。', '0', 'admin', NOW(), 'admin', NOW(), '审核通过'),
(2, 1, 'SF-16542598455', '产品名称1', '送检单位1', '提交人1', '审核人2', '审核人2', 3, '版本3', 'approved',
 '2025-06-18 15:23:23', '煤科院煤炭产品质量检测报告.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf',
 'AI核查已完成，内容符合提交规范。', '格式正确，给予审核通过。', '0', 'admin', NOW(), 'admin', NOW(), '审核通过'),
(3, 1, 'SF-16542598456', '产品名称1', '送检单位1', '提交人1', '审核人1', '审核人1', 3, '版本3', 'returned',
 '2025-06-18 15:23:23', '煤科院煤炭产品质量检测报告.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf',
 '当前版本存在多处待确认问题，需要重新上传。', '请根据 AI 标注结果重新上传。', '0', 'admin', NOW(), 'admin', NOW(), '驳回'),
(4, 1, 'SF-16542598457', '产品名称1', '送检单位1', '提交人1', '审核人2', '审核人2', 3, '版本2', 'returned',
 '2025-06-18 15:23:23', '煤科院煤炭产品质量检测报告.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf',
 'AI已初步通过，但人工审核要求补充说明。', '请补充证明材料后再次提交。', '0', 'admin', NOW(), 'admin', NOW(), '驳回');

INSERT INTO `audit_asset_ai_version`
(`version_id`, `asset_id`, `version_no`, `word_count_text`, `current_flag`, `create_time`)
VALUES
(1, 1, '版本1', '单次AI共计审核8千字', '0', '2025-06-24 15:26:23'),
(2, 1, '版本2', '单次AI共计审核9千字', '0', '2025-06-24 15:27:23'),
(3, 1, '版本3', '单次AI共计审核1w字', '1', '2025-06-24 15:29:23'),
(4, 2, '版本3', '单次AI共计审核1w字', '1', '2025-06-24 15:29:23'),
(5, 3, '版本3', '单次AI共计审核1w字', '1', '2025-06-24 15:29:23'),
(6, 4, '版本2', '单次AI共计审核9千字', '1', '2025-06-24 15:29:23');

INSERT INTO `audit_asset_ai_step`
(`step_id`, `version_id`, `step_no`, `step_title`, `step_content`, `step_time`, `sort_num`)
VALUES
(1, 3, 1, '排队等待AI分析', '当前队列共计30个，该条位于队伍第三位', '2025-06-24 15:29:23', 1),
(2, 3, 2, 'AI正在解析审核', 'AI解析进度100%，已全部解析完成', '2025-06-24 15:30:23', 2),
(3, 3, 3, 'AI审核初步通过', 'AI已初步通过审核', '2025-06-24 15:32:23', 3),
(4, 4, 1, '排队等待AI分析', '当前队列共计30个，该条位于队伍第三位', '2025-06-24 15:29:23', 1),
(5, 4, 2, 'AI正在解析审核', 'AI解析进度100%，已全部解析完成', '2025-06-24 15:30:23', 2),
(6, 4, 3, 'AI审核初步通过', 'AI已初步通过审核', '2025-06-24 15:32:23', 3),
(7, 5, 1, '排队等待AI分析', '当前队列共计30个，该条位于队伍第三位', '2025-06-24 15:29:23', 1),
(8, 5, 2, 'AI正在解析审核', 'AI解析进度100%，已全部解析完成', '2025-06-24 15:30:23', 2),
(9, 5, 3, '待修改', 'AI检测出问题1,2,3', '2025-06-24 15:32:23', 3),
(10, 6, 1, '排队等待AI分析', '当前队列共计30个，该条位于队伍第三位', '2025-06-24 15:29:23', 1),
(11, 6, 2, 'AI正在解析审核', 'AI解析进度100%，已全部解析完成', '2025-06-24 15:30:23', 2),
(12, 6, 3, '待修改', 'AI检测出问题1,2', '2025-06-24 15:32:23', 3);

INSERT INTO `audit_asset_resubmit_record`
(`record_id`, `asset_id`, `version_no`, `submitter`, `submit_time`, `file_name`, `file_url`, `image_urls`, `sort_num`)
VALUES
(1, 1, 'v1.0版本', '提交人1', '2025-06-28 15:38:24', '防爆电机检验报告 V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '', 1),
(2, 1, 'v2.0版本', '提交人1', '2025-06-28 15:38:24', '防爆电机检验报告 V2.0.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf', '', 2),
(3, 3, 'v1.0版本', '提交人1', '2025-06-29 09:10:00', '防爆电机检验报告 V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '', 1),
(4, 4, 'v1.0版本', '提交人1', '2025-06-29 11:18:00', '防爆电机检验报告 V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '', 1);
