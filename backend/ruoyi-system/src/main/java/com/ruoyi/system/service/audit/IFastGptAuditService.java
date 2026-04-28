package com.ruoyi.system.service.audit;

import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.FastGptAuditResult;

/**
 * FastGPT 审核服务接口
 *
 * @author ruoyi
 */
public interface IFastGptAuditService
{
    /**
     * 分析 AI 任务
     *
     * 根据 AuditAiTask 构造 FastGPT 请求，调用 FastGPT 工作流，解析响应并返回结构化结果
     *
     * @param task AI 任务对象
     * @return FastGPT 审核结果
     * @throws com.ruoyi.system.exception.FastGptAuditException 当 FastGPT 调用失败或响应解析失败时抛出
     */
    FastGptAuditResult analyze(AuditAiTask task);
}
