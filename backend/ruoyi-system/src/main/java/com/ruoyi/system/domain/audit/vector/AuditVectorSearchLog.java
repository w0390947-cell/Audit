package com.ruoyi.system.domain.audit.vector;

import com.ruoyi.common.core.domain.BaseEntity;

public class AuditVectorSearchLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long logId;

    private String requestId;

    private String workflowCode;

    private String taskId;

    private Integer queryCount;

    private String permissionMode;

    private String scopeSummary;

    private String retrievalConfig;

    private Integer resultCount;

    private String topResourceIds;

    private String status;

    private String errorCode;

    private String errorMsg;

    private Long costMs;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
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

    public Integer getQueryCount()
    {
        return queryCount;
    }

    public void setQueryCount(Integer queryCount)
    {
        this.queryCount = queryCount;
    }

    public String getPermissionMode()
    {
        return permissionMode;
    }

    public void setPermissionMode(String permissionMode)
    {
        this.permissionMode = permissionMode;
    }

    public String getScopeSummary()
    {
        return scopeSummary;
    }

    public void setScopeSummary(String scopeSummary)
    {
        this.scopeSummary = scopeSummary;
    }

    public String getRetrievalConfig()
    {
        return retrievalConfig;
    }

    public void setRetrievalConfig(String retrievalConfig)
    {
        this.retrievalConfig = retrievalConfig;
    }

    public Integer getResultCount()
    {
        return resultCount;
    }

    public void setResultCount(Integer resultCount)
    {
        this.resultCount = resultCount;
    }

    public String getTopResourceIds()
    {
        return topResourceIds;
    }

    public void setTopResourceIds(String topResourceIds)
    {
        this.topResourceIds = topResourceIds;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(String errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public Long getCostMs()
    {
        return costMs;
    }

    public void setCostMs(Long costMs)
    {
        this.costMs = costMs;
    }
}
