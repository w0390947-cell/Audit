package com.ruoyi.system.domain.audit;

import java.io.Serializable;

/**
 * FastGPT 审核发现项 DTO
 *
 * @author ruoyi
 */
public class FastGptAuditFinding implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 发现类型
     * 枚举值：内容缺失、格式错误、数据异常、逻辑错误、标准不符、其他
     */
    private String type;

    /**
     * 发现标题
     */
    private String title;

    /**
     * 发现内容（详细描述）
     */
    private String content;

    /**
     * 报告原文引用，用于检测结果展示和问题定位。
     */
    private String quote;

    /**
     * 问题位置
     * 例如：第1页 / 基本信息
     */
    private String location;

    /**
     * 报告页码，从 1 开始。
     */
    private Integer pageNo;

    /**
     * 原始定位信息 JSON，供后续页内高亮等能力扩展。
     */
    private String locationJson;

    /**
     * 修改建议
     */
    private String suggestion;

    public FastGptAuditFinding()
    {
    }

    public FastGptAuditFinding(String type, String title, String content)
    {
        this.type = type;
        this.title = title;
        this.content = content;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getQuote()
    {
        return quote;
    }

    public void setQuote(String quote)
    {
        this.quote = quote;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public Integer getPageNo()
    {
        return pageNo;
    }

    public void setPageNo(Integer pageNo)
    {
        this.pageNo = pageNo;
    }

    public String getLocationJson()
    {
        return locationJson;
    }

    public void setLocationJson(String locationJson)
    {
        this.locationJson = locationJson;
    }

    public String getSuggestion()
    {
        return suggestion;
    }

    public void setSuggestion(String suggestion)
    {
        this.suggestion = suggestion;
    }

    @Override
    public String toString()
    {
        return "FastGptAuditFinding{" +
                "type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", quote='" + quote + '\'' +
                ", location='" + location + '\'' +
                ", pageNo=" + pageNo +
                ", locationJson='" + locationJson + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }
}
