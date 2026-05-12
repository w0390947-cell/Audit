UPDATE audit_workflow
SET description = '阶段5工作流：输入解析、知识库检索、AI审核、结构化结果保存、回调通知均已接入真实节点',
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit';

UPDATE audit_workflow_node
SET node_config = JSON_OBJECT('mock', false),
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit'
  AND node_type = 'CALLBACK';
