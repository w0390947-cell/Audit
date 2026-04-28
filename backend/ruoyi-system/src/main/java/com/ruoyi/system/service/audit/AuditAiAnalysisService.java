package com.ruoyi.system.service.audit;

/**
 * AI 审核分析服务接口
 *
 * 负责协调 FastGPT 调用和数据库事务，实现"调用-保存-状态更新"的完整流程
 *
 * @author ruoyi
 */
public interface AuditAiAnalysisService
{
    /**
     * 分析并保存 AI 审核结果
     *
     * 执行流程：
     * 1. 查询任务详情并校验
     * 2. 更新任务状态为 executing
     * 3. 调用 FastGPT 工作流
     * 4. 开启事务保存结果
     *    4.1 删除旧发现项
     *    4.2 插入新发现项
     *    4.3 更新任务摘要和状态
     * 5. 失败时更新失败状态
     *
     * @param aiTaskId AI 任务ID
     * @param operator 操作人（用于 update_by 字段）
     * @throws com.ruoyi.system.exception.FastGptAuditException 当 FastGPT 调用失败时抛出
     */
    void analyzeAndSave(Long aiTaskId, String operator);
}
