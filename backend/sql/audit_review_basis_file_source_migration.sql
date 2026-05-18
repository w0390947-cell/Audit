SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `audit_review_basis_file` (
  `basis_id` bigint NOT NULL AUTO_INCREMENT COMMENT '依据文件明细主键',
  `task_id` bigint NOT NULL COMMENT '审核任务ID',
  `version_id` bigint NOT NULL COMMENT '审核任务版本ID',
  `source_type` varchar(20) NOT NULL DEFAULT 'uploaded' COMMENT '来源类型：uploaded本地上传 library已有文件库',
  `library_resource_id` bigint DEFAULT NULL COMMENT '审核文件库资源ID',
  `file_url` varchar(500) NOT NULL COMMENT '文件地址',
  `file_name` varchar(255) DEFAULT '' COMMENT '文件名',
  `original_filename` varchar(255) DEFAULT '' COMMENT '原始文件名',
  `file_size` varchar(50) DEFAULT '' COMMENT '文件大小',
  `sort_num` int DEFAULT '0' COMMENT '排序号',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`basis_id`),
  KEY `idx_audit_review_basis_task` (`task_id`),
  KEY `idx_audit_review_basis_version` (`version_id`),
  KEY `idx_audit_review_basis_resource` (`library_resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核任务依据文件来源明细表';
