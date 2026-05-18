package com.ruoyi.system.service.audit.impl;

import com.ruoyi.system.domain.audit.AuditAiFinding;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.FastGptAuditFinding;
import com.ruoyi.system.domain.audit.FastGptAuditResult;
import com.ruoyi.system.domain.audit.workflow.AuditWorkflowCallbackEvent;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.mapper.audit.AuditWorkflowCallbackEventMapper;
import com.ruoyi.system.service.audit.AuditAiAnalysisPersistenceService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    private static final int MAX_QUOTE_TEXT_LENGTH = 1000;

    private final AuditAiMapper auditAiMapper;
    private final AuditWorkflowCallbackEventMapper callbackEventMapper;
    private final AuditAiQueuePositionService auditAiQueuePositionService;

    public AuditAiAnalysisPersistenceServiceImpl(AuditAiMapper auditAiMapper,
                                                 AuditWorkflowCallbackEventMapper callbackEventMapper,
                                                 AuditAiQueuePositionService auditAiQueuePositionService)
    {
        this.auditAiMapper = auditAiMapper;
        this.callbackEventMapper = callbackEventMapper;
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
        String runKey = analysisRunKey(result);
        boolean hasRunKey = isNotBlank(runKey);
        boolean shouldIncreaseAnalysisCount = increaseAnalysisCount;
        if (increaseAnalysisCount && hasRunKey)
        {
            shouldIncreaseAnalysisCount = acquireAnalysisCountMarker(task, result, runKey, operator);
            if (!shouldIncreaseAnalysisCount)
            {
                log.info("Analysis result already processed, skip final result persistence, aiTaskId={}, runKey={}",
                        aiTaskId, runKey);
                return;
            }
        }

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
            finding.setQuoteText(truncateQuote(item.getQuote()));
            finding.setPageNo(item.getPageNo());
            finding.setLocationJson(item.getLocationJson());
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

        if (shouldIncreaseAnalysisCount)
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

    private boolean acquireAnalysisCountMarker(AuditAiTask task, FastGptAuditResult result, String runKey,
            String operator)
    {
        AuditWorkflowCallbackEvent marker = new AuditWorkflowCallbackEvent();
        marker.setCallbackEventId(buildAnalysisResultMarkerId(task.getAiTaskId(), runKey));
        marker.setAiTaskId(task.getAiTaskId());
        marker.setWorkflowTaskId(defaultIfBlank(result.getWorkflowTaskId(), result.getWorkflowRunId()));
        marker.setEventStatus("processed");
        marker.setRawPayload("analysis-result-count-marker, runKey=" + runKey + ", operator=" + operator);

        try
        {
            callbackEventMapper.insertAuditWorkflowCallbackEvent(marker);
            return true;
        }
        catch (DuplicateKeyException e)
        {
            log.info("Analysis result count marker already exists, skip increment, aiTaskId={}, runKey={}",
                    task.getAiTaskId(), runKey);
            return false;
        }
    }

    private String analysisRunKey(FastGptAuditResult result)
    {
        if (result == null)
        {
            return "";
        }
        if (isNotBlank(result.getWorkflowTaskId()))
        {
            return result.getWorkflowTaskId().trim();
        }
        if (isNotBlank(result.getWorkflowRunId()))
        {
            return result.getWorkflowRunId().trim();
        }
        return "";
    }

    private String buildAnalysisResultMarkerId(Long aiTaskId, String runKey)
    {
        String markerId = "analysis-result-" + aiTaskId + "-" + runKey;
        if (markerId.length() <= 100)
        {
            return markerId;
        }
        return "analysis-result-" + aiTaskId + "-" + sha256Hex(runKey).substring(0, 32);
    }

    private String sha256Hex(String value)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte item : hash)
            {
                hex.append(String.format("%02x", item));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }

    /**
     * 构建发现项内容，将 location、suggestion 合并到 finding_content。
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
        if (isDisplayableLocation(location))
        {
            content.append(" 位置：").append(location.trim());
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

    private boolean isDisplayableLocation(String location)
    {
        if (isBlank(location))
        {
            return false;
        }
        String trimmed = location.trim();
        return !trimmed.startsWith("{") && !trimmed.startsWith("[")
                && !trimmed.contains("source_chunk_id") && !trimmed.contains("source_chunk_no");
    }

    private String truncateQuote(String quote)
    {
        if (isBlank(quote))
        {
            return "";
        }
        String trimmedQuote = quote.trim();
        if (trimmedQuote.length() > MAX_QUOTE_TEXT_LENGTH)
        {
            log.warn("Finding quote truncated, originalLength={}", trimmedQuote.length());
            return trimmedQuote.substring(0, MAX_QUOTE_TEXT_LENGTH);
        }
        return trimmedQuote;
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
