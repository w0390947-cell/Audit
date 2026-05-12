CREATE TABLE IF NOT EXISTS audit_model_call_log (
  call_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '模型调用ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  task_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  workflow_code VARCHAR(64) NOT NULL COMMENT '工作流编码',
  source_chunk_id BIGINT DEFAULT NULL COMMENT '待审核内容片段ID',
  provider VARCHAR(64) DEFAULT '' COMMENT '模型供应商',
  model_name VARCHAR(128) DEFAULT '' COMMENT '模型名称',
  request_id VARCHAR(128) DEFAULT '' COMMENT '模型请求ID',
  prompt_snapshot MEDIUMTEXT COMMENT '提示词快照',
  response_snapshot MEDIUMTEXT COMMENT '模型响应快照',
  input_tokens INT DEFAULT 0 COMMENT '输入token',
  output_tokens INT DEFAULT 0 COMMENT '输出token',
  call_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '调用状态',
  error_code VARCHAR(64) DEFAULT '' COMMENT '错误码',
  error_msg TEXT COMMENT '错误信息',
  duration_ms BIGINT DEFAULT 0 COMMENT '耗时毫秒',
  create_time DATETIME DEFAULT NULL,
  PRIMARY KEY (call_id),
  KEY idx_audit_model_call_log_task (task_id),
  KEY idx_audit_model_call_log_chunk (source_chunk_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核模型调用日志';

CREATE TABLE IF NOT EXISTS audit_result (
  result_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  task_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  workflow_code VARCHAR(64) NOT NULL COMMENT '工作流编码',
  overall_result VARCHAR(64) DEFAULT '' COMMENT '总体结论',
  risk_level VARCHAR(20) DEFAULT '' COMMENT '总体风险等级',
  total_issues INT DEFAULT 0 COMMENT '问题数量',
  summary JSON DEFAULT NULL COMMENT '结果摘要',
  result_json JSON DEFAULT NULL COMMENT '完整结果JSON',
  validate_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '校验状态',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (result_id),
  UNIQUE KEY uk_audit_result_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核结果汇总';

CREATE TABLE IF NOT EXISTS audit_result_issue (
  issue_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  result_id BIGINT NOT NULL COMMENT '结果ID',
  issue_no VARCHAR(64) NOT NULL COMMENT '问题编号',
  source_chunk_id BIGINT DEFAULT NULL COMMENT '待审核内容片段ID',
  title VARCHAR(255) DEFAULT '' COMMENT '问题标题',
  risk_level VARCHAR(20) DEFAULT '' COMMENT '风险等级',
  problem TEXT COMMENT '问题描述',
  suggestion TEXT COMMENT '整改建议',
  confidence DECIMAL(5,4) DEFAULT NULL COMMENT '置信度',
  location JSON DEFAULT NULL COMMENT '问题位置',
  issue_status VARCHAR(20) DEFAULT 'AI_GENERATED' COMMENT '问题状态',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (issue_id),
  KEY idx_audit_result_issue_task (task_id),
  KEY idx_audit_result_issue_result (result_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核问题明细';

CREATE TABLE IF NOT EXISTS audit_result_reference (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  issue_id BIGINT NOT NULL COMMENT '问题ID',
  retrieval_reference_id BIGINT DEFAULT NULL COMMENT '检索依据快照ID',
  kb_chunk_id VARCHAR(128) DEFAULT '' COMMENT '知识库片段ID',
  file_name VARCHAR(255) DEFAULT '' COMMENT '来源文件名',
  file_url VARCHAR(1000) DEFAULT '' COMMENT '来源文件URL',
  version_no VARCHAR(64) DEFAULT '' COMMENT '来源版本',
  page_no INT DEFAULT NULL COMMENT '页码',
  section_title VARCHAR(255) DEFAULT '' COMMENT '章节标题',
  quote_text TEXT COMMENT '引用原文',
  create_time DATETIME DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_audit_result_reference_task (task_id),
  KEY idx_audit_result_reference_issue (issue_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核问题依据引用';
