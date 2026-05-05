package com.ruoyi.system.domain.audit.vector;

public class DocumentTextBlock
{
    private Integer pageNo;

    private Integer blockNo;

    private String sectionTitle;

    private String text;

    public Integer getPageNo()
    {
        return pageNo;
    }

    public void setPageNo(Integer pageNo)
    {
        this.pageNo = pageNo;
    }

    public Integer getBlockNo()
    {
        return blockNo;
    }

    public void setBlockNo(Integer blockNo)
    {
        this.blockNo = blockNo;
    }

    public String getSectionTitle()
    {
        return sectionTitle;
    }

    public void setSectionTitle(String sectionTitle)
    {
        this.sectionTitle = sectionTitle;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
