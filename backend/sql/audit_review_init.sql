SET NAMES utf8mb4;

DROP TABLE IF EXISTS `audit_review_stage`;
DROP TABLE IF EXISTS `audit_review_issue`;
DROP TABLE IF EXISTS `audit_review_version`;
DROP TABLE IF EXISTS `audit_review_task`;

CREATE TABLE `audit_review_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务主键',
  `task_no` varchar(64) NOT NULL COMMENT '任务编号',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `delivery_unit` varchar(100) NOT NULL COMMENT '送检单位',
  `sponsor` varchar(64) DEFAULT '' COMMENT '发起人',
  `handler_name` varchar(64) DEFAULT '' COMMENT '经办人',
  `priority` varchar(20) DEFAULT 'medium' COMMENT '优先级',
  `ai_analysis_count` int DEFAULT '0' COMMENT 'AI分析次数',
  `task_status` varchar(20) DEFAULT 'uploaded' COMMENT '任务状态',
  `review_status` varchar(20) DEFAULT 'reviewing' COMMENT '审核状态',
  `process_flag` char(1) DEFAULT '0' COMMENT '处理状态（0正常 1暂停）',
  `current_version_no` varchar(20) DEFAULT 'v1.0' COMMENT '当前版本号',
  `main_report_urls` text COMMENT '主报告文件',
  `basis_file_urls` text COMMENT '依据文件',
  `appendix_file_urls` text COMMENT '补充附件',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记（0存在 2删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`task_id`),
  KEY `idx_audit_review_task_no` (`task_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核列表任务表';

CREATE TABLE `audit_review_version` (
  `version_id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本主键',
  `task_id` bigint NOT NULL COMMENT '任务主键',
  `version_no` varchar(20) NOT NULL COMMENT '版本号',
  `report_file_name` varchar(255) DEFAULT '' COMMENT '报告文件名称',
  `report_file_url` varchar(500) DEFAULT '' COMMENT '报告文件地址',
  `main_report_urls` text COMMENT '主报告文件',
  `basis_file_urls` text COMMENT '依据文件',
  `appendix_file_urls` text COMMENT '补充附件',
  `detect_status` varchar(20) DEFAULT 'uploaded' COMMENT '检测状态',
  `submitter` varchar(64) DEFAULT '' COMMENT '提交人',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `ai_summary` text COMMENT 'AI分析观点',
  `review_opinion` text COMMENT '审核意见',
  `current_flag` char(1) DEFAULT '0' COMMENT '是否当前版本（0否 1是）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  KEY `idx_audit_review_version_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核任务版本表';

CREATE TABLE `audit_review_issue` (
  `issue_id` bigint NOT NULL AUTO_INCREMENT COMMENT '问题主键',
  `version_id` bigint NOT NULL COMMENT '版本主键',
  `issue_type` varchar(50) DEFAULT '' COMMENT '问题类型',
  `issue_title` varchar(100) DEFAULT '' COMMENT '问题标题',
  `issue_content` varchar(1000) DEFAULT '' COMMENT '问题内容',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`issue_id`),
  KEY `idx_audit_review_issue_version` (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核问题表';

CREATE TABLE `audit_review_stage` (
  `stage_id` bigint NOT NULL AUTO_INCREMENT COMMENT '阶段主键',
  `version_id` bigint NOT NULL COMMENT '版本主键',
  `stage_code` varchar(30) DEFAULT '' COMMENT '阶段编码',
  `stage_name` varchar(50) DEFAULT '' COMMENT '阶段名称',
  `stage_status` char(1) DEFAULT '1' COMMENT '阶段状态（0未完成 1完成）',
  `stage_time` datetime DEFAULT NULL COMMENT '阶段时间',
  `stage_summary` varchar(255) DEFAULT '' COMMENT '阶段摘要',
  `stage_detail` text COMMENT '阶段详情',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  PRIMARY KEY (`stage_id`),
  KEY `idx_audit_review_stage_version` (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核流转阶段表';

DELETE FROM `sys_dict_data` WHERE `dict_type` IN ('audit_review_priority', 'audit_review_task_status', 'audit_review_status');
DELETE FROM `sys_dict_type` WHERE `dict_type` IN ('audit_review_priority', 'audit_review_task_status', 'audit_review_status');

INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
(2001, '审核优先级', 'audit_review_priority', '0', 'admin', NOW(), '审核列表管理'),
(2002, '审核任务状态', 'audit_review_task_status', '0', 'admin', NOW(), '审核列表管理'),
(2003, '审核状态', 'audit_review_status', '0', 'admin', NOW(), '审核列表管理');

INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(2101, 1, '高优先级', 'high', 'audit_review_priority', 'danger', 'N', '0', 'admin', NOW()),
(2102, 2, '中优先级', 'medium', 'audit_review_priority', 'warning', 'Y', '0', 'admin', NOW()),
(2103, 3, '低优先级', 'low', 'audit_review_priority', 'info', 'N', '0', 'admin', NOW()),
(2201, 1, '已上传', 'uploaded', 'audit_review_task_status', 'warning', 'N', '0', 'admin', NOW()),
(2202, 2, '已解析', 'parsed', 'audit_review_task_status', 'primary', 'N', '0', 'admin', NOW()),
(2203, 3, '已检测', 'detected', 'audit_review_task_status', 'success', 'N', '0', 'admin', NOW()),
(2204, 4, '已暂停', 'paused', 'audit_review_task_status', 'info', 'N', '0', 'admin', NOW()),
(2301, 1, '审核通过', 'approved', 'audit_review_status', 'success', 'N', '0', 'admin', NOW()),
(2304, 2, '审核中', 'reviewing', 'audit_review_status', 'primary', 'Y', '0', 'admin', NOW()),
(2302, 3, '待修改', 'pending', 'audit_review_status', 'warning', 'N', '0', 'admin', NOW()),
(2303, 4, '驳回', 'returned', 'audit_review_status', 'danger', 'N', '0', 'admin', NOW());

DELETE FROM `sys_role_menu` WHERE `menu_id` BETWEEN 2001 AND 2010;
DELETE FROM `sys_menu` WHERE `menu_id` BETWEEN 2001 AND 2010;

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
(2001, '审核列表管理', 0, 8, 'audit', 'Layout', 'AuditRoot', 1, 0, 'M', '0', '0', '', 'audit-review-root', 'admin', NOW(), '审核业务模块'),
(2002, '审核列表', 2001, 1, 'review', 'audit/review/index', 'AuditReview', 1, 0, 'C', '0', '0', 'audit:review:list', 'list', 'admin', NOW(), '审核列表管理'),
(2003, '审核列表查询', 2002, 1, '#', '', '', 1, 0, 'F', '0', '0', 'audit:review:query', '#', 'admin', NOW(), ''),
(2004, '审核列表新增', 2002, 2, '#', '', '', 1, 0, 'F', '0', '0', 'audit:review:add', '#', 'admin', NOW(), ''),
(2005, '审核列表修改', 2002, 3, '#', '', '', 1, 0, 'F', '0', '0', 'audit:review:edit', '#', 'admin', NOW(), ''),
(2006, '审核列表删除', 2002, 4, '#', '', '', 1, 0, 'F', '0', '0', 'audit:review:remove', '#', 'admin', NOW(), ''),
(2007, '审核列表导出', 2002, 5, '#', '', '', 1, 0, 'F', '0', '0', 'audit:review:export', '#', 'admin', NOW(), ''),
(2008, '审核详情查看', 2002, 6, '#', '', '', 1, 0, 'F', '0', '0', 'audit:review:detail', '#', 'admin', NOW(), ''),
(2009, '审核版本追溯', 2002, 7, '#', '', '', 1, 0, 'F', '0', '0', 'audit:review:history', '#', 'admin', NOW(), ''),
(2010, '审核状态切换', 2002, 8, '#', '', '', 1, 0, 'F', '0', '0', 'audit:review:changeStatus', '#', 'admin', NOW(), '');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, t.menu_id FROM (
  SELECT 2001 AS menu_id UNION ALL SELECT 2002 UNION ALL SELECT 2003 UNION ALL SELECT 2004 UNION ALL
  SELECT 2005 UNION ALL SELECT 2006 UNION ALL SELECT 2007 UNION ALL SELECT 2008 UNION ALL
  SELECT 2009 UNION ALL SELECT 2010
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 1 AND rm.menu_id = t.menu_id
);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, t.menu_id FROM (
  SELECT 2001 AS menu_id UNION ALL SELECT 2002 UNION ALL SELECT 2003 UNION ALL SELECT 2004 UNION ALL
  SELECT 2005 UNION ALL SELECT 2006 UNION ALL SELECT 2007 UNION ALL SELECT 2008 UNION ALL
  SELECT 2009 UNION ALL SELECT 2010
) t
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` rm WHERE rm.role_id = 2 AND rm.menu_id = t.menu_id
);

INSERT INTO `audit_review_task`
(`task_id`, `task_no`, `product_name`, `delivery_unit`, `sponsor`, `handler_name`, `priority`, `ai_analysis_count`, `task_status`, `review_status`, `process_flag`, `current_version_no`, `main_report_urls`, `basis_file_urls`, `appendix_file_urls`, `submit_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES
(1, 'SF-16542598454', '产品名称1', '送检单位1', '发起人1', '经办人1', 'high', 3, 'uploaded', 'approved', '0', 'v2.0', '/profile/audit/review/防爆电机检验报告_V2.0.pdf', '/profile/audit/review/检测依据文件_A.pdf,/profile/audit/review/检测依据文件_B.pdf', '/profile/audit/review/补充说明附件_A.pdf', '2025-12-28 06:28:34', 'admin', '2025-12-28 06:28:34', 'admin', '2025-12-28 06:28:34', '当前版本已审核通过'),
(2, 'SF-16542598455', '产品名称1', '送检单位1', '发起人1', '经办人1', 'high', 3, 'parsed', 'approved', '0', 'v1.0', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/检测依据文件_A.pdf', '/profile/audit/review/补充说明附件_A.pdf', '2025-12-28 06:28:34', 'admin', '2025-12-28 06:28:34', 'admin', '2025-12-28 06:28:34', '解析完成待归档'),
(3, 'SF-16542598456', '产品名称1', '送检单位1', '发起人1', '经办人1', 'medium', 3, 'detected', 'approved', '0', 'v1.0', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/检测依据文件_A.pdf', '/profile/audit/review/补充说明附件_A.pdf', '2025-12-28 06:28:34', 'admin', '2025-12-28 06:28:34', 'admin', '2025-12-28 06:28:34', '检测完成审核通过'),
(4, 'SF-16542598457', '产品名称1', '送检单位1', '发起人1', '经办人1', 'low', 3, 'detected', 'returned', '1', 'v1.0', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/检测依据文件_A.pdf', '/profile/audit/review/补充说明附件_A.pdf', '2025-12-28 06:28:34', 'admin', '2025-12-28 06:28:34', 'admin', '2025-12-28 06:28:34', '检测后驳回');

INSERT INTO `audit_review_version`
(`version_id`, `task_id`, `version_no`, `report_file_name`, `report_file_url`, `main_report_urls`, `basis_file_urls`, `appendix_file_urls`, `detect_status`, `submitter`, `submit_time`, `ai_summary`, `review_opinion`, `current_flag`, `create_by`, `create_time`)
VALUES
(1, 1, 'v1.0', '防爆电机检验报告 V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/检测依据文件_A.pdf', '/profile/audit/review/补充说明附件_A.pdf', 'parsed', '发起人1', '2025-12-28 05:58:34', '初版报告已完成解析，共识别 2 类待确认问题。', '建议补充说明后继续审核。', '0', 'admin', '2025-12-28 05:58:34'),
(2, 1, 'v2.0', '防爆电机检验报告 V2.0.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf', '/profile/audit/review/防爆电机检验报告_V2.0.pdf', '/profile/audit/review/检测依据文件_A.pdf,/profile/audit/review/检测依据文件_B.pdf', '/profile/audit/review/补充说明附件_A.pdf', 'uploaded', '发起人1', '2025-12-28 06:28:34', '本次报告经 AI 核查，发现存在内容缺失、格式错误两类问题。', '格式正确，给予审核通过。', '1', 'admin', '2025-12-28 06:28:34'),
(3, 2, 'v1.0', '防爆电机检验报告 V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/检测依据文件_A.pdf', '/profile/audit/review/补充说明附件_A.pdf', 'parsed', '发起人1', '2025-12-28 06:28:34', '文本解析智能体已完成处理。', '解析完成，等待人工确认。', '1', 'admin', '2025-12-28 06:28:34'),
(4, 3, 'v1.0', '防爆电机检验报告 V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/检测依据文件_A.pdf', '/profile/audit/review/补充说明附件_A.pdf', 'detected', '发起人1', '2025-12-28 06:28:34', '检测结果智能体已发现 2 处异常。', '确认问题已修复，审核通过。', '1', 'admin', '2025-12-28 06:28:34'),
(5, 4, 'v1.0', '防爆电机检验报告 V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/防爆电机检验报告_V1.0.pdf', '/profile/audit/review/检测依据文件_A.pdf', '/profile/audit/review/补充说明附件_A.pdf', 'detected', '发起人1', '2025-12-28 06:28:34', 'AI 已标记 3 处需要修订的内容。', '请根据提示修改后重新提交。', '1', 'admin', '2025-12-28 06:28:34');

INSERT INTO `audit_review_issue` (`issue_id`, `version_id`, `issue_type`, `issue_title`, `issue_content`, `sort_num`) VALUES
(1, 2, '数据错误', '识别异常类型：数据错误', '报告第 3 页表 3-1 中“防爆等级”填写为 “Exd II BT4”，与依据文件（GB 3836.1-2021）要求的 “Exd II CT6” 不一致。', 1),
(2, 2, '格式不规范', '识别异常类型：格式不规范', '报告第 10 页“检测人员签字”栏位未填写，且缺少审核人员签字确认。', 2),
(3, 4, '数据错误', '识别异常类型：数据错误', '部分检测结果与标准值存在偏差，建议人工复核。', 1),
(4, 5, '待修改', '识别异常类型：待修改', 'AI 检测出问题 1、2、3，请重新上传修订版。', 1);

INSERT INTO `audit_review_stage` (`stage_id`, `version_id`, `stage_code`, `stage_name`, `stage_status`, `stage_time`, `stage_summary`, `stage_detail`, `sort_num`) VALUES
(1, 2, 'upload', '报告上传', '1', '2024-06-01 14:35:36', '对应智能体-文件校验智能体', '① 格式校验：检测文件为 PDF，符合要求；② 大小校验：文件大小 2.3MB，未超过 5MB 限制；③ 存储校验：已成功存入本地资源目录。', 1),
(2, 2, 'parse', '报告解析', '1', '2024-06-01 14:37:38', '对应智能体-预处理智能体', '① 格式转换：已将 PDF 转为结构化格式；② 字段提取：提取 12 个核心字段；③ 摘要生成：已生成报告摘要，字数 120 字。', 2),
(3, 2, 'detect', '报告检测', '1', '2024-06-01 14:39:40', '对应智能体：比对智能体 + 检测结果智能体', '① 比对智能体：已完成报告与依据文件的 10 个字段比对，发现 2 处不一致；② 检测结果智能体：识别异常类型“数据错误 + 格式不规范”。', 3),
(4, 3, 'upload', '报告上传', '1', '2024-06-01 14:35:36', '对应智能体-文件校验智能体', '报告上传完成，进入文本解析阶段。', 1),
(5, 3, 'parse', '报告解析', '1', '2024-06-01 14:37:38', '对应智能体-预处理智能体', '文本解析智能体处理完成，已生成结构化结果。', 2),
(6, 3, 'detect', '报告检测', '1', '2024-06-01 14:39:40', '对应智能体-检测结果智能体', '检测阶段完成，等待人工确认。', 3),
(7, 4, 'upload', '报告上传', '1', '2024-06-01 14:35:36', '对应智能体-文件校验智能体', '文件校验通过。', 1),
(8, 4, 'parse', '报告解析', '1', '2024-06-01 14:37:38', '对应智能体-预处理智能体', '解析完成并生成摘要。', 2),
(9, 4, 'detect', '报告检测', '1', '2024-06-01 14:39:40', '对应智能体-检测结果智能体', '已完成检测，结果建议通过。', 3),
(10, 5, 'upload', '报告上传', '1', '2024-06-01 14:35:36', '对应智能体-文件校验智能体', '文件校验通过。', 1),
(11, 5, 'parse', '报告解析', '1', '2024-06-01 14:37:38', '对应智能体-预处理智能体', '解析完成并生成结构化结果。', 2),
(12, 5, 'detect', '报告检测', '1', '2024-06-01 14:39:40', '对应智能体-检测结果智能体', '检测完成，当前版本已驳回。', 3);
