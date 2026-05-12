package com.audit.workflow.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class NodeExecutionResult {

    private boolean success;
    private String errorCode;
    private String errorMsg;
    private Map<String, Object> output = new LinkedHashMap<>();

    public static NodeExecutionResult success(Map<String, Object> output) {
        NodeExecutionResult result = new NodeExecutionResult();
        result.setSuccess(true);
        result.setOutput(output == null ? new LinkedHashMap<>() : output);
        return result;
    }

    public static NodeExecutionResult failure(String errorCode, String errorMsg) {
        NodeExecutionResult result = new NodeExecutionResult();
        result.setSuccess(false);
        result.setErrorCode(errorCode);
        result.setErrorMsg(errorMsg);
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }
}
