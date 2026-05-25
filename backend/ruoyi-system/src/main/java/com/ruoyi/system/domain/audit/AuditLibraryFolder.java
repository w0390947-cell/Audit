package com.ruoyi.system.domain.audit;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class AuditLibraryFolder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long folderId;

    private Long parentId;

    private String libraryType;

    @Excel(name = "文件库名称")
    private String folderName;

    @Excel(name = "简介")
    private String intro;

    private String visibleScope;

    private String topFlag;

    private Integer fileCount;

    private String delFlag;

    public Long getFolderId()
    {
        return folderId;
    }

    public void setFolderId(Long folderId)
    {
        this.folderId = folderId;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getLibraryType()
    {
        return libraryType;
    }

    public void setLibraryType(String libraryType)
    {
        this.libraryType = libraryType;
    }

    public String getFolderName()
    {
        return folderName;
    }

    public void setFolderName(String folderName)
    {
        this.folderName = folderName;
    }

    public String getIntro()
    {
        return intro;
    }

    public void setIntro(String intro)
    {
        this.intro = intro;
    }

    public String getVisibleScope()
    {
        return visibleScope;
    }

    public void setVisibleScope(String visibleScope)
    {
        this.visibleScope = visibleScope;
    }

    public String getTopFlag()
    {
        return topFlag;
    }

    public void setTopFlag(String topFlag)
    {
        this.topFlag = topFlag;
    }

    public Integer getFileCount()
    {
        return fileCount;
    }

    public void setFileCount(Integer fileCount)
    {
        this.fileCount = fileCount;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
}
