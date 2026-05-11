package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

public class AuditRetrievalConfig
{
    @JsonAlias("top_k")
    private Integer topK;

    @JsonAlias("min_score")
    private BigDecimal minScore;

    private Boolean hybrid;

    private Boolean rerank;

    @JsonAlias("include_chunk_text")
    private Boolean includeChunkText;

    @JsonAlias("max_chunk_chars")
    private Integer maxChunkChars;

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
}
