package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class AuditWorkflowSearchHit
{
    @JsonProperty("chunk_id")
    private String chunkId;

    @JsonProperty("chunk_uid")
    private String chunkUid;

    @JsonProperty("document_id")
    private String documentId;

    @JsonProperty("resource_id")
    private Long resourceId;

    @JsonProperty("resource_type")
    private String resourceType;

    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("file_hash")
    private String fileHash;

    @JsonProperty("version_no")
    private String versionNo;

    @JsonProperty("folder_id")
    private Long folderId;

    @JsonProperty("folder_name")
    private String folderName;

    @JsonProperty("page_no")
    private Integer pageNo;

    @JsonProperty("section_title")
    private String sectionTitle;

    @JsonProperty("section_path")
    private String sectionPath;

    @JsonProperty("chunk_no")
    private Integer chunkNo;

    @JsonProperty("chunk_text")
    private String chunkText;

    private BigDecimal score;

    @JsonProperty("rank_score")
    private BigDecimal rankScore;

    private String status;

    @JsonProperty("effective_date")
    private String effectiveDate;

    @JsonProperty("expire_date")
    private String expireDate;

    @JsonProperty("knowledge_base_code")
    private String knowledgeBaseCode;

    @JsonProperty("category_code")
    private String categoryCode;

    @JsonProperty("business_type")
    private String businessType;

    @JsonProperty("owner_dept_id")
    private String ownerDeptId;

    @JsonProperty("rule_code")
    private String ruleCode;

    @JsonProperty("paragraph_no")
    private Integer paragraphNo;

    @JsonProperty("content_hash")
    private String contentHash;

    private String metadata;

    public String getChunkId()
    {
        return chunkId;
    }

    public void setChunkId(String chunkId)
    {
        this.chunkId = chunkId;
    }

    public String getChunkUid()
    {
        return chunkUid;
    }

    public void setChunkUid(String chunkUid)
    {
        this.chunkUid = chunkUid;
    }

    public String getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(String documentId)
    {
        this.documentId = documentId;
    }

    public Long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(Long resourceId)
    {
        this.resourceId = resourceId;
    }

    public String getResourceType()
    {
        return resourceType;
    }

    public void setResourceType(String resourceType)
    {
        this.resourceType = resourceType;
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

    public String getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(String versionNo)
    {
        this.versionNo = versionNo;
    }

    public Long getFolderId()
    {
        return folderId;
    }

    public void setFolderId(Long folderId)
    {
        this.folderId = folderId;
    }

    public String getFolderName()
    {
        return folderName;
    }

    public void setFolderName(String folderName)
    {
        this.folderName = folderName;
    }

    public Integer getPageNo()
    {
        return pageNo;
    }

    public void setPageNo(Integer pageNo)
    {
        this.pageNo = pageNo;
    }

    public String getSectionTitle()
    {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle)
    {
        this.sectionTitle = sectionTitle;
    }

    public String getSectionPath()
    {
        return sectionPath;
    }

    public void setSectionPath(String sectionPath)
    {
        this.sectionPath = sectionPath;
    }

    public Integer getChunkNo()
    {
        return chunkNo;
    }

    public void setChunkNo(Integer chunkNo)
    {
        this.chunkNo = chunkNo;
    }

    public String getChunkText()
    {
        return chunkText;
    }

    public void setChunkText(String chunkText)
    {
        this.chunkText = chunkText;
    }

    public BigDecimal getScore()
    {
        return score;
    }

    public void setScore(BigDecimal score)
    {
        this.score = score;
    }

    public BigDecimal getRankScore()
    {
        return rankScore;
    }

    public void setRankScore(BigDecimal rankScore)
    {
        this.rankScore = rankScore;
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

    public String getOwnerDeptId()
    {
        return ownerDeptId;
    }

    public void setOwnerDeptId(String ownerDeptId)
    {
        this.ownerDeptId = ownerDeptId;
    }

    public String getRuleCode()
    {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode)
    {
        this.ruleCode = ruleCode;
    }

    public Integer getParagraphNo()
    {
        return paragraphNo;
    }

    public void setParagraphNo(Integer paragraphNo)
    {
        this.paragraphNo = paragraphNo;
    }

    public String getContentHash()
    {
        return contentHash;
    }

    public void setContentHash(String contentHash)
    {
        this.contentHash = contentHash;
    }

    public String getMetadata()
    {
        return metadata;
    }

    public void setMetadata(String metadata)
    {
        this.metadata = metadata;
    }
}
