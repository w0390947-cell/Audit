package com.ruoyi.system.service.audit.impl;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.mapper.audit.AuditAiMapper;

@Service
public class AuditAiQueuePositionService
{
    private final AuditAiMapper auditAiMapper;

    public AuditAiQueuePositionService(AuditAiMapper auditAiMapper)
    {
        this.auditAiMapper = auditAiMapper;
    }

    public int nextQueuePosition()
    {
        List<AuditAiTask> list = auditAiMapper.selectAuditAiAllList();
        int max = 0;
        for (AuditAiTask task : list)
        {
            if (task.getQueuePosition() != null && task.getQueuePosition() > max)
            {
                max = task.getQueuePosition();
            }
        }
        return max + 1;
    }

    public int resortQueuePositions(String updateBy)
    {
        List<AuditAiTask> list = auditAiMapper.selectAuditAiAllList();
        if (list.isEmpty())
        {
            return 0;
        }
        list.sort(Comparator.comparingInt((AuditAiTask item) -> statusRank(item.getTaskStatus()))
                .thenComparingInt(item -> priorityRank(item.getPriority()))
                .thenComparingInt(this::queuePositionRank)
                .thenComparing(AuditAiTask::getAiTaskId));

        int rows = 0;
        int queuePosition = 1;
        for (AuditAiTask task : list)
        {
            if ("completed".equals(task.getTaskStatus()))
            {
                task.setQueuePosition(0);
            }
            else
            {
                task.setQueuePosition(queuePosition++);
            }
            rows += auditAiMapper.updateAuditAiTaskQueue(task.getAiTaskId(), task.getPriority(), task.getQueuePosition(),
                    task.getTaskStatus(), updateBy);
        }
        return rows;
    }

    private int priorityRank(String priority)
    {
        if ("high".equals(priority))
        {
            return 1;
        }
        if ("medium".equals(priority))
        {
            return 2;
        }
        return 3;
    }

    private int statusRank(String taskStatus)
    {
        if ("executing".equals(taskStatus))
        {
            return 1;
        }
        if ("waiting".equals(taskStatus))
        {
            return 2;
        }
        if ("paused".equals(taskStatus))
        {
            return 3;
        }
        return 4;
    }

    private int queuePositionRank(AuditAiTask task)
    {
        Integer queuePosition = task.getQueuePosition();
        if (queuePosition == null || queuePosition <= 0)
        {
            return Integer.MAX_VALUE;
        }
        return queuePosition;
    }
}
