package com.ruoyi.system.mapper.audit;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.system.domain.audit.AuditAiFinding;
import com.ruoyi.system.domain.audit.AuditAiTask;

public interface AuditAiMapper
{
    List<AuditAiTask> selectAuditAiTaskList(AuditAiTask task);

    List<AuditAiTask> selectAuditAiAllList();

    AuditAiTask selectAuditAiTaskById(Long aiTaskId);

    List<AuditAiFinding> selectAuditAiFindingListByTaskId(Long aiTaskId);

    List<AuditAiFinding> selectAuditAiFindingListByReviewTaskId(Long reviewTaskId);

    List<AuditAiTask> selectAuditAiTaskListByIds(Long[] aiTaskIds);

    List<String> selectAuditAiSubmitterList();

    int insertAuditAiTask(AuditAiTask task);

    int updateAuditAiTaskStatus(@Param("aiTaskId") Long aiTaskId, @Param("taskStatus") String taskStatus,
            @Param("progressText") String progressText, @Param("updateBy") String updateBy);

    int updateAuditAiTaskQueue(@Param("aiTaskId") Long aiTaskId, @Param("priority") String priority,
            @Param("queuePosition") Integer queuePosition, @Param("taskStatus") String taskStatus,
            @Param("updateBy") String updateBy);

    int updateAuditAiReviewDecision(AuditAiTask task);

    int deleteAuditAiFindingByTaskIds(Long[] aiTaskIds);

    int deleteAuditAiTaskByIds(Long[] aiTaskIds);

    /**
     * 插入单条 AI 发现项
     *
     * @param finding 发现项对象
     * @return 插入行数
     */
    int insertAuditAiFinding(AuditAiFinding finding);

    /**
     * 更新 AI 分析结果（成功时调用）
     *
     * @param task AI 任务对象（包含状态、摘要、进度等）
     * @return 更新行数
     */
    int updateAuditAiAnalysisResult(AuditAiTask task);

    /**
     * 更新 AI 分析失败状态
     *
     * @param aiTaskId AI 任务ID
     * @param progressText 进度文本（错误信息）
     * @param updateBy 更新者
     * @return 更新行数
     */
    int updateAuditAiAnalysisFailure(@Param("aiTaskId") Long aiTaskId,
            @Param("progressText") String progressText,
            @Param("updateBy") String updateBy);

    /**
     * 将 AI 任务标记为执行中（不增加 AI 分析次数）
     *
     * @param aiTaskId AI 任务ID
     * @param progressPercent 进度百分比
     * @param progressText 进度文本
     * @param updateBy 更新者
     * @return 更新行数
     */
    int updateAuditAiTaskExecuting(@Param("aiTaskId") Long aiTaskId,
            @Param("progressPercent") Integer progressPercent,
            @Param("progressText") String progressText,
            @Param("updateBy") String updateBy);

    /**
     * 将 AI 任务重新提交到等待队列
     *
     * @param aiTaskId AI 任务ID
     * @param progressPercent 进度百分比
     * @param progressText 进度文本
     * @param updateBy 更新者
     * @return 更新行数
     */
    int requeueAuditAiTask(@Param("aiTaskId") Long aiTaskId,
            @Param("progressPercent") Integer progressPercent,
            @Param("progressText") String progressText,
            @Param("updateBy") String updateBy);

    /**
     * 查询可执行的 AI 任务列表（等待状态）
     *
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AuditAiTask> selectRunnableAiTaskList(@Param("limit") Integer limit);

    /**
     * 统计正在执行的 AI 任务数量
     *
     * @return 执行中的任务数量
     */
    int countRunningAiTask();
}
