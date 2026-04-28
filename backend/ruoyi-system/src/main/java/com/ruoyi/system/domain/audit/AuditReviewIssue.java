package com.ruoyi.system.domain.audit;

import java.io.Serializable;

public class AuditReviewIssue implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long issueId;

    private Long versionId;

    private String issueType;

    private String issueTitle;

    private String issueContent;

    private Integer sortNum;

    public Long getIssueId()
    {
        return issueId;
    }

    public void setIssueId(Long issueId)
    {
        this.issueId = issueId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public String getIssueType()
    {
        return issueType;
    }

    public void setIssueType(String issueType)
    {
        this.issueType = issueType;
    }

    public String getIssueTitle()
    {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle)
    {
        this.issueTitle = issueTitle;
    }

    public String getIssueContent()
    {
        return issueContent;
    }

    public void setIssueContent(String issueContent)
    {
        this.issueContent = issueContent;
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
