package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AuditWorkflowBatchQuery
{
    @JsonAlias("query_id")
    private String queryId;

    private String query;

    @JsonAlias("source_chunk_id")
    private String sourceChunkId;

    @JsonAlias("query_type")
    private String queryType;

    public String getQueryId()
    {
        return queryId;
    }

    public void setQueryId(String queryId)
    {
        this.queryId = queryId;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getSourceChunkId()
    {
        return sourceChunkId;
    }

    public void setSourceChunkId(String sourceChunkId)
    {
        this.sourceChunkId = sourceChunkId;
    }

    public String getQueryType()
    {
        return queryType;
    }

    public void setQueryType(String queryType)
    {
        this.queryType = queryType;
    }
}
