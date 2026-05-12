package com.ruoyi.system.domain.audit.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class AuditWorkflowCallback
{
    @JsonProperty("task_id")
    private Long taskId;

    @JsonProperty("task_no")
    private String taskNo;

    @JsonProperty("workflow_code")
    private String workflowCode;

    @JsonProperty("workflow_task_id")
    private String workflowTaskId;

    @JsonProperty("workflow_task_no")
    private String workflowTaskNo;

    @JsonProperty("workflow_name")
    private String workflowName;

    @JsonProperty("biz_id")
    private String bizId;

    @JsonProperty("task_status")
    private String taskStatus;

    private String status;

    @JsonProperty("progress_percent")
    private Integer progressPercent;

    @JsonProperty("progress_text")
    private String progressText;

    @JsonProperty("started_at")
    private String startedAt;

    private Object summary;

    @JsonProperty("result_url")
    private String resultUrl;

    @JsonProperty("finished_at")
    private String finishedAt;

    @JsonProperty("duration_ms")
    private Long durationMs;

    private List<AuditWorkflowStage> stages;

    private Map<String, Object> result;

    private Map<String, Object> error;

    @JsonProperty("callback_event_id")
    private String callbackEventId;

    @JsonProperty("callback_time")
    private String callbackTime;

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

    public String getWorkflowCode()
    {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode)
    {
        this.workflowCode = workflowCode;
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

    public String getWorkflowName()
    {
        return workflowName;
    }

    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }

    public String getBizId()
    {
        return bizId;
    }

    public void setBizId(String bizId)
    {
        this.bizId = bizId;
    }

    public String getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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

    public String getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(String startedAt)
    {
        this.startedAt = startedAt;
    }

    public Object getSummary()
    {
        return summary;
    }

    public void setSummary(Object summary)
    {
        this.summary = summary;
    }

    public String getResultUrl()
    {
        return resultUrl;
    }

    public void setResultUrl(String resultUrl)
    {
        this.resultUrl = resultUrl;
    }

    public String getFinishedAt()
    {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt)
    {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public void setDurationMs(Long durationMs)
    {
        this.durationMs = durationMs;
    }

    public List<AuditWorkflowStage> getStages()
    {
        return stages;
    }

    public void setStages(List<AuditWorkflowStage> stages)
    {
        this.stages = stages;
    }

    public Map<String, Object> getResult()
    {
        return result;
    }

    public void setResult(Map<String, Object> result)
    {
        this.result = result;
    }

    public Map<String, Object> getError()
    {
        return error;
    }

    public void setError(Map<String, Object> error)
    {
        this.error = error;
    }

    public String getCallbackEventId()
    {
        return callbackEventId;
    }

    public void setCallbackEventId(String callbackEventId)
    {
        this.callbackEventId = callbackEventId;
    }

    public String getCallbackTime()
    {
        return callbackTime;
    }

    public void setCallbackTime(String callbackTime)
    {
        this.callbackTime = callbackTime;
    }
}
