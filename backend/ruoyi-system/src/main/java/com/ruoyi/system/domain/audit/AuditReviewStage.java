package com.ruoyi.system.domain.audit;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class AuditReviewStage implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long stageId;

    private Long versionId;

    private String stageCode;

    private String stageName;

    private String stageStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date stageTime;

    private String stageSummary;

    private String stageDetail;

    private Integer sortNum;

    public Long getStageId()
    {
        return stageId;
    }

    public void setStageId(Long stageId)
    {
        this.stageId = stageId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public String getStageCode()
    {
        return stageCode;
    }

    public void setStageCode(String stageCode)
    {
        this.stageCode = stageCode;
    }

    public String getStageName()
    {
        return stageName;
    }

    public void setStageName(String stageName)
    {
        this.stageName = stageName;
    }

    public String getStageStatus()
    {
        return stageStatus;
    }

    public void setStageStatus(String stageStatus)
    {
        this.stageStatus = stageStatus;
    }

    public Date getStageTime()
    {
        return stageTime;
    }

    public void setStageTime(Date stageTime)
    {
        this.stageTime = stageTime;
    }

    public String getStageSummary()
    {
        return stageSummary;
    }

    public void setStageSummary(String stageSummary)
    {
        this.stageSummary = stageSummary;
    }

    public String getStageDetail()
    {
        return stageDetail;
    }

    public void setStageDetail(String stageDetail)
    {
        this.stageDetail = stageDetail;
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
