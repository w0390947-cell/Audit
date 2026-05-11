package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AuditWorkflowBatchQueryResult
{
    private String queryId;

    private String sourceChunkId;

    private String query;

    private String queryType;

    private String errorCode;

    private String errorMessage;

    private List<AuditWorkflowSearchHit> hits = new ArrayList<>();

    @JsonProperty("query_id")
    public String getQueryId()
    {
        return queryId;
    }

    public void setQueryId(String queryId)
    {
        this.queryId = queryId;
    }

    @JsonProperty("source_chunk_id")
    public String getSourceChunkId()
    {
        return sourceChunkId;
    }

    public void setSourceChunkId(String sourceChunkId)
    {
        this.sourceChunkId = sourceChunkId;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    @JsonProperty("query_type")
    public String getQueryType()
    {
        return queryType;
    }

    public void setQueryType(String queryType)
    {
        this.queryType = queryType;
    }

    @JsonProperty("error_code")
    public String getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }

    @JsonProperty("error_message")
    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    public List<AuditWorkflowSearchHit> getHits()
    {
        return hits;
    }

    public void setHits(List<AuditWorkflowSearchHit> hits)
    {
        this.hits = hits;
    }

    @JsonProperty("results")
    public List<AuditWorkflowSearchHit> getResults()
    {
        return hits;
    }
}
