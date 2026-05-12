UPDATE audit_workflow_node
SET node_config = JSON_OBJECT('mock', false),
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit'
  AND node_type IN ('FILE_PARSE', 'TEXT_SPLIT');

UPDATE audit_workflow
SET description = '阶段2工作流：输入校验、文件解析和文本切分已接入真实实现，后续节点仍使用mock',
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit';
