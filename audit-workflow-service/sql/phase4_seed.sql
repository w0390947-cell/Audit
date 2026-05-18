UPDATE audit_workflow
SET output_schema = JSON_OBJECT(
      'type', 'object',
      'required', JSON_ARRAY('summary', 'findings')
    ),
    description = '阶段4工作流：输入解析、文本切分、知识库检索、AI审核和结构化结果保存已接入真实节点，回调仍使用mock',
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit';

UPDATE audit_workflow_node
SET node_config = JSON_OBJECT('mock', false, 'audit_mode', 'business_report_findings'),
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit'
  AND node_type IN ('AI_AUDIT', 'RESULT_VALIDATE');

UPDATE audit_workflow_node
SET node_config = JSON_OBJECT('mock', false),
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit'
  AND node_type = 'RESULT_SAVE';
