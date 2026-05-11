package com.ruoyi.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "audit-workflow")
public class AuditWorkflowProperties
{
    private boolean enabled = false;

    private String baseUrl = "http://127.0.0.1:8080";

    private String workflowCode = "policy_document_audit";

    private String uploadedBasisWorkflowCode = "uploaded_basis_document_audit";

    private String callbackUrl;

    private String callbackToken;

    private String apiToken;

    private String publicFileBaseUrl;

    private String[] knowledgeBaseCodes = new String[0];

    private String permissionMode = "explicit_scope";

    private int connectTimeout = 10000;

    private int readTimeout = 60000;

    private int pollIntervalMs = 3000;

    private int pollTimeoutMs = 300000;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getBaseUrl()
    {
        return normalizeBaseUrl(baseUrl);
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getWorkflowCode()
    {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode)
    {
        this.workflowCode = workflowCode;
    }

    public String getUploadedBasisWorkflowCode()
    {
        return uploadedBasisWorkflowCode;
    }

    public void setUploadedBasisWorkflowCode(String uploadedBasisWorkflowCode)
    {
        this.uploadedBasisWorkflowCode = uploadedBasisWorkflowCode;
    }

    public String getCallbackUrl()
    {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl)
    {
        this.callbackUrl = callbackUrl;
    }

    public String getCallbackToken()
    {
        return callbackToken;
    }

    public void setCallbackToken(String callbackToken)
    {
        this.callbackToken = callbackToken;
    }

    public String getApiToken()
    {
        return apiToken;
    }

    public void setApiToken(String apiToken)
    {
        this.apiToken = apiToken;
    }

    public String getPublicFileBaseUrl()
    {
        return normalizeBaseUrl(publicFileBaseUrl);
    }

    public void setPublicFileBaseUrl(String publicFileBaseUrl)
    {
        this.publicFileBaseUrl = publicFileBaseUrl;
    }

    public String[] getKnowledgeBaseCodes()
    {
        return knowledgeBaseCodes;
    }

    public void setKnowledgeBaseCodes(String[] knowledgeBaseCodes)
    {
        this.knowledgeBaseCodes = knowledgeBaseCodes;
    }

    public String getPermissionMode()
    {
        return permissionMode;
    }

    public void setPermissionMode(String permissionMode)
    {
        this.permissionMode = permissionMode;
    }

    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout()
    {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout)
    {
        this.readTimeout = readTimeout;
    }

    public int getPollIntervalMs()
    {
        return pollIntervalMs;
    }

    public void setPollIntervalMs(int pollIntervalMs)
    {
        this.pollIntervalMs = pollIntervalMs;
    }

    public int getPollTimeoutMs()
    {
        return pollTimeoutMs;
    }

    public void setPollTimeoutMs(int pollTimeoutMs)
    {
        this.pollTimeoutMs = pollTimeoutMs;
    }

    private static String normalizeBaseUrl(String value)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/"))
        {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
