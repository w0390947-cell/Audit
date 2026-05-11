package com.ruoyi.system.domain.audit.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class AuditWorkflowCallback
{
    @JsonProperty("task_id")
    private Long taskId;

    @JsonProperty("task_no")
    private String taskNo;

    @JsonProperty("workflow_code")
    private String workflowCode;

    @JsonProperty("biz_id")
    private String bizId;

    @JsonProperty("task_status")
    private String taskStatus;

    private Map<String, Object> summary;

    @JsonProperty("result_url")
    private String resultUrl;

    @JsonProperty("finished_at")
    private String finishedAt;

    private Map<String, Object> error;

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

    public Map<String, Object> getSummary()
    {
        return summary;
    }

    public void setSummary(Map<String, Object> summary)
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

    public Map<String, Object> getError()
    {
        return error;
    }

    public void setError(Map<String, Object> error)
    {
        this.error = error;
    }
}
