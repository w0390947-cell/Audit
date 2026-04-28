package com.ruoyi.system.domain.audit;

import java.io.Serializable;

public class AuditAiStats implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer queueGroupCount;

    private Integer totalTaskCount;

    private Integer currentQueueTaskCount;

    private Integer waitingCount;

    private Integer executingCount;

    private Integer pausedCount;

    private Double completionRate;

    private Integer highCount;

    private Integer mediumCount;

    private Integer lowCount;

    public Integer getQueueGroupCount()
    {
        return queueGroupCount;
    }

    public void setQueueGroupCount(Integer queueGroupCount)
    {
        this.queueGroupCount = queueGroupCount;
    }

    public Integer getTotalTaskCount()
    {
        return totalTaskCount;
    }

    public void setTotalTaskCount(Integer totalTaskCount)
    {
        this.totalTaskCount = totalTaskCount;
    }

    public Integer getCurrentQueueTaskCount()
    {
        return currentQueueTaskCount;
    }

    public void setCurrentQueueTaskCount(Integer currentQueueTaskCount)
    {
        this.currentQueueTaskCount = currentQueueTaskCount;
    }

    public Integer getWaitingCount()
    {
        return waitingCount;
    }

    public void setWaitingCount(Integer waitingCount)
    {
        this.waitingCount = waitingCount;
    }

    public Integer getExecutingCount()
    {
        return executingCount;
    }

    public void setExecutingCount(Integer executingCount)
    {
        this.executingCount = executingCount;
    }

    public Integer getPausedCount()
    {
        return pausedCount;
    }

    public void setPausedCount(Integer pausedCount)
    {
        this.pausedCount = pausedCount;
    }

    public Double getCompletionRate()
    {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate)
    {
        this.completionRate = completionRate;
    }

    public Integer getHighCount()
    {
        return highCount;
    }

    public void setHighCount(Integer highCount)
    {
        this.highCount = highCount;
    }

    public Integer getMediumCount()
    {
        return mediumCount;
    }

    public void setMediumCount(Integer mediumCount)
    {
        this.mediumCount = mediumCount;
    }

    public Integer getLowCount()
    {
        return lowCount;
    }

    public void setLowCount(Integer lowCount)
    {
        this.lowCount = lowCount;
    }
}
