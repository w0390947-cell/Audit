SET NAMES utf8mb4;

DROP TABLE IF EXISTS `audit_ai_finding`;
DROP TABLE IF EXISTS `audit_ai_task`;

CREATE TABLE `audit_ai_task` (
  `ai_task_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'AI任务主键',
  `review_task_id` bigint DEFAULT NULL COMMENT '审核任务主键',
  `review_version_id` bigint DEFAULT NULL COMMENT '审核任务版本主键',
  `task_no` varchar(64) NOT NULL COMMENT '任务编号',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `delivery_unit` varchar(100) DEFAULT '' COMMENT '送检单位',
  `submitter` varchar(64) DEFAULT '' COMMENT '提交人',
  `priority` varchar(20) DEFAULT 'medium' COMMENT '优先级',
  `queue_position` int DEFAULT '0' COMMENT '队列位置',
  `task_status` varchar(20) DEFAULT 'waiting' COMMENT '任务状态',
  `estimated_duration` varchar(32) DEFAULT '' COMMENT '预计执行时间',
  `progress_percent` int DEFAULT '0' COMMENT '进度百分比',
  `progress_text` varchar(255) DEFAULT '' COMMENT '进度文案',
  `ai_analysis_count` int DEFAULT '0' COMMENT 'AI分析次数',
  `review_status` varchar(20) DEFAULT 'reviewing' COMMENT '审核状态',
  `report_file_name` varchar(255) DEFAULT '' COMMENT '报告文件名',
  `report_file_url` varchar(500) DEFAULT '' COMMENT '报告文件地址',
  `ai_summary` text COMMENT 'AI总结',
  `review_opinion` text COMMENT '审核意见',
  `reviewer` varchar(64) DEFAULT '' COMMENT '审核人',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`ai_task_id`),
  KEY `idx_audit_ai_task_no` (`task_no`),
  KEY `idx_audit_ai_review_version` (`review_task_id`, `review_version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI任务队列表';

CREATE TABLE `audit_ai_finding` (
  `finding_id` bigint NOT NULL AUTO_INCREMENT COMMENT '发现项主键',
  `ai_task_id` bigint NOT NULL COMMENT 'AI任务主键',
  `finding_type` varchar(50) DEFAULT '' COMMENT '发现类型',
  `finding_title` varchar(100) DEFAULT '' COMMENT '发现标题',
  `finding_content` varchar(1000) DEFAULT '' COMMENT '发现内容',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`finding_id`),
  KEY `idx_audit_ai_finding_task` (`ai_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI审核发现项';

DELETE FROM `sys_dict_data` WHERE `dict_type` IN ('audit_ai_task_status');
DELETE FROM `sys_dict_type` WHERE `dict_type` IN ('audit_ai_task_status');

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
(2011, 'AI任务状态', 'audit_ai_task_status', '0', 'admin', NOW(), 'AI审核管理');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(2311, 1, '执行中', 'executing', 'audit_ai_task_status', 'primary', 'N', '0', 'admin', NOW()),
(2312, 2, '等待中', 'waiting', 'audit_ai_task_status', 'warning', 'Y', '0', 'admin', NOW()),
(2313, 3, '已暂停', 'paused', 'audit_ai_task_status', 'info', 'N', '0', 'admin', NOW()),
(2314, 4, '已完成', 'completed', 'audit_ai_task_status', 'success', 'N', '0', 'admin', NOW());

DELETE FROM `sys_role_menu` WHERE `menu_id` BETWEEN 2011 AND 2020;
DELETE FROM `sys_menu` WHERE `menu_id` BETWEEN 2011 AND 2020;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(2011, 'AI审核管理', 0, 9, 'audit-ai', 'Layout', 'AuditAiRoot', 1, 0, 'M', '0', '0', '', 'audit-ai-root', 'admin', NOW(), 'AI审核业务'),
(2012, 'AI任务队列', 2011, 1, 'queue', 'audit/ai/index', 'AuditAiQueue', 1, 0, 'C', '0', '0', 'audit:ai:list', 'education', 'admin', NOW(), 'AI任务队列'),
(2013, 'AI任务查询', 2012, 1, '#', '', '', 1, 0, 'F', '0', '0', 'audit:ai:query', '#', 'admin', NOW(), ''),
(2014, 'AI任务详情', 2012, 2, '#', '', '', 1, 0, 'F', '0', '0', 'audit:ai:detail', '#', 'admin', NOW(), ''),
(2015, 'AI任务导出', 2012, 3, '#', '', '', 1, 0, 'F', '0', '0', 'audit:ai:export', '#', 'admin', NOW(), ''),
(2016, 'AI任务状态切换', 2012, 4, '#', '', '', 1, 0, 'F', '0', '0', 'audit:ai:changeStatus', '#', 'admin', NOW(), ''),
(2017, 'AI任务提升优先级', 2012, 5, '#', '', '', 1, 0, 'F', '0', '0', 'audit:ai:raisePriority', '#', 'admin', NOW(), ''),
(2018, 'AI任务人工审核', 2012, 6, '#', '', '', 1, 0, 'F', '0', '0', 'audit:ai:review', '#', 'admin', NOW(), ''),
(2019, 'AI任务删除', 2012, 7, '#', '', '', 1, 0, 'F', '0', '0', 'audit:ai:remove', '#', 'admin', NOW(), '');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, t.menu_id FROM (
  SELECT 2011 AS menu_id UNION ALL SELECT 2012 UNION ALL SELECT 2013 UNION ALL SELECT 2014 UNION ALL
  SELECT 2015 UNION ALL SELECT 2016 UNION ALL SELECT 2017 UNION ALL SELECT 2018 UNION ALL
  SELECT 2019
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = t.menu_id
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, t.menu_id FROM (
  SELECT 2011 AS menu_id UNION ALL SELECT 2012 UNION ALL SELECT 2013 UNION ALL SELECT 2014 UNION ALL
  SELECT 2015 UNION ALL SELECT 2016 UNION ALL SELECT 2017 UNION ALL SELECT 2018 UNION ALL
  SELECT 2019
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 2 AND rm.menu_id = t.menu_id
);

INSERT INTO `audit_ai_task`
(`ai_task_id`, `review_task_id`, `review_version_id`, `task_no`, `product_name`, `delivery_unit`, `submitter`, `priority`, `queue_position`,
 `task_status`, `estimated_duration`, `progress_percent`, `progress_text`, `ai_analysis_count`, `review_status`,
 `report_file_name`, `report_file_url`, `ai_summary`, `review_opinion`, `reviewer`, `submit_time`, `create_by`,
 `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(1, 1, 2, 'SF-16542598454', '产品名称1', '送检单位1', '提交人1', 'high', 1, 'executing', '3分钟', 62, '智能体等待处理', 3, 'pending',
 '煤科院煤炭产品质量检测报告.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf',
 '本次报告经 AI 核查，发现存在内容缺失、格式错误两类问题，具体已汇总如下，对应报告中相关标注位置，便于整改完善。',
 '', '', '2025-06-18 13:35:00', 'admin', NOW(), 'admin', NOW(), '执行中的高优先级任务'),
(2, 2, 3, 'SF-16542598455', '产品名称1', '送检单位1', '提交人1', 'high', 2, 'executing', '3分钟', 44, '文本解析智能体处理中', 3, 'pending',
 '煤科院煤炭产品质量检测报告.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf',
 '文本解析智能体已完成段落抽取，正在输出结构化结果。',
 '', '', '2025-06-18 13:36:00', 'admin', NOW(), 'admin', NOW(), '正在解析的任务'),
(3, 3, 4, 'SF-16542598456', '产品名称1', '送检单位1', '提交人1', 'medium', 3, 'waiting', '3分钟', 18, '智能体等待处理', 3, 'pending',
 '煤科院煤炭产品质量检测报告.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf',
 '当前任务正在排队，预计在完成前序高优先级任务后自动启动。',
 '', '', '2025-06-18 13:37:00', 'admin', NOW(), 'admin', NOW(), '等待中的任务'),
(4, 4, 5, 'SF-16542598457', '产品名称1', '送检单位1', '提交人1', 'low', 4, 'paused', '3分钟', 6, '任务已暂停，等待恢复', 3, 'pending',
 '煤科院煤炭产品质量检测报告.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf',
 '低优先级任务已被暂缓处理，待人工恢复后重新进入队列。',
 '', '', '2025-06-18 13:38:00', 'admin', NOW(), 'admin', NOW(), '暂停中的任务');

INSERT INTO `audit_ai_finding`
(`finding_id`, `ai_task_id`, `finding_type`, `finding_title`, `finding_content`, `sort_num`)
VALUES
(1, 1, '内容缺失', '内容缺失：委托单位联系人及联系方式未填写', '本次报告经AI核查，发现存在内容缺失、格式错误两类问题，具体已汇总如下，对应报告中相关标注位置，便于整改完善。', 1),
(2, 1, '格式错误', '格式错误：检测项目及结果未按规范设置表格标题', '本次报告经AI核查，发现存在内容缺失、格式错误两类问题，具体已汇总如下，对应报告中相关标注位置，便于整改完善。', 2),
(3, 2, '解析摘要', '文本结构化处理进度', '文本解析智能体已完成段落拆分，正在生成标准字段映射。', 1),
(4, 3, '排队提示', '等待执行', '当前队列共计3个任务，该条任务位于队列第3位。', 1),
(5, 4, '暂停提示', '任务已暂停', '当前任务已暂停，如需重新参与AI审核请执行恢复操作。', 1);

-- ============================================================
-- FastGPT 集成相关菜单和权限（阶段 4）
-- ============================================================

-- AI 任务重新分析按钮权限
INSERT INTO `sys_menu`
(`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`,
 `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,
 `create_by`, `create_time`, `remark`)
VALUES
(2020, 'AI任务重新分析', 2012, 8, '#', '', '', 1, 0, 'F', '0', '0',
 'audit:ai:analyze', '#', 'admin', NOW(), '手动触发AI分析按钮权限')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 同步给管理员角色分配权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 2020
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = 2020
);

-- ============================================================
-- FastGPT 定时任务初始化（阶段 4）
-- ============================================================

-- AI 报告分析定时任务
-- 注意：初始 status='0' 表示正常，初始化后自动启用定时分析任务
INSERT INTO `sys_job`
(`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`,
 `concurrent`, `status`, `create_by`, `create_time`, `remark`)
VALUES
(20, 'AI报告分析任务', 'DEFAULT', 'auditAiAnalysisTask.run()', '0 */1 * * * ?',
 '3', '1', '0', 'admin', NOW(), '扫描等待中的AI审核任务并调用FastGPT分析')
ON DUPLICATE KEY UPDATE job_name = VALUES(job_name), invoke_target = VALUES(invoke_target);
