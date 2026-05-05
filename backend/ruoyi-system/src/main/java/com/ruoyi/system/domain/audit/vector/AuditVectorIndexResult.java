package com.ruoyi.system.domain.audit.vector;

public class AuditVectorIndexResult
{
    private String status;

    private Long documentId;

    private Integer chunkCount;

    private String message;

    public static AuditVectorIndexResult of(String status, Long documentId, Integer chunkCount, String message)
    {
        AuditVectorIndexResult result = new AuditVectorIndexResult();
        result.setStatus(status);
        result.setDocumentId(documentId);
        result.setChunkCount(chunkCount);
        result.setMessage(message);
        return result;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getDocumentId()
    {
        return documentId;
    }

    public void setDocumentId(Long documentId)
    {
        this.documentId = documentId;
    }

    public Integer getChunkCount()
    {
        return chunkCount;
    }

    public void setChunkCount(Integer chunkCount)
    {
        this.chunkCount = chunkCount;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
