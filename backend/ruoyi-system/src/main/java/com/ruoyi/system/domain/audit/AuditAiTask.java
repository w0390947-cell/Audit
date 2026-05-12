package com.ruoyi.system.domain.audit;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class AuditAiTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long aiTaskId;

    private Long reviewTaskId;

    private Long reviewVersionId;

    private Long[] aiTaskIds;

    @Excel(name = "任务编号")
    private String taskNo;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "送检单位")
    private String deliveryUnit;

    @Excel(name = "提交人")
    private String submitter;

    @Excel(name = "优先级", dictType = "audit_review_priority")
    private String priority;

    @Excel(name = "队列位置")
    private Integer queuePosition;

    @Excel(name = "任务状态", dictType = "audit_ai_task_status")
    private String taskStatus;

    private String estimatedDuration;

    private Integer progressPercent;

    private String progressText;

    @Excel(name = "AI分析次数")
    private Integer aiAnalysisCount;

    private String reviewStatus;

    private String reportFileName;

    private String reportFileUrl;

    private String basisFileUrls;

    private String aiSummary;

    private String reviewOpinion;

    private String reviewer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    private String delFlag;

    private String keyword;

    private List<AuditAiFinding> findingList;

    private List<AuditAiFlowStage> flowStageList;

    public Long getAiTaskId()
    {
        return aiTaskId;
    }

    public void setAiTaskId(Long aiTaskId)
    {
        this.aiTaskId = aiTaskId;
    }

    public Long getReviewTaskId()
    {
        return reviewTaskId;
    }

    public void setReviewTaskId(Long reviewTaskId)
    {
        this.reviewTaskId = reviewTaskId;
    }

    public Long getReviewVersionId()
    {
        return reviewVersionId;
    }

    public void setReviewVersionId(Long reviewVersionId)
    {
        this.reviewVersionId = reviewVersionId;
    }

    public Long[] getAiTaskIds()
    {
        return aiTaskIds;
    }

    public void setAiTaskIds(Long[] aiTaskIds)
    {
        this.aiTaskIds = aiTaskIds;
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

    public String getPriority()
    {
        return priority;
    }

    public void setPriority(String priority)
    {
        this.priority = priority;
    }

    public Integer getQueuePosition()
    {
        return queuePosition;
    }

    public void setQueuePosition(Integer queuePosition)
    {
        this.queuePosition = queuePosition;
    }

    public String getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public String getEstimatedDuration()
    {
        return estimatedDuration;
    }

    public void setEstimatedDuration(String estimatedDuration)
    {
        this.estimatedDuration = estimatedDuration;
    }

    public Integer getProgressPercent()
    {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent)
    {
        this.progressPercent = progressPercent;
    }

    public String getProgressText()
    {
        return progressText;
    }

    public void setProgressText(String progressText)
    {
        this.progressText = progressText;
    }

    public Integer getAiAnalysisCount()
    {
        return aiAnalysisCount;
    }

    public void setAiAnalysisCount(Integer aiAnalysisCount)
    {
        this.aiAnalysisCount = aiAnalysisCount;
    }

    public String getReviewStatus()
    {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus)
    {
        this.reviewStatus = reviewStatus;
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

    public String getBasisFileUrls()
    {
        return basisFileUrls;
    }

    public void setBasisFileUrls(String basisFileUrls)
    {
        this.basisFileUrls = basisFileUrls;
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

    public String getReviewer()
    {
        return reviewer;
    }

    public void setReviewer(String reviewer)
    {
        this.reviewer = reviewer;
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

    public List<AuditAiFinding> getFindingList()
    {
        return findingList;
    }

    public void setFindingList(List<AuditAiFinding> findingList)
    {
        this.findingList = findingList;
    }

    public List<AuditAiFlowStage> getFlowStageList()
    {
        return flowStageList;
    }

    public void setFlowStageList(List<AuditAiFlowStage> flowStageList)
    {
        this.flowStageList = flowStageList;
    }
}
