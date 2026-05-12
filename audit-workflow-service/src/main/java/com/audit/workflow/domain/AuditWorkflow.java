package com.audit.workflow.domain;

import java.time.LocalDateTime;

public class AuditWorkflow {

    private Long workflowId;
    private String workflowCode;
    private String workflowName;
    private String description;
    private String inputSchema;
    private String knowledgeBinding;
    private String retrievalConfig;
    private String promptTemplate;
    private String outputSchema;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(Long workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowCode() {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputSchema() {
        return inputSchema;
    }

    public void setInputSchema(String inputSchema) {
        this.inputSchema = inputSchema;
    }

    public String getKnowledgeBinding() {
        return knowledgeBinding;
    }

    public void setKnowledgeBinding(String knowledgeBinding) {
        this.knowledgeBinding = knowledgeBinding;
    }

    public String getRetrievalConfig() {
        return retrievalConfig;
    }

    public void setRetrievalConfig(String retrievalConfig) {
        this.retrievalConfig = retrievalConfig;
    }

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    public String getOutputSchema() {
        return outputSchema;
    }

    public void setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
