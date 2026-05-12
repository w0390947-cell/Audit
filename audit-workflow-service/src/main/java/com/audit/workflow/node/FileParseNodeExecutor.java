package com.audit.workflow.node;

import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.repository.AuditTaskInputRepository;
import com.audit.workflow.service.AuditInputFetchResult;
import com.audit.workflow.service.AuditInputFetchService;
import com.audit.workflow.service.DocumentParserService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FileParseNodeExecutor implements WorkflowNodeExecutor {

    private final AuditInputFetchService inputFetchService;
    private final DocumentParserService documentParserService;
    private final AuditTaskInputRepository taskInputRepository;

    public FileParseNodeExecutor(AuditInputFetchService inputFetchService,
                                 DocumentParserService documentParserService,
                                 AuditTaskInputRepository taskInputRepository) {
        this.inputFetchService = inputFetchService;
        this.documentParserService = documentParserService;
        this.taskInputRepository = taskInputRepository;
    }

    @Override
    public String nodeType() {
        return NodeType.FILE_PARSE;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        taskInputRepository.deleteByTaskId(context.getTask().getTaskId());
        AuditInputFetchResult input = inputFetchService.fetch(context.getInput());
        ParsedDocument document = documentParserService.parse(input);
        taskInputRepository.insertSuccess(context.getTask(), input, document);
        context.putVariable("parsed_document", document);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("parse_status", "SUCCESS");
        output.put("input_type", input.getInputType());
        output.put("file_name", input.getFileName());
        output.put("file_type", input.getFileType());
        output.put("file_hash", input.getFileHash());
        output.put("text_hash", document.getTextHash());
        output.put("char_count", document.getFullText().length());
        output.put("block_count", document.getBlocks().size());
        if (document.getMetadata().containsKey("page_count")) {
            output.put("page_count", document.getMetadata().get("page_count"));
        }
        if (document.getMetadata().containsKey("page_location_source")) {
            output.put("page_location_source", document.getMetadata().get("page_location_source"));
        }
        if (document.getMetadata().containsKey("page_location_support")) {
            output.put("page_location_support", document.getMetadata().get("page_location_support"));
        }
        return NodeExecutionResult.success(output);
    }
}
