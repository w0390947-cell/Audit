package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AuditWorkflowSearchData
{
    private String query;

    @JsonProperty("query_type")
    private String queryType;

    private Integer total;

    private List<AuditWorkflowSearchHit> results = new ArrayList<>();

    @JsonProperty("ignored_filters")
    private List<String> ignoredFilters = new ArrayList<>();

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

    public Integer getTotal()
    {
        return total;
    }

    public void setTotal(Integer total)
    {
        this.total = total;
    }

    public List<AuditWorkflowSearchHit> getResults()
    {
        return results;
    }

    public void setResults(List<AuditWorkflowSearchHit> results)
    {
        this.results = results;
    }

    public List<String> getIgnoredFilters()
    {
        return ignoredFilters;
    }

    public void setIgnoredFilters(List<String> ignoredFilters)
    {
        this.ignoredFilters = ignoredFilters;
    }
}
