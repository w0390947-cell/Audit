package com.ruoyi.system.service.audit;

import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.FastGptAuditResult;
import com.ruoyi.system.domain.audit.workflow.AuditWorkflowCallback;

public interface IAuditWorkflowAuditService
{
    boolean isEnabled();

    FastGptAuditResult analyze(AuditAiTask task, String operator);

    void handleCallback(AuditWorkflowCallback callback, String authorization);

    void handleStageCallback(AuditWorkflowCallback callback, String authorization);
}
