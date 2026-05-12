package com.ruoyi.system.domain.audit.vector;

import java.math.BigDecimal;

public class RerankResult
{
    private Integer index;

    private BigDecimal score;

    public Integer getIndex()
    {
        return index;
    }

    public void setIndex(Integer index)
    {
        this.index = index;
    }

    public BigDecimal getScore()
    {
        return score;
    }

    public void setScore(BigDecimal score)
    {
        this.score = score;
    }
}
