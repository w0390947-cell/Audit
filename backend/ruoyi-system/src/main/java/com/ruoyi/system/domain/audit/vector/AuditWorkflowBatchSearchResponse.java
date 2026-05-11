package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuditWorkflowBatchSearchResponse
{
    private int code;

    private String message;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("workflow_code")
    private String workflowCode;

    @JsonProperty("task_id")
    private String taskId;

    private AuditWorkflowBatchSearchData data;

    public static AuditWorkflowBatchSearchResponse success(AuditWorkflowBatchSearchRequest request,
            AuditWorkflowBatchSearchData data)
    {
        AuditWorkflowBatchSearchResponse response = base(request);
        response.setCode(AuditWorkflowSearchResponse.CODE_SUCCESS);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static AuditWorkflowBatchSearchResponse error(AuditWorkflowBatchSearchRequest request, int code,
            String errorCode, String message)
    {
        AuditWorkflowBatchSearchResponse response = base(request);
        response.setCode(code);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }

    private static AuditWorkflowBatchSearchResponse base(AuditWorkflowBatchSearchRequest request)
    {
        AuditWorkflowBatchSearchResponse response = new AuditWorkflowBatchSearchResponse();
        if (request != null)
        {
            response.setRequestId(request.getRequestId());
            response.setWorkflowCode(request.getWorkflowCode());
            response.setTaskId(request.getTaskId());
        }
        return response;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getWorkflowCode()
    {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode)
    {
        this.workflowCode = workflowCode;
    }

    public String getTaskId()
    {
        return taskId;
    }

    public void setTaskId(String taskId)
    {
        this.taskId = taskId;
    }

    public AuditWorkflowBatchSearchData getData()
    {
        return data;
    }

    public void setData(AuditWorkflowBatchSearchData data)
    {
        this.data = data;
    }
}
