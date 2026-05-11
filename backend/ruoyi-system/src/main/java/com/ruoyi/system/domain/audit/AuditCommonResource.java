package com.ruoyi.system.domain.audit;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class AuditCommonResource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long resourceId;

    private Long[] resourceIds;

    @Excel(name = "文档名称")
    private String documentName;

    private Long folderId;

    @Excel(name = "归属文件库")
    private String folderName;

    @Excel(name = "向量化状态", dictType = "audit_file_storage_status")
    private String storageStatus;

    private String progressText;

    @Excel(name = "创建者")
    private String creator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最新修改时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date latestModifyTime;

    @Excel(name = "文件大小")
    private String fileSize;

    private String fileName;

    private String fileUrl;

    private String currentVersionNo;

    private String delFlag;

    private String keyword;

    private List<AuditCommonResourceVersion> versionList;

    public Long getResourceId()
    {
        return resourceId;
    }

    public void setResourceId(Long resourceId)
    {
        this.resourceId = resourceId;
    }

    public Long[] getResourceIds()
    {
        return resourceIds;
    }

    public void setResourceIds(Long[] resourceIds)
    {
        this.resourceIds = resourceIds;
    }

    public String getDocumentName()
    {
        return documentName;
    }

    public void setDocumentName(String documentName)
    {
        this.documentName = documentName;
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

    public String getStorageStatus()
    {
        return storageStatus;
    }

    public void setStorageStatus(String storageStatus)
    {
        this.storageStatus = storageStatus;
    }

    public String getProgressText()
    {
        return progressText;
    }

    public void setProgressText(String progressText)
    {
        this.progressText = progressText;
    }

    public String getCreator()
    {
        return creator;
    }

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public Date getLatestModifyTime()
    {
        return latestModifyTime;
    }

    public void setLatestModifyTime(Date latestModifyTime)
    {
        this.latestModifyTime = latestModifyTime;
    }

    public String getFileSize()
    {
        return fileSize;
    }

    public void setFileSize(String fileSize)
    {
        this.fileSize = fileSize;
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

    public String getCurrentVersionNo()
    {
        return currentVersionNo;
    }

    public void setCurrentVersionNo(String currentVersionNo)
    {
        this.currentVersionNo = currentVersionNo;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public List<AuditCommonResourceVersion> getVersionList()
    {
        return versionList;
    }

    public void setVersionList(List<AuditCommonResourceVersion> versionList)
    {
        this.versionList = versionList;
    }
}
