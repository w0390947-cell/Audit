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
     * 严重程度
     * 枚举值：high, medium, low
     */
    private String severity;

    /**
     * 问题位置
     * 例如：第1页 / 基本信息
     */
    private String location;

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

    public String getSeverity()
    {
        return severity;
    }

    public void setSeverity(String severity)
    {
        this.severity = severity;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
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
                ", severity='" + severity + '\'' +
                ", location='" + location + '\'' +
                ", suggestion='" + suggestion + '\'' +
                '}';
    }
}
