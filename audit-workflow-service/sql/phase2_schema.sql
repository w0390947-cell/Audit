CREATE TABLE IF NOT EXISTS audit_task_input (
  input_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '输入ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  task_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  input_type VARCHAR(20) NOT NULL COMMENT 'file_url/file_id/text/binary',
  file_id VARCHAR(128) DEFAULT '' COMMENT '业务系统文件ID',
  file_url VARCHAR(1000) DEFAULT '' COMMENT '文件URL',
  file_name VARCHAR(255) DEFAULT '' COMMENT '文件名',
  file_type VARCHAR(20) DEFAULT '' COMMENT '文件类型',
  file_hash VARCHAR(64) DEFAULT '' COMMENT '文件hash',
  text_hash VARCHAR(64) DEFAULT '' COMMENT '文本hash',
  metadata JSON DEFAULT NULL COMMENT '业务元数据',
  raw_input_snapshot JSON DEFAULT NULL COMMENT '原始输入快照',
  parse_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '解析状态',
  error_msg TEXT COMMENT '错误信息',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (input_id),
  KEY idx_audit_task_input_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核任务输入';

CREATE TABLE IF NOT EXISTS audit_task_content_chunk (
  source_chunk_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '待审核内容片段ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  task_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  chunk_no INT NOT NULL COMMENT '片段序号',
  chunk_text MEDIUMTEXT NOT NULL COMMENT '片段文本',
  page_no INT DEFAULT NULL COMMENT '页码',
  section_title VARCHAR(255) DEFAULT '' COMMENT '章节标题',
  section_path VARCHAR(500) DEFAULT '' COMMENT '章节路径',
  token_count INT DEFAULT 0 COMMENT '估算token数',
  char_count INT DEFAULT 0 COMMENT '字符数',
  content_hash VARCHAR(64) DEFAULT '' COMMENT '片段hash',
  metadata JSON DEFAULT NULL COMMENT '片段元数据',
  create_time DATETIME DEFAULT NULL,
  PRIMARY KEY (source_chunk_id),
  KEY idx_audit_task_content_chunk_task (task_id),
  KEY idx_audit_task_content_chunk_no (task_id, chunk_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核待审核内容切片';
