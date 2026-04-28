package com.ruoyi.system.domain.audit;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

public class AuditAssetAiVersion
{
    private Long versionId;

    private Long assetId;

    private String versionNo;

    private String wordCountText;

    private String currentFlag;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private List<AuditAssetAiStep> stepList;

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(String versionNo)
    {
        this.versionNo = versionNo;
    }

    public String getWordCountText()
    {
        return wordCountText;
    }

    public void setWordCountText(String wordCountText)
    {
        this.wordCountText = wordCountText;
    }

    public String getCurrentFlag()
    {
        return currentFlag;
    }

    public void setCurrentFlag(String currentFlag)
    {
        this.currentFlag = currentFlag;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public List<AuditAssetAiStep> getStepList()
    {
        return stepList;
    }

    public void setStepList(List<AuditAssetAiStep> stepList)
    {
        this.stepList = stepList;
    }
}
