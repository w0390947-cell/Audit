package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AuditWorkflowBatchSearchData
{
    @JsonProperty("total_queries")
    private Integer totalQueries;

    @JsonProperty("total_hits")
    private Integer totalHits;

    private List<AuditWorkflowBatchQueryResult> results = new ArrayList<>();

    public Integer getTotalQueries()
    {
        return totalQueries;
    }

    public void setTotalQueries(Integer totalQueries)
    {
        this.totalQueries = totalQueries;
    }

    public Integer getTotalHits()
    {
        return totalHits;
    }

    public void setTotalHits(Integer totalHits)
    {
        this.totalHits = totalHits;
    }

    public List<AuditWorkflowBatchQueryResult> getResults()
    {
        return results;
    }

    public void setResults(List<AuditWorkflowBatchQueryResult> results)
    {
        this.results = results;
    }

    @JsonProperty("groups")
    public List<AuditWorkflowBatchQueryResult> getGroups()
    {
        return results;
    }
}
