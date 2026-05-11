package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AuditWorkflowSearchRequest
{
    @JsonAlias("request_id")
    private String requestId;

    @JsonAlias("workflow_code")
    private String workflowCode;

    @JsonAlias("task_id")
    private String taskId;

    private String query;

    @JsonAlias("query_type")
    private String queryType;

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

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getQueryType()
    {
        return queryType;
    }

    public void setQueryType(String queryType)
    {
        this.queryType = queryType;
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
