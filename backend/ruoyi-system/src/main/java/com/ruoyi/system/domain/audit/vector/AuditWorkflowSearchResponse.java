package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuditWorkflowSearchResponse
{
    public static final int CODE_SUCCESS = 200;

    public static final int CODE_BAD_REQUEST = 400;

    public static final int CODE_PERMISSION_DENIED = 403;

    public static final int CODE_INDEX_NOT_READY = 409;

    public static final int CODE_UNAVAILABLE = 503;

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

    private AuditWorkflowSearchData data;

    public static AuditWorkflowSearchResponse success(AuditWorkflowSearchRequest request, AuditWorkflowSearchData data)
    {
        AuditWorkflowSearchResponse response = base(request);
        response.setCode(CODE_SUCCESS);
        response.setMessage("success");
        response.setData(data);
        return response;
    }

    public static AuditWorkflowSearchResponse error(AuditWorkflowSearchRequest request, int code, String errorCode,
            String message)
    {
        AuditWorkflowSearchResponse response = base(request);
        response.setCode(code);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }

    private static AuditWorkflowSearchResponse base(AuditWorkflowSearchRequest request)
    {
        AuditWorkflowSearchResponse response = new AuditWorkflowSearchResponse();
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

    public AuditWorkflowSearchData getData()
    {
        return data;
    }

    public void setData(AuditWorkflowSearchData data)
    {
        this.data = data;
    }
}
