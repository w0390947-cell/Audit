package com.audit.workflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateAuditTaskResponse {

    private Long taskId;
    private String taskNo;
    private String taskStatus;
    @JsonProperty("workflow_task_id")
    private String workflowTaskId;
    @JsonProperty("workflow_task_no")
    private String workflowTaskNo;
    private String status;
    private String message;

    public CreateAuditTaskResponse(Long taskId, String taskNo, String taskStatus) {
        this.taskId = taskId;
        this.taskNo = taskNo;
        this.taskStatus = taskStatus;
        this.workflowTaskId = String.valueOf(taskId);
        this.workflowTaskNo = taskNo;
        this.status = "accepted";
        this.message = "任务已接收";
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getWorkflowTaskId() {
        return workflowTaskId;
    }

    public void setWorkflowTaskId(String workflowTaskId) {
        this.workflowTaskId = workflowTaskId;
    }

    public String getWorkflowTaskNo() {
        return workflowTaskNo;
    }

    public void setWorkflowTaskNo(String workflowTaskNo) {
        this.workflowTaskNo = workflowTaskNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
