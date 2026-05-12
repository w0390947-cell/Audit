package com.audit.workflow.dto;

public class RetryTaskResponse {

    private Long taskId;
    private String taskStatus;
    private Integer retryCount;

    public RetryTaskResponse(Long taskId, String taskStatus, Integer retryCount) {
        this.taskId = taskId;
        this.taskStatus = taskStatus;
        this.retryCount = retryCount;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }
}
