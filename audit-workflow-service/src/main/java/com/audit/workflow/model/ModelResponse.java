package com.audit.workflow.model;

public class ModelResponse {

    private boolean success;
    private String requestId;
    private String provider;
    private String modelName;
    private String content;
    private int inputTokens;
    private int outputTokens;
    private String errorCode;
    private String errorMsg;
    private long durationMs;

    public static ModelResponse success(String requestId, String provider, String modelName, String content,
                                        int inputTokens, int outputTokens, long durationMs) {
        ModelResponse response = new ModelResponse();
        response.setSuccess(true);
        response.setRequestId(requestId);
        response.setProvider(provider);
        response.setModelName(modelName);
        response.setContent(content);
        response.setInputTokens(inputTokens);
        response.setOutputTokens(outputTokens);
        response.setDurationMs(durationMs);
        return response;
    }

    public static ModelResponse failure(String provider, String modelName, String errorCode, String errorMsg, long durationMs) {
        ModelResponse response = new ModelResponse();
        response.setSuccess(false);
        response.setProvider(provider);
        response.setModelName(modelName);
        response.setErrorCode(errorCode);
        response.setErrorMsg(errorMsg);
        response.setDurationMs(durationMs);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(int inputTokens) {
        this.inputTokens = inputTokens;
    }

    public int getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(int outputTokens) {
        this.outputTokens = outputTokens;
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

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }
}
