package com.ruoyi.system.domain.audit.vector;

import java.util.ArrayList;
import java.util.List;

public class AuditVectorSearchResult
{
    private String query;

    private Integer topK;

    private Integer total;

    private List<AuditVectorSearchHit> hits = new ArrayList<>();

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public Integer getTopK()
    {
        return topK;
    }

    public void setTopK(Integer topK)
    {
        this.topK = topK;
    }

    public Integer getTotal()
    {
        return total;
    }

    public void setTotal(Integer total)
    {
        this.total = total;
    }

    public List<AuditVectorSearchHit> getHits()
    {
        return hits;
    }

    public void setHits(List<AuditVectorSearchHit> hits)
    {
        this.hits = hits;
    }
}
