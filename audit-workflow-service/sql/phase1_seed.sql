INSERT INTO audit_workflow (
  workflow_code,
  workflow_name,
  description,
  input_schema,
  knowledge_binding,
  retrieval_config,
  prompt_template,
  output_schema,
  enabled,
  create_time,
  update_time
) VALUES (
  'policy_document_audit',
  '制度文件审核工作流',
  '阶段1示例工作流，当前使用mock节点跑通状态机',
  JSON_OBJECT('required', JSON_ARRAY('biz_id', 'input')),
  JSON_OBJECT('provider', 'business_audit_library'),
  JSON_OBJECT('top_k', 8, 'min_score', 0.3),
  '',
  JSON_OBJECT('type', 'object'),
  1,
  NOW(),
  NOW()
) ON DUPLICATE KEY UPDATE
  workflow_name = VALUES(workflow_name),
  description = VALUES(description),
  enabled = VALUES(enabled),
  update_time = NOW();

DELETE FROM audit_workflow_node
WHERE workflow_code = 'policy_document_audit';

INSERT INTO audit_workflow_node (
  workflow_code,
  node_code,
  node_name,
  node_type,
  node_order,
  node_config,
  enabled,
  create_time,
  update_time
) VALUES
('policy_document_audit', 'input_validate', '输入校验', 'INPUT_VALIDATE', 10, JSON_OBJECT(), 1, NOW(), NOW()),
('policy_document_audit', 'file_parse', '文件解析', 'FILE_PARSE', 20, JSON_OBJECT('mock', true), 1, NOW(), NOW()),
('policy_document_audit', 'text_split', '文本切分', 'TEXT_SPLIT', 30, JSON_OBJECT('mock', true), 1, NOW(), NOW()),
('policy_document_audit', 'knowledge_retrieve', '知识库检索', 'KNOWLEDGE_RETRIEVE', 40, JSON_OBJECT('mock', true), 1, NOW(), NOW()),
('policy_document_audit', 'ai_audit', 'AI审核', 'AI_AUDIT', 50, JSON_OBJECT('mock', true), 1, NOW(), NOW()),
('policy_document_audit', 'result_validate', '结果校验', 'RESULT_VALIDATE', 60, JSON_OBJECT('mock', true), 1, NOW(), NOW()),
('policy_document_audit', 'result_save', '结果保存', 'RESULT_SAVE', 70, JSON_OBJECT('mock', true), 1, NOW(), NOW()),
('policy_document_audit', 'callback', '回调通知', 'CALLBACK', 80, JSON_OBJECT('mock', true), 1, NOW(), NOW());
