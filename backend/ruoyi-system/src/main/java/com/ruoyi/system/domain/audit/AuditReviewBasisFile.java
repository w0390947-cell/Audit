package com.ruoyi.system.domain.audit;

import com.ruoyi.common.core.domain.BaseEntity;

public class AuditReviewBasisFile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    public static final String SOURCE_UPLOADED = "uploaded";

    public static final String SOURCE_LIBRARY = "library";

    private Long basisId;

    private Long taskId;

    private Long versionId;

    private String sourceType;

    private Long libraryResourceId;

    private String fileUrl;

    private String fileName;

    private String originalFilename;

    private String fileSize;

    private Integer sortNum;

    public Long getBasisId()
    {
        return basisId;
    }

    public void setBasisId(Long basisId)
    {
        this.basisId = basisId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public Long getLibraryResourceId()
    {
        return libraryResourceId;
    }

    public void setLibraryResourceId(Long libraryResourceId)
    {
        this.libraryResourceId = libraryResourceId;
    }

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

    public Integer getSortNum()
    {
        return sortNum;
    }

    public void setSortNum(Integer sortNum)
    {
        this.sortNum = sortNum;
    }
}
