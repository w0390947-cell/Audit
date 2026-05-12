CREATE TABLE IF NOT EXISTS audit_callback_log (
  callback_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '回调ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  task_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  callback_url VARCHAR(1000) NOT NULL COMMENT '回调地址',
  callback_status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/SUCCESS/FAILED',
  request_payload JSON DEFAULT NULL COMMENT '请求内容',
  response_status INT DEFAULT NULL COMMENT 'HTTP状态码',
  response_body TEXT COMMENT '响应内容',
  error_msg TEXT COMMENT '错误信息',
  retry_count INT DEFAULT 0 COMMENT '重试次数',
  max_retry_count INT DEFAULT 3 COMMENT '最大重试次数',
  next_retry_time DATETIME DEFAULT NULL COMMENT '下次重试时间',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (callback_id),
  KEY idx_audit_callback_log_task (task_id),
  KEY idx_audit_callback_log_status (callback_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核回调日志';

CREATE TABLE IF NOT EXISTS audit_review_feedback (
  feedback_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  task_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  issue_id BIGINT DEFAULT NULL COMMENT '问题ID',
  review_status VARCHAR(32) NOT NULL COMMENT 'confirmed/rejected/modified',
  reviewer_id VARCHAR(64) DEFAULT '' COMMENT '复核人ID',
  reviewer_name VARCHAR(128) DEFAULT '' COMMENT '复核人名称',
  feedback_content TEXT COMMENT '复核意见',
  corrected_issue JSON DEFAULT NULL COMMENT '修正后的问题内容',
  source_system VARCHAR(64) DEFAULT '' COMMENT '来源系统',
  create_time DATETIME DEFAULT NULL,
  PRIMARY KEY (feedback_id),
  KEY idx_audit_review_feedback_task (task_id),
  KEY idx_audit_review_feedback_issue (issue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核人工复核反馈';
