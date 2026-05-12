package com.ruoyi.system.domain.audit;

import java.io.Serializable;

public class AuditUploadedFile implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String fileUrl;

    private String fileName;

    private String originalFilename;

    private String fileSize;

    public String getFileUrl()
    {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl)
    {
        this.fileUrl = fileUrl;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getOriginalFilename()
    {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename)
    {
        this.originalFilename = originalFilename;
    }

    public String getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(String fileSize)
    {
        this.fileSize = fileSize;
    }
}
