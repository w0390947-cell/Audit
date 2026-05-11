package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.ArrayList;
import java.util.List;

public class AuditWorkflowBatchSearchRequest
{
    @JsonAlias("request_id")
    private String requestId;

    @JsonAlias("workflow_code")
    private String workflowCode;

    @JsonAlias("task_id")
    private String taskId;

    private List<AuditWorkflowBatchQuery> queries = new ArrayList<>();

    @JsonAlias("knowledge_scope")
    private AuditKnowledgeScope knowledgeScope;

    @JsonAlias("caller_context")
    private AuditWorkflowCallerContext callerContext;

    @JsonAlias("retrieval_config")
    private AuditRetrievalConfig retrievalConfig;

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getWorkflowCode()
    {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode)
    {
        this.workflowCode = workflowCode;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public List<AuditWorkflowBatchQuery> getQueries()
    {
        return queries;
    }

    public void setQueries(List<AuditWorkflowBatchQuery> queries)
    {
        this.queries = queries;
    }

    public AuditKnowledgeScope getKnowledgeScope()
    {
        return knowledgeScope;
    }

    public void setKnowledgeScope(AuditKnowledgeScope knowledgeScope)
    {
        this.knowledgeScope = knowledgeScope;
    }

    public AuditWorkflowCallerContext getCallerContext()
    {
        return callerContext;
    }

    public void setCallerContext(AuditWorkflowCallerContext callerContext)
    {
        this.callerContext = callerContext;
    }

    public AuditRetrievalConfig getRetrievalConfig()
    {
        return retrievalConfig;
    }

    public void setRetrievalConfig(AuditRetrievalConfig retrievalConfig)
    {
        this.retrievalConfig = retrievalConfig;
    }
}
