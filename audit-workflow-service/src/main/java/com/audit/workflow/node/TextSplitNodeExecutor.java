package com.audit.workflow.node;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.ContentChunk;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.repository.AuditTaskContentChunkRepository;
import com.audit.workflow.service.TextChunkService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class TextSplitNodeExecutor implements WorkflowNodeExecutor {

    private final TextChunkService textChunkService;
    private final AuditTaskContentChunkRepository contentChunkRepository;

    public TextSplitNodeExecutor(TextChunkService textChunkService,
                                 AuditTaskContentChunkRepository contentChunkRepository) {
        this.textChunkService = textChunkService;
        this.contentChunkRepository = contentChunkRepository;
    }

    @Override
    public String nodeType() {
        return NodeType.TEXT_SPLIT;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        Object parsed = context.getVariables().get("parsed_document");
        if (!(parsed instanceof ParsedDocument document)) {
            throw new BusinessException("SPLIT_FAILED", "parsed document not found in context");
        }
        contentChunkRepository.deleteByTaskId(context.getTask().getTaskId());
        List<ContentChunk> chunks = textChunkService.split(document);
        if (chunks.isEmpty()) {
            throw new BusinessException("SPLIT_FAILED", "no content chunk generated");
        }
        List<Long> sourceChunkIds = contentChunkRepository.insertChunks(context.getTask(), chunks);
        context.putVariable("source_chunk_ids", sourceChunkIds);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("chunk_count", chunks.size());
        output.put("source_chunk_ids", sourceChunkIds);
        return NodeExecutionResult.success(output);
    }
}
