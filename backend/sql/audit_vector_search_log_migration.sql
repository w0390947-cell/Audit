SET NAMES utf8mb4;

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
