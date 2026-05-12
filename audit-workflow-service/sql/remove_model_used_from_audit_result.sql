-- 删除历史审核结果 JSON 中对外不再保留的 model_used 字段。
UPDATE audit_result
SET result_json = JSON_REMOVE(result_json, '$.model_used', '$.raw_output.model_used')
WHERE result_json IS NOT NULL
  AND (
    JSON_CONTAINS_PATH(result_json, 'one', '$.model_used')
    OR JSON_CONTAINS_PATH(result_json, 'one', '$.raw_output.model_used')
  );
