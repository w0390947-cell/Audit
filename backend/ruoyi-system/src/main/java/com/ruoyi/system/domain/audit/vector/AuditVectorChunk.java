package com.ruoyi.system.domain.audit.vector;

public class AuditVectorChunk
{
    private Long chunkId;

    private Long documentId;

    private Long resourceId;

    private Long folderId;

    private Integer chunkNo;

    private String chunkText;

    private Integer pageNo;

    private String sectionTitle;

    private Integer tokenCount;

    private float[] embedding;

    private String chunkUid;

    private String ruleCode;

    private String sectionPath;

    private Integer paragraphNo;

    private String contentHash;

    private String metadata;

    public Long getChunkId()
    {
        return chunkId;
    }

    public void setChunkId(Long chunkId)
    {
        this.chunkId = chunkId;
    }

    public Long getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(Long documentId)
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

    public Long getFolderId()
    {
        return folderId;
    }

    public void setFolderId(Long folderId)
    {
        this.folderId = folderId;
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

    public Integer getTokenCount()
    {
        return tokenCount;
    }

    public void setTokenCount(Integer tokenCount)
    {
        this.tokenCount = tokenCount;
    }

    public float[] getEmbedding()
    {
        return embedding;
    }

    public void setEmbedding(float[] embedding)
    {
        this.embedding = embedding;
    }

    public String getChunkUid()
    {
        return chunkUid;
    }

    public void setChunkUid(String chunkUid)
    {
        this.chunkUid = chunkUid;
    }

    public String getRuleCode()
    {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode)
    {
        this.ruleCode = ruleCode;
    }

    public String getSectionPath()
    {
        return sectionPath;
    }

    public void setSectionPath(String sectionPath)
    {
        this.sectionPath = sectionPath;
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
