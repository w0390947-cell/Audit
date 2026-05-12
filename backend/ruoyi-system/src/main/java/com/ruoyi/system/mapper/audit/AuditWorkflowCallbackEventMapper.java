package com.ruoyi.system.mapper.audit;

import com.ruoyi.system.domain.audit.workflow.AuditWorkflowCallbackEvent;
import org.apache.ibatis.annotations.Param;

public interface AuditWorkflowCallbackEventMapper
{
    AuditWorkflowCallbackEvent selectAuditWorkflowCallbackEventByEventId(String callbackEventId);

    int insertAuditWorkflowCallbackEvent(AuditWorkflowCallbackEvent event);

    int updateAuditWorkflowCallbackEventStatus(@Param("callbackEventId") String callbackEventId,
            @Param("eventStatus") String eventStatus,
            @Param("errorMessage") String errorMessage);

    int retryFailedAuditWorkflowCallbackEvent(@Param("callbackEventId") String callbackEventId,
            @Param("aiTaskId") Long aiTaskId,
            @Param("workflowTaskId") String workflowTaskId,
            @Param("rawPayload") String rawPayload);
}
