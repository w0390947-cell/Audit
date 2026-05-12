package com.audit.workflow.retrieval;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BatchRetrievalRequest {

    private String requestId;
    private String workflowCode;
    private String taskNo;
    private Long taskId;
    private List<RetrievalRequest> queries = new ArrayList<>();
    private Map<String, Object> knowledgeScope = new LinkedHashMap<>();
    private Map<String, Object> callerContext = new LinkedHashMap<>();
    private Map<String, Object> retrievalConfig = new LinkedHashMap<>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getWorkflowCode() {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public List<RetrievalRequest> getQueries() {
        return queries;
    }

    public void setQueries(List<RetrievalRequest> queries) {
        this.queries = queries;
    }

    public Map<String, Object> getKnowledgeScope() {
        return knowledgeScope;
    }

    public void setKnowledgeScope(Map<String, Object> knowledgeScope) {
        this.knowledgeScope = knowledgeScope;
    }

    public Map<String, Object> getCallerContext() {
        return callerContext;
    }

    public void setCallerContext(Map<String, Object> callerContext) {
        this.callerContext = callerContext;
    }

    public Map<String, Object> getRetrievalConfig() {
        return retrievalConfig;
    }

    public void setRetrievalConfig(Map<String, Object> retrievalConfig) {
        this.retrievalConfig = retrievalConfig;
    }
}
