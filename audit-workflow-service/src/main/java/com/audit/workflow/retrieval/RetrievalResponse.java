package com.audit.workflow.retrieval;

import com.audit.workflow.domain.RetrievalReference;

import java.util.ArrayList;
import java.util.List;

public class RetrievalResponse {

    private String requestId;
    private boolean success;
    private String errorCode;
    private String errorMsg;
    private List<RetrievalReference> references = new ArrayList<>();

    public static RetrievalResponse success(String requestId, List<RetrievalReference> references) {
        RetrievalResponse response = new RetrievalResponse();
        response.setRequestId(requestId);
        response.setSuccess(true);
        response.setReferences(references == null ? new ArrayList<>() : references);
        return response;
    }

    public static RetrievalResponse failure(String requestId, String errorCode, String errorMsg) {
        RetrievalResponse response = new RetrievalResponse();
        response.setRequestId(requestId);
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setErrorMsg(errorMsg);
        return response;
    }

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

    public List<RetrievalReference> getReferences() {
        return references;
    }

    public void setReferences(List<RetrievalReference> references) {
        this.references = references;
    }
}
