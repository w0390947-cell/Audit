package com.ruoyi.web.controller.audit;

import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.audit.workflow.AuditWorkflowCallback;
import com.ruoyi.system.service.audit.IAuditWorkflowAuditService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Anonymous
@RestController
@RequestMapping("/audit/ai/workflow")
public class AuditWorkflowCallbackController
{
    private final IAuditWorkflowAuditService auditWorkflowAuditService;

    public AuditWorkflowCallbackController(IAuditWorkflowAuditService auditWorkflowAuditService)
    {
        this.auditWorkflowAuditService = auditWorkflowAuditService;
    }

    @PostMapping("/callback")
    public AjaxResult callback(@RequestBody AuditWorkflowCallback callback,
            @RequestHeader(value = "Authorization", required = false) String authorization)
    {
        auditWorkflowAuditService.handleCallback(callback, authorization);
        return AjaxResult.success();
    }
}
