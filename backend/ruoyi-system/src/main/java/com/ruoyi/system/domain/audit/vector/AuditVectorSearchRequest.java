package com.ruoyi.system.domain.audit.vector;

import java.math.BigDecimal;

public class AuditVectorSearchRequest
{
    private String query;

    private Long folderId;

    private Long[] folderIds;

    private Long[] resourceIds;

    private Integer topK;

    private BigDecimal minScore;

    private Boolean includeChunkText;

    private Integer maxChunkChars;

    private String[] knowledgeBaseCodes;

    private String[] categoryCodes;

    private String businessType;

    private Boolean effectiveOnly;

    private String asOfDate;

    private Boolean hybrid;

    private Boolean rerank;

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public Long getFolderId()
    {
        return folderId;
    }

    public void setFolderId(Long folderId)
    {
        this.folderId = folderId;
    }

    public Long[] getFolderIds()
    {
        return folderIds;
    }

    public void setFolderIds(Long[] folderIds)
    {
        this.folderIds = folderIds;
    }

    public Long[] getResourceIds()
    {
        return resourceIds;
    }

    public void setResourceIds(Long[] resourceIds)
    {
        this.resourceIds = resourceIds;
    }

    public Integer getTopK()
    {
        return topK;
    }

    public void setTopK(Integer topK)
    {
        this.topK = topK;
    }

    public BigDecimal getMinScore()
    {
        return minScore;
    }

    public void setMinScore(BigDecimal minScore)
    {
        this.minScore = minScore;
    }

    public Boolean getIncludeChunkText()
    {
        return includeChunkText;
    }

    public void setIncludeChunkText(Boolean includeChunkText)
    {
        this.includeChunkText = includeChunkText;
    }

    public Integer getMaxChunkChars()
    {
        return maxChunkChars;
    }

    public void setMaxChunkChars(Integer maxChunkChars)
    {
        this.maxChunkChars = maxChunkChars;
    }

    public String[] getKnowledgeBaseCodes()
    {
        return knowledgeBaseCodes;
    }

    public void setKnowledgeBaseCodes(String[] knowledgeBaseCodes)
    {
        this.knowledgeBaseCodes = knowledgeBaseCodes;
    }

    public String[] getCategoryCodes()
    {
        return categoryCodes;
    }

    public void setCategoryCodes(String[] categoryCodes)
    {
        this.categoryCodes = categoryCodes;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public Boolean getEffectiveOnly()
    {
        return effectiveOnly;
    }

    public void setEffectiveOnly(Boolean effectiveOnly)
    {
        this.effectiveOnly = effectiveOnly;
    }

    public String getAsOfDate()
    {
        return asOfDate;
    }

    public void setAsOfDate(String asOfDate)
    {
        this.asOfDate = asOfDate;
    }

    public Boolean getHybrid()
    {
        return hybrid;
    }

    public void setHybrid(Boolean hybrid)
    {
        this.hybrid = hybrid;
    }

    public Boolean getRerank()
    {
        return rerank;
    }

    public void setRerank(Boolean rerank)
    {
        this.rerank = rerank;
    }
}
