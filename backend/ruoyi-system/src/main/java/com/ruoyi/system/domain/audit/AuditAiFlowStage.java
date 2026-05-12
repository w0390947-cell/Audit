package com.ruoyi.system.domain.audit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class AuditAiFlowStage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long stageId;

    private Long aiTaskId;

    private String runId;

    private String workflowTaskId;

    private String workflowTaskNo;

    private String stageCode;

    private String stageInstanceId;

    private String stageName;

    private String stageStatus;

    private String agentName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private Long durationMs;

    private String stageSummary;

    private String stageDetail;

    private String outputJson;

    private String errorMessage;

    private Integer sortNum;

    public Long getStageId()
    {
        return stageId;
    }

    public void setStageId(Long stageId)
    {
        this.stageId = stageId;
    }

    public Long getAiTaskId()
    {
        return aiTaskId;
    }

    public void setAiTaskId(Long aiTaskId)
    {
        this.aiTaskId = aiTaskId;
    }

    public String getRunId()
    {
        return runId;
    }

    public void setRunId(String runId)
    {
        this.runId = runId;
    }

    public String getWorkflowTaskId()
    {
        return workflowTaskId;
    }

    public void setWorkflowTaskId(String workflowTaskId)
    {
        this.workflowTaskId = workflowTaskId;
    }

    public String getWorkflowTaskNo()
    {
        return workflowTaskNo;
    }

    public void setWorkflowTaskNo(String workflowTaskNo)
    {
        this.workflowTaskNo = workflowTaskNo;
    }

    public String getStageCode()
    {
        return stageCode;
    }

    public void setStageCode(String stageCode)
    {
        this.stageCode = stageCode;
    }

    public String getStageInstanceId()
    {
        return stageInstanceId;
    }

    public void setStageInstanceId(String stageInstanceId)
    {
        this.stageInstanceId = stageInstanceId;
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

    public String getAgentName()
    {
        return agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public void setDurationMs(Long durationMs)
    {
        this.durationMs = durationMs;
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

    public String getOutputJson()
    {
        return outputJson;
    }

    public void setOutputJson(String outputJson)
    {
        this.outputJson = outputJson;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
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
