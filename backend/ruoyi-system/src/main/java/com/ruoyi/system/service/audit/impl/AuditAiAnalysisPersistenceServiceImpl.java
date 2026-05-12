package com.ruoyi.system.service.audit.impl;

import com.ruoyi.system.domain.audit.AuditAiFinding;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.FastGptAuditFinding;
import com.ruoyi.system.domain.audit.FastGptAuditResult;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.service.audit.AuditAiAnalysisPersistenceService;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * AI 审核分析结果持久化服务实现
 *
 * @author ruoyi
 */
@Service
public class AuditAiAnalysisPersistenceServiceImpl implements AuditAiAnalysisPersistenceService
{
    private static final Logger log = LoggerFactory.getLogger(AuditAiAnalysisPersistenceServiceImpl.class);

    private static final int MAX_FINDING_CONTENT_LENGTH = 1000;

    private final AuditAiMapper auditAiMapper;
    private final AuditAiQueuePositionService auditAiQueuePositionService;

    public AuditAiAnalysisPersistenceServiceImpl(AuditAiMapper auditAiMapper,
                                                 AuditAiQueuePositionService auditAiQueuePositionService)
    {
        this.auditAiMapper = auditAiMapper;
        this.auditAiQueuePositionService = auditAiQueuePositionService;
    }

    /**
     * 保存审核结果（事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAuditResult(AuditAiTask task, FastGptAuditResult result, String operator)
    {
        saveAuditResult(task, result, operator, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAuditResult(AuditAiTask task, FastGptAuditResult result, String operator, boolean increaseAnalysisCount)
    {
        Long aiTaskId = task.getAiTaskId();
        List<FastGptAuditFinding> findings = result.getFindings() == null
                ? Collections.emptyList()
                : result.getFindings();

        // 1. 删除旧发现项
        auditAiMapper.deleteAuditAiFindingByTaskIds(new Long[]{ aiTaskId });
        log.debug("Old findings deleted, aiTaskId={}", aiTaskId);

        // 2. 插入新发现项
        int sort = 1;
        for (FastGptAuditFinding item : findings)
        {
            AuditAiFinding finding = new AuditAiFinding();
            finding.setAiTaskId(aiTaskId);
            finding.setFindingType(defaultIfBlank(item.getType(), "其他"));
            finding.setFindingTitle(defaultIfBlank(item.getTitle(), "AI发现问题"));
            finding.setFindingContent(buildFindingContent(item));
            finding.setSortNum(sort++);
            auditAiMapper.insertAuditAiFinding(finding);
        }
        log.info("Findings inserted, aiTaskId={}, count={}", aiTaskId, findings.size());

        // 3. 更新任务摘要和状态
        AuditAiTask update = new AuditAiTask();
        update.setAiTaskId(aiTaskId);
        update.setTaskStatus("completed");
        update.setReviewStatus("reviewing");
        update.setProgressPercent(100);

        String summary = result.getSummary();
        if (isBlank(summary))
        {
            summary = buildDefaultSummary(findings.size());
        }

        if (!findings.isEmpty())
        {
            update.setProgressText("AI分析完成，待人工审核");
        }
        else if (requiresManualReview(summary))
        {
            update.setProgressText("AI分析完成，建议人工复核");
        }
        else
        {
            update.setProgressText("AI分析完成，未发现关键问题");
        }

        update.setAiSummary(summary);
        update.setUpdateBy(operator);

        if (increaseAnalysisCount)
        {
            auditAiMapper.updateAuditAiAnalysisResult(update);
        }
        else
        {
            auditAiMapper.updateAuditAiAnalysisResultWithoutCount(update);
        }
        auditAiQueuePositionService.resortQueuePositions(operator);
        log.info("Task status updated to completed, aiTaskId={}", aiTaskId);
    }

    /**
     * 构建发现项内容，将 severity、location、suggestion 合并到 finding_content。
     */
    private String buildFindingContent(FastGptAuditFinding item)
    {
        StringBuilder content = new StringBuilder();

        String baseContent = defaultIfBlank(item.getContent(), item.getTitle());
        if (isBlank(baseContent))
        {
            baseContent = "发现问题";
        }
        content.append(baseContent);

        String location = item.getLocation();
        if (!isBlank(location))
        {
            content.append(" 位置：").append(location);
        }

        String suggestion = item.getSuggestion();
        if (!isBlank(suggestion))
        {
            content.append("。建议：").append(suggestion);
        }

        String mergedContent = content.toString();
        if (mergedContent.length() > MAX_FINDING_CONTENT_LENGTH)
        {
            log.warn("Finding content truncated, originalLength={}", mergedContent.length());
            return mergedContent.substring(0, MAX_FINDING_CONTENT_LENGTH);
        }

        return mergedContent;
    }

    /**
     * 构建默认摘要。
     */
    private String buildDefaultSummary(int issueCount)
    {
        if (issueCount == 0)
        {
            return "未发现关键问题";
        }
        return String.format("本次报告发现 %d 个问题", issueCount);
    }

    private boolean requiresManualReview(String summary)
    {
        if (isBlank(summary))
        {
            return false;
        }
        return summary.contains("人工复核") || summary.contains("人工审核")
                || summary.contains("风险信号") || summary.contains("复核");
    }
}
