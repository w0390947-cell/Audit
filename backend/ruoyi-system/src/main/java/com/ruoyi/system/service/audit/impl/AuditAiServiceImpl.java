package com.ruoyi.system.service.audit.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.FastGptProperties;
import com.ruoyi.system.domain.audit.AuditAiStats;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.AuditReviewTask;
import com.ruoyi.system.domain.audit.AuditReviewVersion;
import com.ruoyi.system.mapper.audit.AuditAiFlowStageMapper;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.mapper.audit.AuditReviewMapper;
import com.ruoyi.system.service.audit.AuditAiAnalysisService;
import com.ruoyi.system.service.audit.IAuditAiService;

@Service
public class AuditAiServiceImpl implements IAuditAiService
{
    private static final Logger log = LoggerFactory.getLogger(AuditAiServiceImpl.class);

    @Autowired
    private AuditAiMapper auditAiMapper;

    @Autowired
    private AuditAiFlowStageMapper auditAiFlowStageMapper;

    @Autowired
    private AuditReviewMapper auditReviewMapper;

    @Autowired
    private AuditAiQueuePositionService auditAiQueuePositionService;

    @Autowired
    private FastGptProperties fastGptProperties;

    @Autowired
    private AuditAiAnalysisService auditAiAnalysisService;

    @Override
    public List<AuditAiTask> selectAuditAiTaskList(AuditAiTask task)
    {
        return auditAiMapper.selectAuditAiTaskList(task);
    }

    @Override
    public AuditAiTask selectAuditAiTaskDetail(Long aiTaskId)
    {
        AuditAiTask task = auditAiMapper.selectAuditAiTaskById(aiTaskId);
        if (task != null)
        {
            task.setFindingList(auditAiMapper.selectAuditAiFindingListByTaskId(aiTaskId));
            task.setFlowStageList(auditAiFlowStageMapper.selectAuditAiFlowStageListByTaskId(aiTaskId));
        }
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuditAiTask ensureAuditAiTaskByReviewTaskId(Long reviewTaskId, Long reviewVersionId, String operator)
    {
        if (reviewTaskId == null)
        {
            throw new ServiceException("审核任务ID不能为空");
        }

        AuditReviewTask reviewTask = auditReviewMapper.selectAuditReviewTaskById(reviewTaskId);
        if (reviewTask == null)
        {
            throw new ServiceException("审核任务不存在");
        }
        AuditReviewVersion reviewVersion = getReviewVersion(reviewTaskId, reviewVersionId);
        AuditAiTask existingTask = auditAiMapper.selectAuditAiTaskByReviewVersion(reviewTaskId, reviewVersion.getVersionId());
        if (existingTask != null)
        {
            return existingTask;
        }
        if ("v1.0".equals(reviewVersion.getVersionNo()))
        {
            AuditAiTask legacyTask = auditAiMapper.selectAuditAiTaskByReviewTaskWithoutVersion(reviewTaskId);
            if (legacyTask != null)
            {
                auditAiMapper.bindAuditAiTaskReviewVersion(legacyTask.getAiTaskId(), reviewVersion.getVersionId(), operator);
                return auditAiMapper.selectAuditAiTaskById(legacyTask.getAiTaskId());
            }
        }

        AuditAiTask aiTask = buildAuditAiTask(reviewTask, reviewVersion, operator);
        auditAiMapper.insertAuditAiTask(aiTask);
        auditAiQueuePositionService.resortQueuePositions(operator);
        return auditAiMapper.selectAuditAiTaskById(aiTask.getAiTaskId());
    }

    @Override
    public AuditAiStats selectAuditAiStats()
    {
        List<AuditAiTask> list = auditAiMapper.selectAuditAiAllList();
        AuditAiStats stats = new AuditAiStats();
        stats.setQueueGroupCount((int) list.stream()
                .map(AuditAiTask::getPriority)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .count());
        stats.setTotalTaskCount(list.size());
        stats.setCurrentQueueTaskCount(list.size());
        stats.setWaitingCount((int) list.stream().filter(item -> "waiting".equals(item.getTaskStatus())).count());
        stats.setExecutingCount((int) list.stream().filter(item -> "executing".equals(item.getTaskStatus())).count());
        stats.setPausedCount((int) list.stream().filter(item -> "paused".equals(item.getTaskStatus())).count());
        stats.setCompletedCount((int) list.stream().filter(item -> "completed".equals(item.getTaskStatus())).count());
        stats.setHighCount((int) list.stream().filter(item -> "high".equals(item.getPriority())).count());
        stats.setMediumCount((int) list.stream().filter(item -> "medium".equals(item.getPriority())).count());
        stats.setLowCount((int) list.stream().filter(item -> "low".equals(item.getPriority())).count());

        double avg = list.stream()
                .map(AuditAiTask::getProgressPercent)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0D);
        stats.setCompletionRate(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue());
        return stats;
    }

    @Override
    public List<String> selectSubmitterList()
    {
        return auditAiMapper.selectAuditAiSubmitterList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTaskStatus(Long[] aiTaskIds, String taskStatus, String updateBy)
    {
        if (aiTaskIds == null || aiTaskIds.length == 0 || StringUtils.isBlank(taskStatus))
        {
            return 0;
        }
        List<AuditAiTask> taskList = auditAiMapper.selectAuditAiTaskListByIds(aiTaskIds);
        if (taskList.isEmpty())
        {
            return 0;
        }
        validateTaskStatusChange(taskList, taskStatus);
        int rows = 0;
        String progressText = buildProgressText(taskStatus);
        for (AuditAiTask task : taskList)
        {
            rows += auditAiMapper.updateAuditAiTaskStatus(task.getAiTaskId(), taskStatus, progressText, updateBy);
        }
        auditAiQueuePositionService.resortQueuePositions(updateBy);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int raisePriority(Long[] aiTaskIds, String updateBy)
    {
        if (aiTaskIds == null || aiTaskIds.length == 0)
        {
            return 0;
        }
        List<AuditAiTask> changedList = auditAiMapper.selectAuditAiTaskListByIds(aiTaskIds);
        if (changedList.isEmpty())
        {
            return 0;
        }
        int rows = 0;
        for (AuditAiTask task : changedList)
        {
            rows += auditAiMapper.updateAuditAiTaskQueue(task.getAiTaskId(), bumpPriority(task.getPriority()),
                    task.getQueuePosition(), task.getTaskStatus(), updateBy);
        }
        auditAiQueuePositionService.resortQueuePositions(updateBy);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateReviewDecision(AuditAiTask task)
    {
        if (task == null || task.getAiTaskId() == null)
        {
            return 0;
        }
        AuditAiTask dbTask = auditAiMapper.selectAuditAiTaskById(task.getAiTaskId());
        if (dbTask == null)
        {
            return 0;
        }
        task.setReviewer(task.getUpdateBy());
        if ("approved".equals(task.getReviewStatus()))
        {
            task.setTaskStatus("completed");
            task.setProgressPercent(100);
            task.setProgressText("人工审核已完成");
        }
        else if ("returned".equals(task.getReviewStatus()))
        {
            task.setTaskStatus("completed");
            task.setProgressPercent(100);
            task.setProgressText("人工审核已驳回");
        }
        else
        {
            task.setTaskStatus("waiting");
            task.setProgressPercent(35);
            task.setProgressText("待修改后重新提交");
        }
        int rows = auditAiMapper.updateAuditAiReviewDecision(task);
        syncReviewTaskDecision(dbTask, task);
        auditAiQueuePositionService.resortQueuePositions(task.getUpdateBy());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditAiTaskByIds(Long[] aiTaskIds)
    {
        if (aiTaskIds == null || aiTaskIds.length == 0)
        {
            return 0;
        }
        for (Long aiTaskId : aiTaskIds)
        {
            auditAiFlowStageMapper.deleteAuditAiFlowStageByTaskId(aiTaskId);
        }
        auditAiMapper.deleteAuditAiFindingByTaskIds(aiTaskIds);
        int rows = auditAiMapper.deleteAuditAiTaskByIds(aiTaskIds);
        auditAiQueuePositionService.resortQueuePositions("admin");
        return rows;
    }

    private String bumpPriority(String priority)
    {
        if ("low".equals(priority))
        {
            return "medium";
        }
        if ("medium".equals(priority))
        {
            return "high";
        }
        return "high";
    }

    private void validateTaskStatusChange(List<AuditAiTask> taskList, String targetStatus)
    {
        if ("paused".equals(targetStatus))
        {
            for (AuditAiTask task : taskList)
            {
                if ("executing".equals(task.getTaskStatus()))
                {
                    throw new ServiceException("执行中的任务正在被工作流处理，不能暂停");
                }
                if (!"waiting".equals(task.getTaskStatus()))
                {
                    throw new ServiceException("只有等待中的任务可以暂停");
                }
            }
        }
        if ("waiting".equals(targetStatus))
        {
            for (AuditAiTask task : taskList)
            {
                if (!"paused".equals(task.getTaskStatus()))
                {
                    throw new ServiceException("只有已暂停的任务可以恢复");
                }
            }
        }
    }

    private AuditReviewVersion getReviewVersion(Long reviewTaskId, Long reviewVersionId)
    {
        if (reviewVersionId != null)
        {
            AuditReviewVersion reviewVersion = auditReviewMapper.selectAuditReviewVersionById(reviewVersionId);
            if (reviewVersion == null || !reviewTaskId.equals(reviewVersion.getTaskId()))
            {
                throw new ServiceException("审核任务版本不存在");
            }
            return reviewVersion;
        }

        List<AuditReviewVersion> versionList = auditReviewMapper.selectAuditReviewVersionListByTaskId(reviewTaskId);
        for (AuditReviewVersion reviewVersion : versionList)
        {
            if ("1".equals(reviewVersion.getCurrentFlag()))
            {
                return reviewVersion;
            }
        }
        if (!versionList.isEmpty())
        {
            return versionList.get(0);
        }
        throw new ServiceException("审核任务版本不存在");
    }

    private AuditAiTask buildAuditAiTask(AuditReviewTask reviewTask, AuditReviewVersion reviewVersion, String operator)
    {
        String reportFileUrl = StringUtils.defaultIfBlank(getPrimaryFileUrl(reviewVersion.getMainReportUrls()),
                reviewVersion.getReportFileUrl());
        AuditAiTask aiTask = new AuditAiTask();
        aiTask.setReviewTaskId(reviewTask.getTaskId());
        aiTask.setReviewVersionId(reviewVersion.getVersionId());
        aiTask.setTaskNo(reviewTask.getTaskNo());
        aiTask.setProductName(reviewTask.getProductName());
        aiTask.setDeliveryUnit(reviewTask.getDeliveryUnit());
        aiTask.setSubmitter(StringUtils.defaultIfBlank(reviewVersion.getSubmitter(), reviewTask.getSponsor()));
        aiTask.setPriority(StringUtils.defaultIfBlank(reviewTask.getPriority(), "medium"));
        aiTask.setQueuePosition(auditAiQueuePositionService.nextQueuePosition());
        aiTask.setTaskStatus("waiting");
        aiTask.setEstimatedDuration("3分钟");
        aiTask.setProgressPercent(0);
        aiTask.setProgressText("智能体等待处理");
        aiTask.setAiAnalysisCount(0);
        aiTask.setReviewStatus(StringUtils.defaultIfBlank(reviewTask.getReviewStatus(), "reviewing"));
        aiTask.setReportFileUrl(reportFileUrl);
        aiTask.setReportFileName(StringUtils.defaultIfBlank(reviewVersion.getReportFileName(),
                extractFileName(reportFileUrl, reviewTask.getProductName() + "_" + reviewVersion.getVersionNo() + ".pdf")));
        aiTask.setAiSummary(StringUtils.defaultIfBlank(reviewVersion.getAiSummary(), ""));
        aiTask.setReviewOpinion(StringUtils.defaultIfBlank(reviewVersion.getReviewOpinion(), ""));
        aiTask.setReviewer("");
        aiTask.setSubmitTime(reviewVersion.getSubmitTime() == null ? reviewTask.getSubmitTime() : reviewVersion.getSubmitTime());
        aiTask.setCreateBy(operator);
        aiTask.setRemark("由审核列表详情入口自动创建：" + reviewVersion.getVersionNo());
        return aiTask;
    }

    private String getPrimaryFileUrl(String fileUrls)
    {
        if (StringUtils.isBlank(fileUrls))
        {
            return "";
        }
        return fileUrls.split(",")[0];
    }

    private String extractFileName(String fileUrl, String fallback)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return fallback;
        }
        int index = fileUrl.lastIndexOf("/");
        return index > -1 ? fileUrl.substring(index + 1) : fileUrl;
    }

    private String buildProgressText(String taskStatus)
    {
        if ("paused".equals(taskStatus))
        {
            return "任务已暂停，等待恢复";
        }
        if ("waiting".equals(taskStatus))
        {
            return "智能体等待处理";
        }
        if ("executing".equals(taskStatus))
        {
            return "文本解析智能体处理中";
        }
        return "人工审核已完成";
    }

    private void syncReviewTaskDecision(AuditAiTask dbTask, AuditAiTask decision)
    {
        if (dbTask.getReviewTaskId() == null || StringUtils.isBlank(decision.getReviewStatus()))
        {
            return;
        }
        AuditReviewTask reviewTask = new AuditReviewTask();
        reviewTask.setTaskId(dbTask.getReviewTaskId());
        reviewTask.setReviewStatus(decision.getReviewStatus());
        reviewTask.setUpdateBy(decision.getUpdateBy());
        auditReviewMapper.updateAuditReviewTask(reviewTask);
    }

    @Override
    public int runWaitingAiAnalysis(String operator)
    {
        // 1. 检查功能开关
        if (!fastGptProperties.isEnabled())
        {
            log.debug("FastGPT is disabled, skip waiting tasks analysis");
            return 0;
        }

        // 2. 查询当前 executing 数量
        int runningCount = auditAiMapper.countRunningAiTask();
        log.debug("Current running tasks: {}", runningCount);

        // 3. 计算剩余可执行数量
        int maxRunningTasks = fastGptProperties.getMaxRunningTasks();
        int remaining = maxRunningTasks - runningCount;

        if (remaining <= 0)
        {
            log.info("Max running tasks reached ({}/{}), skip new tasks", runningCount, maxRunningTasks);
            return 0;
        }

        // 4. 查询可执行任务
        List<AuditAiTask> runnableTasks = auditAiMapper.selectRunnableAiTaskList(remaining);
        if (runnableTasks == null || runnableTasks.isEmpty())
        {
            log.debug("No waiting tasks found");
            return 0;
        }

        log.info("Found {} waiting tasks to execute", runnableTasks.size());

        // 5. 逐个同步执行分析，统一由 Quartz 控制批量和频率
        int successCount = 0;
        for (AuditAiTask task : runnableTasks)
        {
            try
            {
                if (StringUtils.isEmpty(task.getReportFileUrl()))
                {
                    log.warn("Task report URL is empty, mark as failed, aiTaskId={}", task.getAiTaskId());
                    auditAiMapper.updateAuditAiAnalysisFailure(task.getAiTaskId(), "报告文件URL为空", operator);
                    auditAiQueuePositionService.resortQueuePositions(operator);
                    continue;
                }
                auditAiAnalysisService.analyzeAndSave(task.getAiTaskId(), operator);
                successCount++;
            }
            catch (Exception e)
            {
                log.error("Failed to analyze task {}, skip and continue", task.getAiTaskId(), e);
            }
        }

        log.info("Batch analysis completed, succeeded {}/{} tasks", successCount, runnableTasks.size());
        return successCount;
    }

    @Override
    public int triggerAiAnalysis(Long aiTaskId, String operator)
    {
        log.info("Manual requeue AI analysis, aiTaskId={}, operator={}", aiTaskId, operator);

        // 1. 查询任务
        AuditAiTask task = auditAiMapper.selectAuditAiTaskById(aiTaskId);
        if (task == null)
        {
            log.warn("Task not found, aiTaskId={}", aiTaskId);
            return 0;
        }

        // 2. 检查报告 URL
        if (StringUtils.isEmpty(task.getReportFileUrl()))
        {
            log.warn("Task report URL is empty, mark as failed, aiTaskId={}", aiTaskId);
            auditAiMapper.updateAuditAiAnalysisFailure(aiTaskId, "报告文件URL为空", operator);
            auditAiQueuePositionService.resortQueuePositions(operator);
            return 0;
        }

        // 3. 正在执行中的任务不允许重复提交
        if ("executing".equals(task.getTaskStatus()))
        {
            log.warn("Task is already executing, reject requeue, aiTaskId={}", aiTaskId);
            return 0;
        }

        // 4. 只重新提交到等待队列，由 Quartz 下一轮统一领取执行
        int rows = auditAiMapper.requeueAuditAiTask(aiTaskId, 0, "智能体等待处理", operator);
        if (rows == 0)
        {
            log.warn("Failed to requeue task (already executing or deleted), aiTaskId={}", aiTaskId);
            return 0;
        }

        auditAiQueuePositionService.resortQueuePositions(operator);
        return 1;
    }
}
