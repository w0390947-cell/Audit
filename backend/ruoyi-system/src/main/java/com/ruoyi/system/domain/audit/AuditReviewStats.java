package com.ruoyi.system.domain.audit;

import java.io.Serializable;

public class AuditReviewStats implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer totalCount;

    private Integer reviewingCount;

    private Integer pendingCount;

    private Integer returnedCount;

    private Integer approvedCount;

    private Integer archivedCount;

    private Integer todayNewCount;

    private Integer todoCount;

    private Integer pausedCount;

    private Integer highPriorityPendingCount;

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    public Integer getReviewingCount()
    {
        return reviewingCount;
    }

    public void setReviewingCount(Integer reviewingCount)
    {
        this.reviewingCount = reviewingCount;
    }

    public Integer getPendingCount()
    {
        return pendingCount;
    }

    public void setPendingCount(Integer pendingCount)
    {
        this.pendingCount = pendingCount;
    }

    public Integer getReturnedCount()
    {
        return returnedCount;
    }

    public void setReturnedCount(Integer returnedCount)
    {
        this.returnedCount = returnedCount;
    }

    public Integer getApprovedCount()
    {
        return approvedCount;
    }

    public void setApprovedCount(Integer approvedCount)
    {
        this.approvedCount = approvedCount;
    }

    public Integer getArchivedCount()
    {
        return archivedCount;
    }

    public void setArchivedCount(Integer archivedCount)
    {
        this.archivedCount = archivedCount;
    }

    public Integer getTodayNewCount()
    {
        return todayNewCount;
    }

    public void setTodayNewCount(Integer todayNewCount)
    {
        this.todayNewCount = todayNewCount;
    }

    public Integer getTodoCount()
    {
        return todoCount;
    }

    public void setTodoCount(Integer todoCount)
    {
        this.todoCount = todoCount;
    }

    public Integer getPausedCount()
    {
        return pausedCount;
    }

    public void setPausedCount(Integer pausedCount)
    {
        this.pausedCount = pausedCount;
    }

    public Integer getHighPriorityPendingCount()
    {
        return highPriorityPendingCount;
    }

    public void setHighPriorityPendingCount(Integer highPriorityPendingCount)
    {
        this.highPriorityPendingCount = highPriorityPendingCount;
    }
}
