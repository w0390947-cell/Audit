package com.ruoyi.system.domain.audit;

public class AuditAiReportPreview
{
    private String fileName;

    private String sourceFileUrl;

    private String previewFileUrl;

    private String fileType;

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getSourceFileUrl()
    {
        return sourceFileUrl;
    }

    public void setSourceFileUrl(String sourceFileUrl)
    {
        this.sourceFileUrl = sourceFileUrl;
    }

    public String getPreviewFileUrl()
    {
        return previewFileUrl;
    }

    public void setPreviewFileUrl(String previewFileUrl)
    {
        this.previewFileUrl = previewFileUrl;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }
}
