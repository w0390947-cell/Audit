package com.ruoyi.system.service.audit;

import java.util.List;
import com.ruoyi.system.domain.audit.AuditAiStats;
import com.ruoyi.system.domain.audit.AuditAiTask;

public interface IAuditAiService
{
    List<AuditAiTask> selectAuditAiTaskList(AuditAiTask task);

    AuditAiTask selectAuditAiTaskDetail(Long aiTaskId);

    AuditAiTask ensureAuditAiTaskByReviewTaskId(Long reviewTaskId, Long reviewVersionId, String operator);

    AuditAiStats selectAuditAiStats();

    List<String> selectSubmitterList();

    int updateTaskStatus(Long[] aiTaskIds, String taskStatus, String updateBy);

    int raisePriority(Long[] aiTaskIds, String updateBy);

    int updateReviewDecision(AuditAiTask task);

    int deleteAuditAiTaskByIds(Long[] aiTaskIds);

    /**
     * 批量扫描并执行等待中的 AI 分析任务
     * 用于 Quartz 定时任务调用
     *
     * @param operator 操作人（通常为 "quartz" 或 "system"）
     * @return 成功提交执行的任务数量
     */
    int runWaitingAiAnalysis(String operator);

    /**
     * 手动触发单个任务的 AI 分析
     * 用于 Controller 手动重跑接口
     *
     * @param aiTaskId AI 任务ID
     * @param operator 操作人
     * @return 提交结果（1=成功，0=失败）
     */
    int triggerAiAnalysis(Long aiTaskId, String operator);
}
