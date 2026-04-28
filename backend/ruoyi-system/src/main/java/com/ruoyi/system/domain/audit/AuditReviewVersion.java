package com.ruoyi.system.domain.audit;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

public class AuditReviewVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long versionId;

    private Long taskId;

    private String versionNo;

    private String reportFileName;

    private String reportFileUrl;

    private String mainReportUrls;

    private String basisFileUrls;

    private String appendixFileUrls;

    private String detectStatus;

    private String submitter;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private String aiSummary;

    private String reviewOpinion;

    private String currentFlag;

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(String versionNo)
    {
        this.versionNo = versionNo;
    }

    public String getReportFileName()
    {
        return reportFileName;
    }

    public void setReportFileName(String reportFileName)
    {
        this.reportFileName = reportFileName;
    }

    public String getReportFileUrl()
    {
        return reportFileUrl;
    }

    public void setReportFileUrl(String reportFileUrl)
    {
        this.reportFileUrl = reportFileUrl;
    }

    public String getMainReportUrls()
    {
        return mainReportUrls;
    }

    public void setMainReportUrls(String mainReportUrls)
    {
        this.mainReportUrls = mainReportUrls;
    }

    public String getBasisFileUrls()
    {
        return basisFileUrls;
    }

    public void setBasisFileUrls(String basisFileUrls)
    {
        this.basisFileUrls = basisFileUrls;
    }

    public String getAppendixFileUrls()
    {
        return appendixFileUrls;
    }

    public void setAppendixFileUrls(String appendixFileUrls)
    {
        this.appendixFileUrls = appendixFileUrls;
    }

    public String getDetectStatus()
    {
        return detectStatus;
    }

    public void setDetectStatus(String detectStatus)
    {
        this.detectStatus = detectStatus;
    }

    public String getSubmitter()
    {
        return submitter;
    }

    public void setSubmitter(String submitter)
    {
        this.submitter = submitter;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public String getAiSummary()
    {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary)
    {
        this.aiSummary = aiSummary;
    }

    public String getReviewOpinion()
    {
        return reviewOpinion;
    }

    public void setReviewOpinion(String reviewOpinion)
    {
        this.reviewOpinion = reviewOpinion;
    }

    public String getCurrentFlag()
    {
        return currentFlag;
    }

    public void setCurrentFlag(String currentFlag)
    {
        this.currentFlag = currentFlag;
    }
}
