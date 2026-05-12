package com.audit.workflow.controller;

import com.audit.workflow.common.ApiResponse;
import com.audit.workflow.dto.WorkflowSummaryResponse;
import com.audit.workflow.service.WorkflowDefinitionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit/workflows")
public class AuditWorkflowController {

    private final WorkflowDefinitionService workflowDefinitionService;

    public AuditWorkflowController(WorkflowDefinitionService workflowDefinitionService) {
        this.workflowDefinitionService = workflowDefinitionService;
    }

    @GetMapping
    public ApiResponse<List<WorkflowSummaryResponse>> listWorkflows() {
        return ApiResponse.ok(workflowDefinitionService.listEnabledWorkflows());
    }
}
