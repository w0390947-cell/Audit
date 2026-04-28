package com.ruoyi.system.service.audit.impl;

import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.FastGptAuditResult;
import com.ruoyi.system.exception.FastGptAuditException;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.service.audit.AuditAiAnalysisPersistenceService;
import com.ruoyi.system.service.audit.AuditAiAnalysisService;
import com.ruoyi.system.service.audit.IFastGptAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * AI 审核分析服务实现
 *
 * @author ruoyi
 */
@Service
public class AuditAiAnalysisServiceImpl implements AuditAiAnalysisService
{
    private static final Logger log = LoggerFactory.getLogger(AuditAiAnalysisServiceImpl.class);

    private final AuditAiMapper auditAiMapper;
    private final IFastGptAuditService fastGptAuditService;
    private final AuditAiAnalysisPersistenceService persistenceService;

    public AuditAiAnalysisServiceImpl(AuditAiMapper auditAiMapper,
                                       IFastGptAuditService fastGptAuditService,
                                       AuditAiAnalysisPersistenceService persistenceService)
    {
        this.auditAiMapper = auditAiMapper;
        this.fastGptAuditService = fastGptAuditService;
        this.persistenceService = persistenceService;
    }

    @Override
    public void analyzeAndSave(Long aiTaskId, String operator)
    {
        log.info("AI analysis started, aiTaskId={}, operator={}", aiTaskId, operator);

        // 1. 查询任务详情
        AuditAiTask task = auditAiMapper.selectAuditAiTaskById(aiTaskId);
        if (task == null)
        {
            log.error("AI analysis failed, aiTaskId={}, error=Task not found", aiTaskId);
            throw new IllegalArgumentException("AI任务不存在，aiTaskId=" + aiTaskId);
        }

        // 2. 校验任务状态
        if ("executing".equals(task.getTaskStatus()))
        {
            log.warn("AI analysis rejected, aiTaskId={}, error=Task already executing", aiTaskId);
            throw new IllegalStateException("任务正在执行中，请勿重复提交");
        }

        // 3. 更新任务状态为 executing
        updateTaskToExecuting(aiTaskId, operator);

        try
        {
            // 4. 调用 FastGPT
            log.info("FastGPT calling, aiTaskId={}", aiTaskId);
            FastGptAuditResult result = fastGptAuditService.analyze(task);
            log.info("FastGPT completed, aiTaskId={}, findingCount={}, elapsedMs={}",
                    aiTaskId,
                    result.getFindings() != null ? result.getFindings().size() : 0,
                    result.getElapsedMs());

            // 5. 保存结果（事务）
            persistenceService.saveAuditResult(task, result, operator);

            log.info("AI analysis completed successfully, aiTaskId={}", aiTaskId);
        }
        catch (FastGptAuditException e)
        {
            // FastGPT 调用失败
            log.error("AI analysis failed, aiTaskId={}, errorCode={}", aiTaskId, e.getErrorCode());
            markAnalysisFailure(aiTaskId, e, operator);
            throw e;
        }
        catch (Exception e)
        {
            // 未预期的异常
            log.error("AI analysis failed with unexpected error, aiTaskId={}", aiTaskId, e);
            markAnalysisFailure(aiTaskId, e, operator);
            throw new RuntimeException("AI分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新任务状态为 executing
     */
    private void updateTaskToExecuting(Long aiTaskId, String operator)
    {
        int rows = auditAiMapper.updateAuditAiTaskExecuting(aiTaskId, 35, "FastGPT 工作流分析中", operator);
        if (rows > 0)
        {
            log.info("Task updated to executing, aiTaskId={}", aiTaskId);
        }
        else
        {
            log.warn("Failed to update task to executing, aiTaskId={}", aiTaskId);
            throw new IllegalStateException("任务正在执行中或状态更新失败，请勿重复提交");
        }
    }

    /**
     * 标记分析失败
     */
    private void markAnalysisFailure(Long aiTaskId, Exception e, String operator)
    {
        String message = sanitizeErrorMessage(e);
        String progressText = "AI分析失败：" + message;

        auditAiMapper.updateAuditAiAnalysisFailure(aiTaskId, progressText, operator);
        log.info("Task marked as failed, aiTaskId={}, message={}", aiTaskId, message);
    }

    /**
     * 清洗错误信息
     * 去除敏感信息，限制长度
     */
    private String sanitizeErrorMessage(Exception e)
    {
        String message = e.getMessage();

        if (message == null)
        {
            return "未知错误";
        }

        // 去除可能包含的 API Key
        message = message.replaceAll("(?i)(api[_-]?key|authorization)\\s*[=:]\\s*[\\w-]+", "$1=***");

        // 去除可能包含的完整 URL
        message = message.replaceAll("https?://[^\\s/$.?#][^\\s]*", "***");

        // 限制长度
        if (message.length() > 200)
        {
            message = message.substring(0, 200) + "...";
        }

        return message;
    }
}
