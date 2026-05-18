package com.ruoyi.system.service.audit.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;
import com.ruoyi.system.mapper.audit.AuditAiMapper;

@Service
public class AuditAiEstimatedDurationAsyncService
{
    public static final String PENDING_TEXT = "估算中";

    private static final Logger log = LoggerFactory.getLogger(AuditAiEstimatedDurationAsyncService.class);

    private final AuditAiEstimatedDurationService estimatedDurationService;

    private final AuditAiMapper auditAiMapper;

    private final ThreadPoolTaskExecutor taskExecutor;

    public AuditAiEstimatedDurationAsyncService(AuditAiEstimatedDurationService estimatedDurationService,
            AuditAiMapper auditAiMapper,
            @Qualifier("threadPoolTaskExecutor") ThreadPoolTaskExecutor taskExecutor)
    {
        this.estimatedDurationService = estimatedDurationService;
        this.auditAiMapper = auditAiMapper;
        this.taskExecutor = taskExecutor;
    }

    public void estimateAfterCommit(Long aiTaskId, AuditReviewTask reviewTask, AuditReviewVersion reviewVersion,
            String reportFileUrl, String reportFileName, String operator)
    {
        Runnable task = () -> estimateAndUpdate(aiTaskId, reviewTask, reviewVersion, reportFileUrl, reportFileName,
                operator);
        if (TransactionSynchronizationManager.isSynchronizationActive())
        {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
            {
                @Override
                public void afterCommit()
                {
                    taskExecutor.execute(task);
                }
            });
        }
        else
        {
            taskExecutor.execute(task);
        }
    }

    private void estimateAndUpdate(Long aiTaskId, AuditReviewTask reviewTask, AuditReviewVersion reviewVersion,
            String reportFileUrl, String reportFileName, String operator)
    {
        if (aiTaskId == null)
        {
            return;
        }
        try
        {
            String estimatedDuration = estimatedDurationService.estimate(reviewTask, reviewVersion, reportFileUrl,
                    reportFileName);
            auditAiMapper.updateAuditAiEstimatedDuration(aiTaskId, estimatedDuration, operator);
        }
        catch (Exception e)
        {
            log.warn("Failed to estimate AI task duration, aiTaskId={}, reportFileUrl={}", aiTaskId, reportFileUrl, e);
        }
    }
}
