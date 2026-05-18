package com.ruoyi.system.domain.audit;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class AuditAssetRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long assetId;

    private Long reviewTaskId;

    private Long libraryResourceId;

    private Long[] assetIds;

    @Excel(name = "任务编号")
    private String taskNo;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "送检单位")
    private String deliveryUnit;

    @Excel(name = "提交人")
    private String submitter;

    @Excel(name = "审核人")
    private String reviewer;

    private String permissionOwner;

    @Excel(name = "AI分析次数")
    private Integer aiAnalysisCount;

    private String currentAiVersion;

    @Excel(name = "审核状态", dictType = "audit_review_status")
    private String reviewStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "审核时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    private String reportFileName;

    private String reportFileUrl;

    private String aiOpinion;

    private String finalOpinion;

    private String delFlag;

    private String keyword;

    private AuditAssetAiVersion currentVersion;

    private List<AuditAssetAiVersion> versionList;

    private List<AuditAssetResubmitRecord> resubmitRecordList;

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public Long getReviewTaskId()
    {
        return reviewTaskId;
    }

    public void setReviewTaskId(Long reviewTaskId)
    {
        this.reviewTaskId = reviewTaskId;
    }

    public Long getLibraryResourceId()
    {
        return libraryResourceId;
    }

    public void setLibraryResourceId(Long libraryResourceId)
    {
        this.libraryResourceId = libraryResourceId;
    }

    public Long[] getAssetIds()
    {
        return assetIds;
    }

    public void setAssetIds(Long[] assetIds)
    {
        this.assetIds = assetIds;
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

    public String getSubmitter()
    {
        return submitter;
    }

    public void setSubmitter(String submitter)
    {
        this.submitter = submitter;
    }

    public String getReviewer()
    {
        return reviewer;
    }

    public void setReviewer(String reviewer)
    {
        this.reviewer = reviewer;
    }

    public String getPermissionOwner()
    {
        return permissionOwner;
    }

    public void setPermissionOwner(String permissionOwner)
    {
        this.permissionOwner = permissionOwner;
    }

    public Integer getAiAnalysisCount()
    {
        return aiAnalysisCount;
    }

    public void setAiAnalysisCount(Integer aiAnalysisCount)
    {
        this.aiAnalysisCount = aiAnalysisCount;
    }

    public String getCurrentAiVersion()
    {
        return currentAiVersion;
    }

    public void setCurrentAiVersion(String currentAiVersion)
    {
        this.currentAiVersion = currentAiVersion;
    }

    public String getReviewStatus()
    {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus)
    {
        this.reviewStatus = reviewStatus;
    }

    public Date getReviewTime()
    {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime)
    {
        this.reviewTime = reviewTime;
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

    public String getAiOpinion()
    {
        return aiOpinion;
    }

    public void setAiOpinion(String aiOpinion)
    {
        this.aiOpinion = aiOpinion;
    }

    public String getFinalOpinion()
    {
        return finalOpinion;
    }

    public void setFinalOpinion(String finalOpinion)
    {
        this.finalOpinion = finalOpinion;
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

    public AuditAssetAiVersion getCurrentVersion()
    {
        return currentVersion;
    }

    public void setCurrentVersion(AuditAssetAiVersion currentVersion)
    {
        this.currentVersion = currentVersion;
    }

    public List<AuditAssetAiVersion> getVersionList()
    {
        return versionList;
    }

    public void setVersionList(List<AuditAssetAiVersion> versionList)
    {
        this.versionList = versionList;
    }

    public List<AuditAssetResubmitRecord> getResubmitRecordList()
    {
        return resubmitRecordList;
    }

    public void setResubmitRecordList(List<AuditAssetResubmitRecord> resubmitRecordList)
    {
        this.resubmitRecordList = resubmitRecordList;
    }
}
