package com.audit.workflow.retrieval;

import com.audit.workflow.domain.RetrievalReference;
import com.audit.workflow.support.JsonSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class HttpKnowledgeRetrievalClient implements KnowledgeRetrievalClient {

    private final String baseUrl;
    private final String searchEndpoint;
    private final String batchSearchEndpoint;
    private final String serviceToken;
    private final int timeoutSeconds;
    private final boolean required;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpKnowledgeRetrievalClient(@Value("${audit.knowledge.base-url:}") String baseUrl,
                                        @Value("${audit.knowledge.search-endpoint:/audit/library/vector/workflow-search}") String searchEndpoint,
                                        @Value("${audit.knowledge.batch-search-endpoint:/audit/library/vector/workflow-batch-search}") String batchSearchEndpoint,
                                        @Value("${audit.knowledge.service-token:}") String serviceToken,
                                        @Value("${audit.knowledge.timeout-seconds:30}") int timeoutSeconds,
                                        @Value("${audit.knowledge.required:false}") boolean required,
                                        ObjectMapper objectMapper,
                                        JsonSupport jsonSupport) {
        this.baseUrl = trimRight(baseUrl);
        this.searchEndpoint = searchEndpoint;
        this.batchSearchEndpoint = batchSearchEndpoint;
        this.serviceToken = serviceToken;
        this.timeoutSeconds = timeoutSeconds;
        this.required = required;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Override
    public RetrievalResponse search(RetrievalRequest request) {
        if (baseUrl == null || baseUrl.isBlank()) {
            if (required) {
                return RetrievalResponse.failure(request.getRequestId(), "KB_SCOPE_REQUIRED", "audit.knowledge.base-url is required");
            }
            return RetrievalResponse.success(request.getRequestId(), List.of());
        }
        try {
            Map<String, Object> payload = toSearchPayload(request);
            Map<String, Object> body = post(searchEndpoint, payload, request.getRequestId(), request.getWorkflowCode());
            String errorCode = businessErrorCode(body);
            if (!errorCode.isBlank()) {
                return RetrievalResponse.failure(request.getRequestId(), errorCode, businessErrorMsg(body));
            }
            return RetrievalResponse.success(request.getRequestId(), parseReferences(extractResults(body), request.getSourceChunkId()));
        } catch (Exception ex) {
            return RetrievalResponse.failure(request.getRequestId(), "KB_UNAVAILABLE", ex.getMessage());
        }
    }

    @Override
    public BatchRetrievalResponse batchSearch(BatchRetrievalRequest request) {
        if (baseUrl == null || baseUrl.isBlank()) {
            BatchRetrievalResponse response = new BatchRetrievalResponse();
            response.setRequestId(request.getRequestId());
            if (required) {
                response.setSuccess(false);
                response.setErrorCode("KB_SCOPE_REQUIRED");
                response.setErrorMsg("audit.knowledge.base-url is required");
                return response;
            }
            response.setSuccess(true);
            for (RetrievalRequest query : request.getQueries()) {
                response.getResponses().put(query.getSourceChunkId(), RetrievalResponse.success(query.getRequestId(), List.of()));
            }
            return response;
        }

        try {
            Map<String, Object> payload = toBatchPayload(request);
            Map<String, Object> body = post(batchSearchEndpoint, payload, request.getRequestId(), request.getWorkflowCode());
            BatchRetrievalResponse response = new BatchRetrievalResponse();
            response.setRequestId(request.getRequestId());
            String errorCode = businessErrorCode(body);
            if (!errorCode.isBlank()) {
                response.setSuccess(false);
                response.setErrorCode(errorCode);
                response.setErrorMsg(businessErrorMsg(body));
                return response;
            }
            response.setSuccess(true);

            Map<Long, RetrievalRequest> requestByChunk = new LinkedHashMap<>();
            Map<String, RetrievalRequest> requestByQueryId = new LinkedHashMap<>();
            for (RetrievalRequest query : request.getQueries()) {
                requestByChunk.put(query.getSourceChunkId(), query);
                requestByQueryId.put(query.getRequestId(), query);
            }

            Object data = body.get("data");
            Object groups = data instanceof Map<?, ?> dataMap ? first(dataMap, "groups", "results") : body.get("groups");
            if (groups instanceof List<?> groupList) {
                for (Object group : groupList) {
                    if (group instanceof Map<?, ?> groupMap) {
                        Long sourceChunkId = longValue(first(groupMap, "source_chunk_id", "sourceChunkId"));
                        String queryId = stringValue(first(groupMap, "query_id", "queryId", "request_id", "requestId"));
                        RetrievalRequest query = sourceChunkId == null ? requestByQueryId.get(queryId) : requestByChunk.get(sourceChunkId);
                        if (sourceChunkId == null && query != null) {
                            sourceChunkId = query.getSourceChunkId();
                        }
                        Object rawResults = first(groupMap, "results", "references", "hits", "items");
                        if (rawResults == null && first(groupMap, "chunk_id", "chunkId", "kb_chunk_id", "kbChunkId") != null) {
                            rawResults = List.of(groupMap);
                        }
                        response.getResponses().put(
                                sourceChunkId,
                                RetrievalResponse.success(
                                        query == null ? request.getRequestId() : query.getRequestId(),
                                        parseReferences(rawResults, sourceChunkId)));
                    }
                }
            }
            for (RetrievalRequest query : request.getQueries()) {
                response.getResponses().putIfAbsent(query.getSourceChunkId(), RetrievalResponse.success(query.getRequestId(), List.of()));
            }
            return response;
        } catch (Exception ex) {
            BatchRetrievalResponse response = new BatchRetrievalResponse();
            response.setRequestId(request.getRequestId());
            response.setSuccess(false);
            response.setErrorCode("KB_UNAVAILABLE");
            response.setErrorMsg(ex.getMessage());
            return response;
        }
    }

    private Map<String, Object> post(String endpoint, Map<String, Object> payload, String requestId, String workflowCode) throws Exception {
        String body = objectMapper.writeValueAsString(payload);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + endpoint))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("X-Request-Id", requestId == null ? "" : requestId)
                .header("X-Workflow-Code", workflowCode == null ? "" : workflowCode)
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (serviceToken != null && !serviceToken.isBlank()) {
            builder.header("Authorization", "Bearer " + serviceToken);
        }
        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("knowledge search http status " + response.statusCode());
        }
        return objectMapper.readValue(response.body(), Map.class);
    }

    private String businessErrorCode(Map<String, Object> body) {
        String errorCode = stringValue(body.get("error_code"));
        if (!errorCode.isBlank() && !"null".equalsIgnoreCase(errorCode)) {
            return errorCode;
        }
        Object code = body.get("code");
        if (code == null || String.valueOf(code).isBlank()) {
            return "";
        }
        String codeText = String.valueOf(code);
        if ("200".equals(codeText) || "0".equals(codeText)) {
            return "";
        }
        return "KB_BAD_RESPONSE";
    }

    private String businessErrorMsg(Map<String, Object> body) {
        String errorMsg = stringValue(first(body, "error_msg", "message", "msg"));
        return errorMsg.isBlank() || "null".equalsIgnoreCase(errorMsg) ? "knowledge search failed" : errorMsg;
    }

    private Map<String, Object> toSearchPayload(RetrievalRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("request_id", request.getRequestId());
        payload.put("workflow_code", request.getWorkflowCode());
        payload.put("task_id", request.getTaskNo());
        payload.put("query", request.getQuery());
        payload.put("query_type", "audit_basis");
        payload.put("knowledge_scope", request.getKnowledgeScope());
        payload.put("caller_context", request.getCallerContext());
        payload.put("retrieval_config", request.getRetrievalConfig());
        payload.put("source_chunk_id", request.getSourceChunkId());
        return payload;
    }

    private Map<String, Object> toBatchPayload(BatchRetrievalRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("request_id", request.getRequestId());
        payload.put("workflow_code", request.getWorkflowCode());
        payload.put("task_id", request.getTaskNo());
        payload.put("knowledge_scope", request.getKnowledgeScope());
        payload.put("caller_context", request.getCallerContext());
        payload.put("retrieval_config", request.getRetrievalConfig());
        List<Map<String, Object>> queries = new ArrayList<>();
        for (RetrievalRequest query : request.getQueries()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("query_id", query.getRequestId());
            item.put("query", query.getQuery());
            item.put("source_chunk_id", query.getSourceChunkId());
            item.put("query_type", "audit_basis");
            queries.add(item);
        }
        payload.put("queries", queries);
        return payload;
    }

    private Object extractResults(Map<String, Object> body) {
        Object data = body.get("data");
        if (data instanceof Map<?, ?> dataMap) {
            return first(dataMap, "results", "references");
        }
        return data;
    }

    private List<RetrievalReference> parseReferences(Object rawResults, Long sourceChunkId) {
        List<RetrievalReference> references = new ArrayList<>();
        if (!(rawResults instanceof List<?> list)) {
            return references;
        }
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                RetrievalReference reference = new RetrievalReference();
                reference.setSourceChunkId(sourceChunkId);
                reference.setKbChunkId(stringValue(first(map, "chunk_id", "chunkId", "kb_chunk_id", "kbChunkId")));
                reference.setKbDocumentId(stringValue(first(map, "document_id", "documentId", "kb_document_id", "kbDocumentId")));
                reference.setResourceId(longValue(first(map, "resource_id", "resourceId")));
                reference.setResourceType(stringValue(first(map, "resource_type", "resourceType")));
                reference.setFileName(stringValue(first(map, "file_name", "fileName")));
                reference.setFileUrl(stringValue(first(map, "file_url", "fileUrl")));
                reference.setVersionNo(stringValue(first(map, "version_no", "versionNo", "current_version_no", "currentVersionNo")));
                reference.setPageNo(intValue(first(map, "page_no", "pageNo")));
                reference.setSectionTitle(stringValue(first(map, "section_title", "sectionTitle")));
                reference.setSectionPath(stringValue(first(map, "section_path", "sectionPath")));
                reference.setRuleCode(stringValue(first(map, "rule_code", "ruleCode")));
                reference.setChunkTextSnapshot(stringValue(first(map, "chunk_text", "chunkText", "content")));
                reference.setScore(decimalValue(first(map, "score")));
                reference.setRankScore(decimalValue(first(map, "rank_score", "rankScore")));
                reference.setEffectiveDate(dateValue(first(map, "effective_date", "effectiveDate")));
                reference.setStatus(stringValue(first(map, "status")));
                references.add(reference);
            }
        }
        return references;
    }

    private Object first(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Long longValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Integer intValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private LocalDate dateValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return LocalDate.parse(String.valueOf(value));
    }

    private String trimRight(String value) {
        if (value == null) {
            return "";
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
