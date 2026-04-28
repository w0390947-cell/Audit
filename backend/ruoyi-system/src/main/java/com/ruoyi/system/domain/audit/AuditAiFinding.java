package com.ruoyi.system.domain.audit;

import java.io.Serializable;

public class AuditAiFinding implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long findingId;

    private Long aiTaskId;

    private String findingType;

    private String findingTitle;

    private String findingContent;

    private Integer sortNum;

    public Long getFindingId()
    {
        return findingId;
    }

    public void setFindingId(Long findingId)
    {
        this.findingId = findingId;
    }

    public Long getAiTaskId()
    {
        return aiTaskId;
    }

    public void setAiTaskId(Long aiTaskId)
    {
        this.aiTaskId = aiTaskId;
    }

    public String getFindingType()
    {
        return findingType;
    }

    public void setFindingType(String findingType)
    {
        this.findingType = findingType;
    }

    public String getFindingTitle()
    {
        return findingTitle;
    }

    public void setFindingTitle(String findingTitle)
    {
        this.findingTitle = findingTitle;
    }

    public String getFindingContent()
    {
        return findingContent;
    }

    public void setFindingContent(String findingContent)
    {
        this.findingContent = findingContent;
    }

    public Integer getSortNum()
    {
        return sortNum;
    }

    public void setSortNum(Integer sortNum)
    {
        this.sortNum = sortNum;
    }
}
