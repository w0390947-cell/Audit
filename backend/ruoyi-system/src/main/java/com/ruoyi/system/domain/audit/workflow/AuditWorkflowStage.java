package com.ruoyi.system.domain.audit.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class AuditWorkflowStage
{
    @JsonProperty("stage_code")
    private String stageCode;

    @JsonProperty("stage_instance_id")
    private String stageInstanceId;

    @JsonProperty("stage_name")
    private String stageName;

    @JsonProperty("stage_status")
    private String stageStatus;

    @JsonProperty("agent_name")
    private String agentName;

    @JsonProperty("started_at")
    private String startedAt;

    @JsonProperty("finished_at")
    private String finishedAt;

    @JsonProperty("duration_ms")
    private Long durationMs;

    private String summary;

    private String detail;

    private Map<String, Object> output;

    private Object error;

    @JsonProperty("sort_num")
    private Integer sortNum;

    public String getStageCode()
    {
        return stageCode;
    }

    public void setStageCode(String stageCode)
    {
        this.stageCode = stageCode;
    }

    public String getStageInstanceId()
    {
        return stageInstanceId;
    }

    public void setStageInstanceId(String stageInstanceId)
    {
        this.stageInstanceId = stageInstanceId;
    }

    public String getStageName()
    {
        return stageName;
    }

    public void setStageName(String stageName)
    {
        this.stageName = stageName;
    }

    public String getStageStatus()
    {
        return stageStatus;
    }

    public void setStageStatus(String stageStatus)
    {
        this.stageStatus = stageStatus;
    }

    public String getAgentName()
    {
        return agentName;
    }

    public void setAgentName(String agentName)
    {
        this.agentName = agentName;
    }

    public String getStartedAt()
    {
        return startedAt;
    }

    public void setStartedAt(String startedAt)
    {
        this.startedAt = startedAt;
    }

    public String getFinishedAt()
    {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt)
    {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs()
    {
        return durationMs;
    }

    public void setDurationMs(Long durationMs)
    {
        this.durationMs = durationMs;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    public String getDetail()
    {
        return detail;
    }

    public void setDetail(String detail)
    {
        this.detail = detail;
    }

    public Map<String, Object> getOutput()
    {
        return output;
    }

    public void setOutput(Map<String, Object> output)
    {
        this.output = output;
    }

    public Object getError()
    {
        return error;
    }

    public void setError(Object error)
    {
        this.error = error;
    }

    public Integer getSortNum()
    {
        return sortNum;
    }

    public void setSortNum(Integer sortNum)
    {
        this.sortNum = sortNum;
    }
}
