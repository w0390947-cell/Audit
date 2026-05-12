package com.ruoyi.system.domain.audit.workflow;

public class AuditWorkflowCallbackEvent
{
    private Long eventId;

    private String callbackEventId;

    private Long aiTaskId;

    private String workflowTaskId;

    private String eventStatus;

    private String rawPayload;

    private String errorMessage;

    public Long getEventId()
    {
        return eventId;
    }

    public void setEventId(Long eventId)
    {
        this.eventId = eventId;
    }

    public String getCallbackEventId()
    {
        return callbackEventId;
    }

    public void setCallbackEventId(String callbackEventId)
    {
        this.callbackEventId = callbackEventId;
    }

    public Long getAiTaskId()
    {
        return aiTaskId;
    }

    public void setAiTaskId(Long aiTaskId)
    {
        this.aiTaskId = aiTaskId;
    }

    public String getWorkflowTaskId()
    {
        return workflowTaskId;
    }

    public void setWorkflowTaskId(String workflowTaskId)
    {
        this.workflowTaskId = workflowTaskId;
    }

    public String getEventStatus()
    {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus)
    {
        this.eventStatus = eventStatus;
    }

    public String getRawPayload()
    {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload)
    {
        this.rawPayload = rawPayload;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }
}
