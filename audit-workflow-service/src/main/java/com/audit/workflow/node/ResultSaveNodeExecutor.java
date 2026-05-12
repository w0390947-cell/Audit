package com.audit.workflow.node;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.RetrievalReference;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.repository.AuditResultRepository;
import com.audit.workflow.repository.AuditRetrievalRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ResultSaveNodeExecutor implements WorkflowNodeExecutor {

    private final AuditResultRepository resultRepository;
    private final AuditRetrievalRepository retrievalRepository;

    public ResultSaveNodeExecutor(AuditResultRepository resultRepository,
                                  AuditRetrievalRepository retrievalRepository) {
        this.resultRepository = resultRepository;
        this.retrievalRepository = retrievalRepository;
    }

    @Override
    public String nodeType() {
        return NodeType.RESULT_SAVE;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        Object rawResult = context.getVariables().get("validated_result");
        if (!(rawResult instanceof Map<?, ?>)) {
            throw new BusinessException("RESULT_SAVE_FAILED", "validated result not found");
        }
        Map<String, Object> result = mapValue(rawResult);
        resultRepository.deleteByTaskId(context.getTask().getTaskId());
        Long resultId = resultRepository.insertResult(context.getTask(), result);

        Map<String, RetrievalReference> referenceByKbChunkId = retrievalRepository.findReferencesByTaskId(context.getTask().getTaskId())
                .stream()
                .filter(reference -> reference.getKbChunkId() != null && !reference.getKbChunkId().isBlank())
                .collect(Collectors.toMap(RetrievalReference::getKbChunkId, Function.identity(), (left, right) -> left));

        List<Map<String, Object>> issues = listOfMaps(result.get("issues"));
        if (issues.isEmpty() && result.containsKey("findings")) {
            issues = findingsToIssues(result.get("findings"));
        }
        int index = 1;
        for (Map<String, Object> issue : issues) {
            Long issueId = resultRepository.insertIssue(resultId, context.getTask(), issue, index++);
            for (Map<String, Object> basis : listOfMaps(issue.get("basis"))) {
                String kbChunkId = stringValue(basis.get("kb_chunk_id"));
                RetrievalReference reference = referenceByKbChunkId.get(kbChunkId);
                String quote = stringValue(first(basis, "quote", "quote_text", "content"));
                if (quote.isBlank() && reference != null) {
                    quote = reference.getChunkTextSnapshot();
                }
                resultRepository.insertResultReference(context.getTask(), issueId, reference, quote);
            }
        }

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("result_id", resultId);
        output.put("issue_count", issues.size());
        return NodeExecutionResult.success(output);
    }

    private List<Map<String, Object>> findingsToIssues(Object value) {
        List<Map<String, Object>> issues = new ArrayList<>();
        for (Map<String, Object> finding : listOfMaps(value)) {
            Map<String, Object> issue = new LinkedHashMap<>();
            issue.put("title", finding.get("title"));
            issue.put("risk_level", finding.get("severity"));
            issue.put("problem", finding.get("content"));
            issue.put("suggestion", finding.get("suggestion"));
            Map<String, Object> location = mapValue(finding.get("location"));
            if (location.isEmpty() && !stringValue(finding.get("location")).isBlank()) {
                location.put("section", finding.get("location"));
            }
            Map<String, Object> debug = mapValue(finding.get("debug"));
            for (String key : List.of("page", "pageNo", "page_no", "source_chunk_id", "source_chunk_no")) {
                if (finding.containsKey(key) && !stringValue(finding.get(key)).isBlank()) {
                    location.putIfAbsent(key, finding.get(key));
                }
                if (debug.containsKey(key) && !stringValue(debug.get(key)).isBlank()) {
                    location.putIfAbsent(key, debug.get(key));
                }
            }
            if (debug.containsKey("source_chunk_id") && !stringValue(debug.get("source_chunk_id")).isBlank()) {
                issue.put("source_chunk_id", debug.get("source_chunk_id"));
            }
            issue.put("location", location);
            issue.put("basis", listOfMaps(finding.get("basis")));
            issues.add(issue);
        }
        return issues;
    }

    private Object first(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private List<Map<String, Object>> listOfMaps(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                result.add(mapValue(item));
            }
        } else if (value instanceof Map<?, ?>) {
            result.add(mapValue(value));
        }
        return result;
    }

    private Map<String, Object> mapValue(Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> rawMap) {
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() != null) {
                    map.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }
        return map;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
