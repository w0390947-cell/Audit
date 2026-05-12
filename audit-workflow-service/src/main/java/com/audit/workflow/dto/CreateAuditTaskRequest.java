package com.audit.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Map;

public class CreateAuditTaskRequest {

    @NotBlank
    @JsonAlias("workflow_code")
    private String workflowCode;
    @JsonAlias("biz_id")
    private String bizId;
    @JsonAlias("callback_url")
    private String callbackUrl;
    private Map<String, Object> input;

    public String getWorkflowCode() {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }
}
