SET NAMES utf8mb4;

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
