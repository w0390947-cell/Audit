package com.ruoyi.system.domain.audit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * FastGPT 审核结果 DTO
 *
 * @author ruoyi
 */
public class FastGptAuditResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * FastGPT 是否认为本次分析成功
     */
    private boolean success;

    /**
     * 审核摘要
     * 后续写入 audit_ai_task.ai_summary
     */
    private String summary;

    /**
     * 问题数量
     * 通常等于 findings.size()
     */
    private Integer totalIssues;

    /**
     * 结构化发现项列表
     */
    private List<FastGptAuditFinding> findings;

    /**
     * 原始模型输出
     * 便于失败排查，日志中需截断
     */
    private String rawContent;

    /**
     * 本次调用追踪 ID
     * 格式：audit-ai-{aiTaskId}-{timestamp}
     */
    private String chatId;

    /**
     * 外部工作流任务 ID，用于最终结果保存幂等。
     */
    private String workflowTaskId;

    /**
     * 外部工作流运行 ID，用于最终结果保存幂等。
     */
    private String workflowRunId;

    /**
     * 响应时间（毫秒）
     */
    private Long elapsedMs;

    public FastGptAuditResult()
    {
        this.findings = new ArrayList<>();
        this.success = false;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public Integer getTotalIssues()
    {
        return totalIssues;
    }

    public void setTotalIssues(Integer totalIssues)
    {
        this.totalIssues = totalIssues;
    }

    public List<FastGptAuditFinding> getFindings()
    {
        return findings;
    }

    public void setFindings(List<FastGptAuditFinding> findings)
    {
        this.findings = findings;
    }

    public String getRawContent()
    {
        return rawContent;
    }

    public void setRawContent(String rawContent)
    {
        this.rawContent = rawContent;
    }

    public String getChatId()
    {
        return chatId;
    }

    public void setChatId(String chatId)
    {
        this.chatId = chatId;
    }

    public String getWorkflowTaskId()
    {
        return workflowTaskId;
    }

    public void setWorkflowTaskId(String workflowTaskId)
    {
        this.workflowTaskId = workflowTaskId;
    }

    public String getWorkflowRunId()
    {
        return workflowRunId;
    }

    public void setWorkflowRunId(String workflowRunId)
    {
        this.workflowRunId = workflowRunId;
    }

    public Long getElapsedMs()
    {
        return elapsedMs;
    }

    public void setElapsedMs(Long elapsedMs)
    {
        this.elapsedMs = elapsedMs;
    }

    /**
     * 获取截断后的原始内容（用于日志）
     * @param maxLength 最大长度
     * @return 截断后的内容
     */
    public String getTruncatedRawContent(int maxLength)
    {
        if (rawContent == null)
        {
            return "";
        }
        if (rawContent.length() <= maxLength)
        {
            return rawContent;
        }
        return rawContent.substring(0, maxLength) + "...";
    }

    @Override
    public String toString()
    {
        return "FastGptAuditResult{" +
                "success=" + success +
                ", summary='" + summary + '\'' +
                ", totalIssues=" + totalIssues +
                ", findingsCount=" + (findings != null ? findings.size() : 0) +
                ", rawContentSize=" + (rawContent != null ? rawContent.length() : 0) +
                ", chatId='" + chatId + '\'' +
                ", workflowTaskId='" + workflowTaskId + '\'' +
                ", workflowRunId='" + workflowRunId + '\'' +
                ", elapsedMs=" + elapsedMs +
                '}';
    }
}
