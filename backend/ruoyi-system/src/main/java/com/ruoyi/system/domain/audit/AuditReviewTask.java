package com.ruoyi.system.domain.audit;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class AuditReviewTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;

    @Excel(name = "任务编号")
    private String taskNo;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "送检单位")
    private String deliveryUnit;

    @Excel(name = "发起人")
    private String sponsor;

    private String handlerName;

    @Excel(name = "优先级", dictType = "audit_review_priority")
    private String priority;

    private Integer aiAnalysisCount;

    private String taskStatus;

    @Excel(name = "审核状态", dictType = "audit_review_status")
    private String reviewStatus;

    private String processFlag;

    private String currentVersionNo;

    private String mainReportUrls;

    private String basisFileUrls;

    private String appendixFileUrls;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private String delFlag;

    private String keyword;

    private AuditReviewVersion currentVersion;

    private List<AuditReviewVersion> versionList;

    private List<AuditReviewIssue> issueList;

    private List<AuditReviewStage> stageList;

    private List<AuditUploadedFile> basisUploadedFiles;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getTaskNo()
    {
        return taskNo;
    }

    public void setTaskNo(String taskNo)
    {
        this.taskNo = taskNo;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getDeliveryUnit()
    {
        return deliveryUnit;
    }

    public void setDeliveryUnit(String deliveryUnit)
    {
        this.deliveryUnit = deliveryUnit;
    }

    public String getSponsor()
    {
        return sponsor;
    }

    public void setSponsor(String sponsor)
    {
        this.sponsor = sponsor;
    }

    public String getHandlerName()
    {
        return handlerName;
    }

    public void setHandlerName(String handlerName)
    {
        this.handlerName = handlerName;
    }

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public Integer getAiAnalysisCount()
    {
        return aiAnalysisCount;
    }

    public void setAiAnalysisCount(Integer aiAnalysisCount)
    {
        this.aiAnalysisCount = aiAnalysisCount;
    }

    public String getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public String getReviewStatus()
    {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus)
    {
        this.reviewStatus = reviewStatus;
    }

    public String getProcessFlag()
    {
        return processFlag;
    }

    public void setProcessFlag(String processFlag)
    {
        this.processFlag = processFlag;
    }

    public String getCurrentVersionNo()
    {
        return currentVersionNo;
    }

    public void setCurrentVersionNo(String currentVersionNo)
    {
        this.currentVersionNo = currentVersionNo;
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

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public AuditReviewVersion getCurrentVersion()
    {
        return currentVersion;
    }

    public void setCurrentVersion(AuditReviewVersion currentVersion)
    {
        this.currentVersion = currentVersion;
    }

    public List<AuditReviewVersion> getVersionList()
    {
        return versionList;
    }

    public void setVersionList(List<AuditReviewVersion> versionList)
    {
        this.versionList = versionList;
    }

    public List<AuditReviewIssue> getIssueList()
    {
        return issueList;
    }

    public void setIssueList(List<AuditReviewIssue> issueList)
    {
        this.issueList = issueList;
    }

    public List<AuditReviewStage> getStageList()
    {
        return stageList;
    }

    public void setStageList(List<AuditReviewStage> stageList)
    {
        this.stageList = stageList;
    }

    public List<AuditUploadedFile> getBasisUploadedFiles()
    {
        return basisUploadedFiles;
    }

    public void setBasisUploadedFiles(List<AuditUploadedFile> basisUploadedFiles)
    {
        this.basisUploadedFiles = basisUploadedFiles;
    }
}
