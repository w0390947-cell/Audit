package com.audit.workflow.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RetrievalReference {

    private Long referenceId;
    private Long retrievalId;
    private Long taskId;
    private Long sourceChunkId;
    private String kbChunkId;
    private String kbDocumentId;
    private Long resourceId;
    private String resourceType;
    private String fileName;
    private String fileUrl;
    private String versionNo;
    private Integer pageNo;
    private String sectionTitle;
    private String sectionPath;
    private String ruleCode;
    private String chunkTextSnapshot;
    private BigDecimal score;
    private BigDecimal rankScore;
    private LocalDate effectiveDate;
    private String status;

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Long getRetrievalId() {
        return retrievalId;
    }

    public void setRetrievalId(Long retrievalId) {
        this.retrievalId = retrievalId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getSourceChunkId() {
        return sourceChunkId;
    }

    public void setSourceChunkId(Long sourceChunkId) {
        this.sourceChunkId = sourceChunkId;
    }

    public String getKbChunkId() {
        return kbChunkId;
    }

    public void setKbChunkId(String kbChunkId) {
        this.kbChunkId = kbChunkId;
    }

    public String getKbDocumentId() {
        return kbDocumentId;
    }

    public void setKbDocumentId(String kbDocumentId) {
        this.kbDocumentId = kbDocumentId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getSectionTitle() {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = sectionTitle;
    }

    public String getSectionPath() {
        return sectionPath;
    }

    public void setSectionPath(String sectionPath) {
        this.sectionPath = sectionPath;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getChunkTextSnapshot() {
        return chunkTextSnapshot;
    }

    public void setChunkTextSnapshot(String chunkTextSnapshot) {
        this.chunkTextSnapshot = chunkTextSnapshot;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getRankScore() {
        return rankScore;
    }

    public void setRankScore(BigDecimal rankScore) {
        this.rankScore = rankScore;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
