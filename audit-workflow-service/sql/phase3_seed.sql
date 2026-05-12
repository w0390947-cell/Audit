UPDATE audit_workflow
SET knowledge_binding = JSON_OBJECT(
      'provider', 'business_audit_library',
      'search_endpoint', '/audit/library/vector/workflow-search',
      'batch_search_endpoint', '/audit/library/vector/workflow-batch-search',
      'knowledge_base_codes', JSON_ARRAY('project_policy', 'compliance_policy'),
      'default_scope', JSON_OBJECT('effective_only', true),
      'permission_mode', 'explicit_scope',
      'retrieval_config', JSON_OBJECT(
        'top_k', 8,
        'min_score', 0.3,
        'hybrid', true,
        'rerank', true,
        'max_chunk_chars', 1800,
        'max_query_chars', 1200
      )
    ),
    description = '阶段3工作流：输入解析、文本切分、业务知识库检索已接入真实节点，后续AI审核仍使用mock',
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit';

UPDATE audit_workflow_node
SET node_config = JSON_OBJECT('mock', false),
    update_time = NOW()
WHERE workflow_code = 'policy_document_audit'
  AND node_type = 'KNOWLEDGE_RETRIEVE';
