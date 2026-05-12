package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.util.List;

public class RerankResponse
{
    private List<Result> results;

    public List<Result> getResults()
    {
        return results;
    }

    public void setResults(List<Result> results)
    {
        this.results = results;
    }

    public static class Result
    {
        private Integer index;

        @JsonAlias("relevance_score")
        private BigDecimal relevanceScore;

        public Integer getIndex()
        {
            return index;
        }

        public void setIndex(Integer index)
        {
            this.index = index;
        }

        public BigDecimal getRelevanceScore()
        {
            return relevanceScore;
        }

        public void setRelevanceScore(BigDecimal relevanceScore)
        {
            this.relevanceScore = relevanceScore;
        }
    }
}
