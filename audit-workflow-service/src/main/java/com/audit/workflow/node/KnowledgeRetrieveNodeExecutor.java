package com.audit.workflow.node;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditWorkflow;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.ContentChunk;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.RetrievalReference;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.repository.AuditRetrievalRepository;
import com.audit.workflow.repository.AuditTaskContentChunkRepository;
import com.audit.workflow.repository.AuditWorkflowRepository;
import com.audit.workflow.retrieval.BatchRetrievalRequest;
import com.audit.workflow.retrieval.BatchRetrievalResponse;
import com.audit.workflow.retrieval.KnowledgeRetrievalClient;
import com.audit.workflow.retrieval.RetrievalRequest;
import com.audit.workflow.retrieval.RetrievalResponse;
import com.audit.workflow.support.JsonSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KnowledgeRetrieveNodeExecutor implements WorkflowNodeExecutor {

    private final AuditTaskContentChunkRepository contentChunkRepository;
    private final AuditRetrievalRepository retrievalRepository;
    private final AuditWorkflowRepository workflowRepository;
    private final KnowledgeRetrievalClient retrievalClient;
    private final JsonSupport jsonSupport;
    private final int batchSize;

    public KnowledgeRetrieveNodeExecutor(AuditTaskContentChunkRepository contentChunkRepository,
                                         AuditRetrievalRepository retrievalRepository,
                                         AuditWorkflowRepository workflowRepository,
                                         KnowledgeRetrievalClient retrievalClient,
                                         JsonSupport jsonSupport,
                                         @Value("${audit.knowledge.batch-size:10}") int batchSize) {
        this.contentChunkRepository = contentChunkRepository;
        this.retrievalRepository = retrievalRepository;
        this.workflowRepository = workflowRepository;
        this.retrievalClient = retrievalClient;
        this.jsonSupport = jsonSupport;
        this.batchSize = batchSize;
    }

    @Override
    public String nodeType() {
        return NodeType.KNOWLEDGE_RETRIEVE;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        List<ContentChunk> chunks = contentChunkRepository.findByTaskId(context.getTask().getTaskId());
        if (chunks.isEmpty()) {
            throw new BusinessException("KB_SCOPE_EMPTY", "no source chunks for retrieval");
        }

        AuditWorkflow workflow = workflowRepository.findByCode(context.getTask().getWorkflowCode())
                .orElseThrow(() -> new BusinessException("WORKFLOW_NOT_FOUND", "workflow not found"));
        Map<String, Object> binding = jsonSupport.toMap(workflow.getKnowledgeBinding());
        Map<String, Object> scope = mergeMaps(mapValue(binding.get("default_scope")), mapValue(context.getInput().get("knowledge_scope")));
        if (!scope.containsKey("knowledge_base_codes") && binding.containsKey("knowledge_base_codes")) {
            scope.put("knowledge_base_codes", binding.get("knowledge_base_codes"));
        }
        if (scope.isEmpty()) {
            throw new BusinessException("KB_SCOPE_REQUIRED", "knowledge_scope is required");
        }
        Map<String, Object> retrievalConfig = mergeMaps(jsonSupport.toMap(workflow.getRetrievalConfig()), mapValue(binding.get("retrieval_config")));
        retrievalConfig = mergeMaps(retrievalConfig, mapValue(context.getInput().get("retrieval_config")));
        Map<String, Object> callerContext = mapValue(context.getInput().get("caller_context"));

        retrievalRepository.deleteByTaskId(context.getTask().getTaskId());

        int retrievalCount = 0;
        int referenceCount = 0;
        List<Map<String, Object>> referenceSummary = new ArrayList<>();
        for (int start = 0; start < chunks.size(); start += batchSize) {
            List<ContentChunk> batch = chunks.subList(start, Math.min(start + batchSize, chunks.size()));
            BatchRetrievalRequest batchRequest = buildBatchRequest(context, batch, scope, callerContext, retrievalConfig);
            long started = System.currentTimeMillis();
            BatchRetrievalResponse batchResponse = retrievalClient.batchSearch(batchRequest);
            long durationMs = System.currentTimeMillis() - started;
            if (!batchResponse.isSuccess()) {
                if (!"KB_UNAVAILABLE".equals(batchResponse.getErrorCode())) {
                    throw new BusinessException(batchResponse.getErrorCode(), batchResponse.getErrorMsg());
                }
                batchResponse = fallbackSingleSearch(batchRequest);
            }

            for (RetrievalRequest request : batchRequest.getQueries()) {
                RetrievalResponse response = batchResponse.getResponses().get(request.getSourceChunkId());
                if (response == null) {
                    response = RetrievalResponse.success(request.getRequestId(), List.of());
                }
                retrievalCount++;
                List<RetrievalReference> references = response.getReferences();
                referenceCount += references.size();
                String status = response.isSuccess() ? "SUCCESS" : "FAILED";
                Long retrievalId = retrievalRepository.insertRecord(
                        context.getTask(),
                        request,
                        jsonSupport.toJson(request),
                        jsonSupport.toJson(Map.of("result_count", references.size())),
                        references.size(),
                        status,
                        response.getErrorCode(),
                        response.getErrorMsg(),
                        durationMs);
                retrievalRepository.insertReferences(retrievalId, context.getTask(), request.getSourceChunkId(), references);
                if (!response.isSuccess()) {
                    throw new BusinessException(response.getErrorCode(), response.getErrorMsg());
                }
                referenceSummary.add(Map.of(
                        "source_chunk_id", request.getSourceChunkId(),
                        "reference_count", references.size()));
            }
        }

        context.putVariable("retrieval_reference_summary", referenceSummary);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("retrieval_status", "SUCCESS");
        output.put("source_chunk_count", chunks.size());
        output.put("retrieval_count", retrievalCount);
        output.put("reference_count", referenceCount);
        return NodeExecutionResult.success(output);
    }

    private BatchRetrievalResponse fallbackSingleSearch(BatchRetrievalRequest batchRequest) {
        BatchRetrievalResponse response = new BatchRetrievalResponse();
        response.setRequestId(batchRequest.getRequestId());
        response.setSuccess(true);
        for (RetrievalRequest request : batchRequest.getQueries()) {
            response.getResponses().put(request.getSourceChunkId(), retrievalClient.search(request));
        }
        return response;
    }

    private BatchRetrievalRequest buildBatchRequest(WorkflowTaskContext context,
                                                   List<ContentChunk> chunks,
                                                   Map<String, Object> scope,
                                                   Map<String, Object> callerContext,
                                                   Map<String, Object> retrievalConfig) {
        BatchRetrievalRequest batchRequest = new BatchRetrievalRequest();
        batchRequest.setRequestId(context.getTask().getTaskNo() + "-KB-BATCH-" + System.nanoTime());
        batchRequest.setWorkflowCode(context.getTask().getWorkflowCode());
        batchRequest.setTaskId(context.getTask().getTaskId());
        batchRequest.setTaskNo(context.getTask().getTaskNo());
        batchRequest.setKnowledgeScope(scope);
        batchRequest.setCallerContext(callerContext);
        batchRequest.setRetrievalConfig(retrievalConfig);
        for (ContentChunk chunk : chunks) {
            RetrievalRequest request = new RetrievalRequest();
            request.setRequestId(context.getTask().getTaskNo() + "-KB-" + chunk.getSourceChunkId());
            request.setWorkflowCode(context.getTask().getWorkflowCode());
            request.setTaskId(context.getTask().getTaskId());
            request.setTaskNo(context.getTask().getTaskNo());
            request.setSourceChunkId(chunk.getSourceChunkId());
            request.setQuery(buildQuery(chunk, retrievalConfig));
            request.setKnowledgeScope(scope);
            request.setCallerContext(callerContext);
            request.setRetrievalConfig(retrievalConfig);
            batchRequest.getQueries().add(request);
        }
        return batchRequest;
    }

    private String buildQuery(ContentChunk chunk, Map<String, Object> retrievalConfig) {
        int maxChars = intValue(retrievalConfig.get("max_query_chars"), 1000);
        String text = chunk.getChunkText() == null ? "" : chunk.getChunkText();
        String prefix = "请检索与以下待审核内容相关的审核依据、制度条款、标准要求：\n";
        String section = chunk.getSectionTitle() == null || chunk.getSectionTitle().isBlank()
                ? ""
                : "章节：" + chunk.getSectionTitle() + "\n";
        int textMaxChars = Math.max(0, maxChars - prefix.length() - section.length());
        if (text.length() > textMaxChars) {
            text = text.substring(0, textMaxChars);
        }
        String query = prefix + section + text;
        if (query.length() > maxChars) {
            return query.substring(0, maxChars);
        }
        return query;
    }

    private Map<String, Object> mergeMaps(Map<String, Object> first, Map<String, Object> second) {
        Map<String, Object> merged = new LinkedHashMap<>();
        if (first != null) {
            merged.putAll(first);
        }
        if (second != null) {
            merged.putAll(second);
        }
        return merged;
    }

    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() != null) {
                    map.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return map;
        }
        return new LinkedHashMap<>();
    }

    private int intValue(Object value, int defaultValue) {
        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
