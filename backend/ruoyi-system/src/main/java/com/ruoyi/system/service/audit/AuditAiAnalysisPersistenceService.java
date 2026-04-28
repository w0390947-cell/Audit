package com.ruoyi.system.service.audit;

import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.FastGptAuditResult;

/**
 * AI 审核分析结果持久化服务
 *
 * 独立为 Spring Bean，确保事务通过代理生效。
 *
 * @author ruoyi
 */
public interface AuditAiAnalysisPersistenceService
{
    /**
     * 保存 AI 审核分析结果
     *
     * @param task AI 任务
     * @param result FastGPT 审核结果
     * @param operator 操作人
     */
    void saveAuditResult(AuditAiTask task, FastGptAuditResult result, String operator);
}
