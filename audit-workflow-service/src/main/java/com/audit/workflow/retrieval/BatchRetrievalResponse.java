package com.audit.workflow.retrieval;

import java.util.LinkedHashMap;
import java.util.Map;

public class BatchRetrievalResponse {

    private String requestId;
    private boolean success;
    private String errorCode;
    private String errorMsg;
    private Map<Long, RetrievalResponse> responses = new LinkedHashMap<>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
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

    public Map<Long, RetrievalResponse> getResponses() {
        return responses;
    }

    public void setResponses(Map<Long, RetrievalResponse> responses) {
        this.responses = responses;
    }
}
