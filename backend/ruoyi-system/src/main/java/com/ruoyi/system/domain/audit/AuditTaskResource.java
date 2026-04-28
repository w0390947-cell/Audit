package com.ruoyi.system.domain.audit;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class AuditTaskResource extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long resourceId;

    private Long[] resourceIds;

    @Excel(name = "文件编号")
    private String fileNo;

    @Excel(name = "文件名称")
    private String fileName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "归档时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date archiveTime;

    private Long folderId;

    @Excel(name = "归属文件库")
    private String folderName;

    @Excel(name = "文件采集状态", dictType = "audit_task_collect_status")
    private String collectStatus;

    private String previewFileUrl;

    private String delFlag;

    private String keyword;

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

    public String getFileNo()
    {
        return fileNo;
    }

    public void setFileNo(String fileNo)
    {
        this.fileNo = fileNo;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public Date getArchiveTime()
    {
        return archiveTime;
    }

    public void setArchiveTime(Date archiveTime)
    {
        this.archiveTime = archiveTime;
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

    public String getCollectStatus()
    {
        return collectStatus;
    }

    public void setCollectStatus(String collectStatus)
    {
        this.collectStatus = collectStatus;
    }

    public String getPreviewFileUrl()
    {
        return previewFileUrl;
    }

    public void setPreviewFileUrl(String previewFileUrl)
    {
        this.previewFileUrl = previewFileUrl;
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
}
