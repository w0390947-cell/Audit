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
}
