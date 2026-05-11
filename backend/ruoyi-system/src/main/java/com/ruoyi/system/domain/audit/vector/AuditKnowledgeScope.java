package com.ruoyi.system.domain.audit.vector;

import com.fasterxml.jackson.annotation.JsonAlias;

public class AuditKnowledgeScope
{
    @JsonAlias("folder_ids")
    private Long[] folderIds;

    @JsonAlias("resource_ids")
    private Long[] resourceIds;

    @JsonAlias("knowledge_base_codes")
    private String[] knowledgeBaseCodes;

    @JsonAlias("category_codes")
    private String[] categoryCodes;

    @JsonAlias("business_type")
    private String businessType;

    @JsonAlias("effective_only")
    private Boolean effectiveOnly;

    @JsonAlias("as_of_date")
    private String asOfDate;

    public Long[] getFolderIds()
    {
        return folderIds;
    }

    public void setFolderIds(Long[] folderIds)
    {
        this.folderIds = folderIds;
    }

    public Long[] getResourceIds()
    {
        return resourceIds;
    }

    public void setResourceIds(Long[] resourceIds)
    {
        this.resourceIds = resourceIds;
    }

    public String[] getKnowledgeBaseCodes()
    {
        return knowledgeBaseCodes;
    }

    public void setKnowledgeBaseCodes(String[] knowledgeBaseCodes)
    {
        this.knowledgeBaseCodes = knowledgeBaseCodes;
    }

    public String[] getCategoryCodes()
    {
        return categoryCodes;
    }

    public void setCategoryCodes(String[] categoryCodes)
    {
        this.categoryCodes = categoryCodes;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public Boolean getEffectiveOnly()
    {
        return effectiveOnly;
    }

    public void setEffectiveOnly(Boolean effectiveOnly)
    {
        this.effectiveOnly = effectiveOnly;
    }

    public String getAsOfDate()
    {
        return asOfDate;
    }

    public void setAsOfDate(String asOfDate)
    {
        this.asOfDate = asOfDate;
    }
}
