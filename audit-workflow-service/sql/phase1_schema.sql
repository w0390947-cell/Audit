CREATE TABLE IF NOT EXISTS audit_workflow (
  workflow_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '工作流ID',
  workflow_code VARCHAR(64) NOT NULL COMMENT '工作流编码',
  workflow_name VARCHAR(128) NOT NULL COMMENT '工作流名称',
  description VARCHAR(500) DEFAULT '' COMMENT '说明',
  input_schema JSON DEFAULT NULL COMMENT '输入Schema',
  knowledge_binding JSON DEFAULT NULL COMMENT '知识库绑定配置',
  retrieval_config JSON DEFAULT NULL COMMENT '默认检索配置',
  prompt_template TEXT COMMENT '提示词模板',
  output_schema JSON DEFAULT NULL COMMENT '输出Schema',
  enabled TINYINT DEFAULT 1 COMMENT '是否启用',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (workflow_id),
  UNIQUE KEY uk_audit_workflow_code (workflow_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核工作流定义';

CREATE TABLE IF NOT EXISTS audit_workflow_node (
  node_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '节点ID',
  workflow_code VARCHAR(64) NOT NULL COMMENT '工作流编码',
  node_code VARCHAR(64) NOT NULL COMMENT '节点编码',
  node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
  node_type VARCHAR(64) NOT NULL COMMENT '节点类型',
  node_order INT NOT NULL COMMENT '执行顺序',
  node_config JSON DEFAULT NULL COMMENT '节点配置',
  enabled TINYINT DEFAULT 1 COMMENT '是否启用',
  create_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (node_id),
  KEY idx_audit_workflow_node_workflow (workflow_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核工作流节点定义';

CREATE TABLE IF NOT EXISTS audit_task (
  task_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  task_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  workflow_code VARCHAR(64) NOT NULL COMMENT '工作流编码',
  biz_id VARCHAR(128) DEFAULT '' COMMENT '业务系统ID',
  task_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态',
  current_node_code VARCHAR(64) DEFAULT '' COMMENT '当前节点',
  input_snapshot JSON DEFAULT NULL COMMENT '创建任务时的输入快照',
  summary JSON DEFAULT NULL COMMENT '结果摘要',
  error_code VARCHAR(64) DEFAULT '' COMMENT '错误码',
  error_msg TEXT COMMENT '错误信息',
  retry_count INT DEFAULT 0 COMMENT '重试次数',
  max_retry_count INT DEFAULT 3 COMMENT '最大重试次数',
  callback_url VARCHAR(500) DEFAULT '' COMMENT '回调地址',
  create_time DATETIME DEFAULT NULL,
  start_time DATETIME DEFAULT NULL,
  finish_time DATETIME DEFAULT NULL,
  update_time DATETIME DEFAULT NULL,
  PRIMARY KEY (task_id),
  UNIQUE KEY uk_audit_task_no (task_no),
  KEY idx_audit_task_workflow (workflow_code),
  KEY idx_audit_task_status (task_status),
  KEY idx_audit_task_biz (biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核任务';

CREATE TABLE IF NOT EXISTS audit_task_node_log (
  log_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  task_id BIGINT NOT NULL COMMENT '任务ID',
  task_no VARCHAR(64) NOT NULL COMMENT '任务编号',
  workflow_code VARCHAR(64) NOT NULL COMMENT '工作流编码',
  node_code VARCHAR(64) NOT NULL COMMENT '节点编码',
  node_type VARCHAR(64) NOT NULL COMMENT '节点类型',
  node_status VARCHAR(20) NOT NULL COMMENT '节点状态',
  input_snapshot JSON DEFAULT NULL COMMENT '节点输入',
  output_snapshot JSON DEFAULT NULL COMMENT '节点输出',
  error_code VARCHAR(64) DEFAULT '' COMMENT '错误码',
  error_msg TEXT COMMENT '错误信息',
  start_time DATETIME DEFAULT NULL,
  finish_time DATETIME DEFAULT NULL,
  duration_ms BIGINT DEFAULT 0 COMMENT '耗时毫秒',
  create_time DATETIME DEFAULT NULL,
  PRIMARY KEY (log_id),
  KEY idx_audit_task_node_log_task (task_id),
  KEY idx_audit_task_node_log_node (node_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI审核任务节点日志';
