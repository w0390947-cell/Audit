package com.audit.workflow.controller;

import com.audit.workflow.common.ApiResponse;
import com.audit.workflow.dto.AuditTaskResponse;
import com.audit.workflow.dto.CreateAuditTaskRequest;
import com.audit.workflow.dto.CreateAuditTaskResponse;
import com.audit.workflow.dto.RetryTaskResponse;
import com.audit.workflow.dto.ReviewFeedbackRequest;
import com.audit.workflow.service.AuditTaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit/tasks")
public class AuditTaskController {

    private final AuditTaskService auditTaskService;

    public AuditTaskController(AuditTaskService auditTaskService) {
        this.auditTaskService = auditTaskService;
    }

    @PostMapping
    public ApiResponse<CreateAuditTaskResponse> createTask(@Valid @RequestBody CreateAuditTaskRequest request) {
        return ApiResponse.ok(auditTaskService.createTask(request));
    }

    @GetMapping("/{taskId}")
    public ApiResponse<AuditTaskResponse> getTask(@PathVariable Long taskId) {
        return ApiResponse.ok(auditTaskService.getTask(taskId));
    }

    @GetMapping("/{taskId}/result")
    public ApiResponse<Object> getTaskResult(@PathVariable Long taskId) {
        return ApiResponse.ok(auditTaskService.getTaskResult(taskId));
    }

    @PostMapping("/{taskId}/retry")
    public ApiResponse<RetryTaskResponse> retryTask(@PathVariable Long taskId) {
        return ApiResponse.ok(auditTaskService.retryTask(taskId));
    }

    @PostMapping("/{taskId}/review")
    public ApiResponse<Object> reviewTask(@PathVariable Long taskId, @RequestBody ReviewFeedbackRequest request) {
        return ApiResponse.ok(auditTaskService.reviewTask(taskId, request));
    }
}
