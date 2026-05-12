package com.audit.workflow.node;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.service.AuditInputFetchResult;
import com.audit.workflow.service.AuditInputFetchService;
import com.audit.workflow.service.DocumentParserService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class BasisFileParseNodeExecutor implements WorkflowNodeExecutor {

    private final AuditInputFetchService inputFetchService;
    private final DocumentParserService documentParserService;

    public BasisFileParseNodeExecutor(AuditInputFetchService inputFetchService,
                                      DocumentParserService documentParserService) {
        this.inputFetchService = inputFetchService;
        this.documentParserService = documentParserService;
    }

    @Override
    public String nodeType() {
        return NodeType.BASIS_FILE_PARSE;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        List<Map<String, Object>> basisInputs = basisInputs(context.getInput());
        if (basisInputs.isEmpty()) {
            throw new BusinessException("BASIS_FILE_REQUIRED", "basis_files is required");
        }

        List<ParsedDocument> documents = new ArrayList<>();
        List<Map<String, Object>> fileSummaries = new ArrayList<>();
        int failedCount = 0;
        int charCount = 0;
        int blockCount = 0;
        for (Map<String, Object> basisInput : basisInputs) {
            try {
                AuditInputFetchResult fetched = inputFetchService.fetch(basisInput);
                fetched.getMetadata().put("basis_file_id", fetched.getFileId());
                fetched.getMetadata().put("basis_file_name", fetched.getFileName());
                fetched.getMetadata().put("basis_file_url", fetched.getFileUrl());
                ParsedDocument document = documentParserService.parse(fetched);
                documents.add(document);
                charCount += document.getFullText().length();
                blockCount += document.getBlocks().size();

                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("file_id", fetched.getFileId());
                summary.put("file_name", fetched.getFileName());
                summary.put("file_type", fetched.getFileType());
                summary.put("file_hash", fetched.getFileHash());
                summary.put("text_hash", document.getTextHash());
                summary.put("char_count", document.getFullText().length());
                summary.put("block_count", document.getBlocks().size());
                summary.put("parse_status", "SUCCESS");
                fileSummaries.add(summary);
            } catch (BusinessException ex) {
                failedCount++;
                Map<String, Object> summary = new LinkedHashMap<>();
                summary.put("file_id", stringValue(basisInput.get("file_id")));
                summary.put("file_name", stringValue(basisInput.get("file_name")));
                summary.put("parse_status", "FAILED");
                summary.put("error_code", ex.getErrorCode());
                summary.put("error_msg", ex.getMessage());
                fileSummaries.add(summary);
            }
        }

        if (documents.isEmpty()) {
            throw new BusinessException("BASIS_FILE_PARSE_FAILED", "all basis files parse failed");
        }

        context.putVariable("uploaded_basis_documents", documents);
        context.putVariable("uploaded_basis_file_summaries", fileSummaries);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("file_count", basisInputs.size());
        output.put("char_count", charCount);
        output.put("block_count", blockCount);
        output.put("basis_file_count", basisInputs.size());
        output.put("basis_parse_success_count", documents.size());
        output.put("basis_parse_failed_count", failedCount);
        output.put("basis_files", fileSummaries);
        return NodeExecutionResult.success(output);
    }

    private List<Map<String, Object>> basisInputs(Map<String, Object> input) {
        List<Map<String, Object>> result = new ArrayList<>();
        Object basisFiles = input.get("basis_files");
        if (basisFiles instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> map = mapValue(item);
                if (!stringValue(map.get("file_url")).isBlank() || !stringValue(map.get("fileUrl")).isBlank()) {
                    result.add(map);
                }
            }
        }
        if (!result.isEmpty()) {
            return result;
        }
        Object urls = input.get("basis_file_urls");
        if (urls instanceof List<?> list) {
            int index = 1;
            for (Object item : list) {
                String url = stringValue(item);
                if (url.isBlank()) {
                    continue;
                }
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("file_id", "BASIS-" + index);
                map.put("file_url", url);
                map.put("file_name", fileNameFromUrl(url));
                result.add(map);
                index++;
            }
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

    private String fileNameFromUrl(String url) {
        int slash = url.lastIndexOf('/');
        return slash >= 0 ? url.substring(slash + 1) : url;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
