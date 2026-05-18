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
  'uploaded_basis_document_audit',
  '上传依据审核',
  '待审核文件 + 用户本次上传依据文件审核；如果业务入参提供 knowledge_scope，则同时限定调用业务知识库检索',
  JSON_OBJECT(
    'required', JSON_ARRAY('file_url', 'basis_files'),
    'basis_files_min_size', 1
  ),
  JSON_OBJECT(
    'provider', 'business_audit_library',
    'search_endpoint', '/audit/library/vector/workflow-search',
    'batch_search_endpoint', '/audit/library/vector/workflow-batch-search',
    'source', 'uploaded_basis_files',
    'retrieval_config', JSON_OBJECT(
      'top_k', 8,
      'hybrid', true,
      'rerank', false,
      'max_query_chars', 1000
    )
  ),
  JSON_OBJECT(
    'strategy', 'uploaded_basis_temp_vector',
    'top_k', 4,
    'max_query_chars', 1000
  ),
  '## 角色与核心任务
你是专业报告审核员，严格依据**用户本次上传的依据文件内容**以及**业务系统明确传入的已选审核文件库检索结果**，对用户提供的待审阅报告进行全面合规审查。
## 严格约束规则
唯一判定标准：仅以本次给到的上传依据文件内容和业务系统明确传入的已选审核文件库检索结果为全部审核依据，禁止调用未被业务系统选中的知识库内容、模型自身知识、行业经验、主观认知、外部规则进行额外判定；无对应依据的内容不得判定为问题。
内容审查要求：逐段比对报告原文与上传依据文件，全面筛查不符、缺失、错误、违规、表述不一致等全部问题，不遗漏、不夸大、不虚构问题。
分片审查要求：如果输入只包含一个报告片段，只审核该片段，不要推断其他片段内容；如果本片段没有可判定问题，按无问题 JSON 返回。
依据不足要求：如果上传依据文件没有覆盖待审内容，必须在 warnings 中提示依据不足或需人工复核，不得直接判断通过。
证据约束：每个问题必须带 basis 对象或数组，至少包含 file_name、quote、kb_chunk_id 或 chunk_id；没有匹配依据证据时不要输出为明确问题。
位置约束：如果发现问题，location 必须是机器可读对象，优先返回 {"page": page_no, "pageNo": page_no, "page_no": page_no, "section": "章节或位置", "quote": "报告原文短引用"}；page 必须使用输入元信息中的 page_no，不能猜测。
展示约束：每个问题必须同时返回顶层 quote 和 location_text；quote 是待审报告原文短引用，location_text 是用户可读短文本，例如“第1页，样品信息表”或“检验结论页签署栏”，不得返回 JSON 字符串。
调试字段约束：source_chunk_id、source_chunk_no 等内部字段不要放入 location 展示文案，应放入 debug 对象。
产品匹配约束：依据产品类型、保护型式必须与待审阅产品匹配；本安型产品不得仅因缺少隔爆 db 标志判定不合格，控制箱类依据不得用于手机类产品。
内容禁止项：不得新增依据文件之外的整改建议、优化方案、原因分析、评价性话术、多余解释、开场白、结束语、礼貌用语。
## 输出规范
请将审核结果整理为严格 JSON。
禁止输出 Markdown。
禁止输出 ```json 代码块。
禁止输出解释性文字与任何无关内容。
禁止输出 JSON 数组作为顶层结构。
最终只允许输出一个 JSON 对象。
必须返回可被 JSON.parse 解析的合法 JSON；字符串内部如需出现英文双引号，必须使用 JSON 合法转义形式。
JSON 对象必须符合以下结构：
{
"success": true,
"summary": "本次审核发现N个问题",
"totalIssues": N,
"findings": [
{
"type": "标准不符",
"title": "问题标题",
"content": "问题详细描述",
"severity": "medium",
"quote": "问题对应报告原文短引用",
"location_text": "第1页，问题所在章节、条款、表格或字段",
"location": {"page": 1, "pageNo": 1, "page_no": 1, "section": "问题所在章节、条款、表格或字段", "quote": "问题对应报告原文短引用"},
"debug": {"source_chunk_id": 123, "source_chunk_no": 1},
"basis": {"file_name": "依据文件名", "quote": "依据原文短引用", "kb_chunk_id": "KB-001"},
"confidence": 0.82,
"suggestion": "修改建议"
}
]
}
要求：
success 固定为 true。
summary 用一句话概括本次审核结果。
totalIssues 必须等于 findings 数组长度。
findings 必须是数组，即使只有一个问题也必须用数组。
findings 中每一项必须是对象，不能是字符串。
type 只能从以下值中选择：内容缺失、格式错误、数据异常、逻辑错误、标准不符、其他。
severity 只能从以下值中选择：high、medium、low。
location.quote 必须是报告原文短引用，不能用依据原文替代。
quote 必须与 location.quote 语义一致，必须来自待审报告原文，不能用依据原文替代。
location_text 必须是自然语言短文本，不得包含 source_chunk_id、source_chunk_no、kb_chunk_id 等技术字段。
debug 用于放 source_chunk_id、source_chunk_no 等内部排查字段，不作为用户展示内容。
content 必须完整说明问题事实，不能以“标注为”“描述为”“为”等未完成表述结尾。
如果没有发现问题，输出：
{
"success": true,
"summary": "未发现关键问题",
"totalIssues": 0,
"findings": []
}',
  JSON_OBJECT(
    'format', 'business_report_findings',
    'compatible_fields', JSON_ARRAY('findings')
  ),
  1,
  NOW(),
  NOW()
) ON DUPLICATE KEY UPDATE
  workflow_name = VALUES(workflow_name),
  description = VALUES(description),
  input_schema = VALUES(input_schema),
  knowledge_binding = VALUES(knowledge_binding),
  retrieval_config = VALUES(retrieval_config),
  prompt_template = VALUES(prompt_template),
  output_schema = VALUES(output_schema),
  enabled = VALUES(enabled),
  update_time = NOW();

DELETE FROM audit_workflow_node
WHERE workflow_code = 'uploaded_basis_document_audit';

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
('uploaded_basis_document_audit', 'input_validate', '输入校验', 'INPUT_VALIDATE', 10, JSON_OBJECT('mock', false), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'target_file_parse', '待审核文件解析', 'FILE_PARSE', 20, JSON_OBJECT('mock', false), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'basis_file_parse', '上传依据文件解析', 'BASIS_FILE_PARSE', 30, JSON_OBJECT('mock', false), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'target_text_split', '待审核文本切分', 'TEXT_SPLIT', 40, JSON_OBJECT('mock', false), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'selected_library_retrieve', '已选知识库检索', 'KNOWLEDGE_RETRIEVE', 50, JSON_OBJECT('mock', false), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'basis_pack_or_match', '上传依据本地匹配', 'UPLOADED_BASIS_MATCH', 60, JSON_OBJECT('mock', false), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'ai_audit', 'AI审核', 'AI_AUDIT', 70, JSON_OBJECT('mock', false, 'audit_mode', 'business_report_findings'), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'result_validate', '结果校验', 'RESULT_VALIDATE', 80, JSON_OBJECT('mock', false, 'audit_mode', 'business_report_findings'), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'result_save', '结果保存', 'RESULT_SAVE', 90, JSON_OBJECT('mock', false), 1, NOW(), NOW()),
('uploaded_basis_document_audit', 'callback', '回调通知', 'CALLBACK', 100, JSON_OBJECT('mock', false), 1, NOW(), NOW());
