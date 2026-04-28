package com.ruoyi.system.domain.audit;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class AuditAssetAiStep
{
    private Long stepId;

    private Long versionId;

    private Integer stepNo;

    private String stepTitle;

    private String stepContent;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date stepTime;

    private Integer sortNum;

    public Long getStepId()
    {
        return stepId;
    }

    public void setStepId(Long stepId)
    {
        this.stepId = stepId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Integer getStepNo()
    {
        return stepNo;
    }

    public void setStepNo(Integer stepNo)
    {
        this.stepNo = stepNo;
    }

    public String getStepTitle()
    {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle)
    {
        this.stepTitle = stepTitle;
    }

    public String getStepContent()
    {
        return stepContent;
    }

    public void setStepContent(String stepContent)
    {
        this.stepContent = stepContent;
    }

    public Date getStepTime()
    {
        return stepTime;
    }

    public void setStepTime(Date stepTime)
    {
        this.stepTime = stepTime;
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
