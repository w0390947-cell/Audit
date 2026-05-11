package com.ruoyi.system.domain.audit.vector;

import java.util.Date;

public class AuditVectorDocument
{
    public static final String RESOURCE_TYPE_COMMON = "common";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";

    private Long documentId;

    private String resourceType;

    private Long resourceId;

    private Long folderId;

    private String documentName;

    private String fileName;

    private String fileUrl;

    private String fileHash;

    private String fileType;

    private String currentVersionNo;

    private Integer chunkCount;

    private String embeddingModel;

    private Integer embeddingDimensions;

    private String parseStatus;

    private String vectorStatus;

    private Date indexedAt;

    private String errorMsg;

    private String knowledgeBaseCode;

    private String categoryCode;

    private String businessType;

    private String status;

    private String effectiveDate;

    private String expireDate;

    private String ownerDeptId;

    private String sourceSystem;

    public Long getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(Long documentId)
    {
        this.documentId = documentId;
    }

    public String getResourceType()
    {
        return resourceType;
    }

    public void setResourceType(String resourceType)
    {
        this.resourceType = resourceType;
    }

    public Long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(Long resourceId)
    {
        this.resourceId = resourceId;
    }

    public Long getFolderId()
    {
        return folderId;
    }

    public void setFolderId(Long folderId)
    {
        this.folderId = folderId;
    }

    public String getDocumentName()
    {
        return documentName;
    }

    public void setDocumentName(String documentName)
    {
        this.documentName = documentName;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getFileHash()
    {
        return fileHash;
    }

    public void setFileHash(String fileHash)
    {
        this.fileHash = fileHash;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getCurrentVersionNo()
    {
        return currentVersionNo;
    }

    public void setCurrentVersionNo(String currentVersionNo)
    {
        this.currentVersionNo = currentVersionNo;
    }

    public Integer getChunkCount()
    {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount)
    {
        this.chunkCount = chunkCount;
    }

    public String getEmbeddingModel()
    {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel)
    {
        this.embeddingModel = embeddingModel;
    }

    public Integer getEmbeddingDimensions()
    {
        return embeddingDimensions;
    }

    public void setEmbeddingDimensions(Integer embeddingDimensions)
    {
        this.embeddingDimensions = embeddingDimensions;
    }

    public String getParseStatus()
    {
        return parseStatus;
    }

    public void setParseStatus(String parseStatus)
    {
        this.parseStatus = parseStatus;
    }

    public String getVectorStatus()
    {
        return vectorStatus;
    }

    public void setVectorStatus(String vectorStatus)
    {
        this.vectorStatus = vectorStatus;
    }

    public Date getIndexedAt()
    {
        return indexedAt;
    }

    public void setIndexedAt(Date indexedAt)
    {
        this.indexedAt = indexedAt;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getKnowledgeBaseCode()
    {
        return knowledgeBaseCode;
    }

    public void setKnowledgeBaseCode(String knowledgeBaseCode)
    {
        this.knowledgeBaseCode = knowledgeBaseCode;
    }

    public String getCategoryCode()
    {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode)
    {
        this.categoryCode = categoryCode;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getEffectiveDate()
    {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate)
    {
        this.effectiveDate = effectiveDate;
    }

    public String getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(String expireDate)
    {
        this.expireDate = expireDate;
    }

    public String getOwnerDeptId()
    {
        return ownerDeptId;
    }

    public void setOwnerDeptId(String ownerDeptId)
    {
        this.ownerDeptId = ownerDeptId;
    }

    public String getSourceSystem()
    {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem)
    {
        this.sourceSystem = sourceSystem;
    }
}
