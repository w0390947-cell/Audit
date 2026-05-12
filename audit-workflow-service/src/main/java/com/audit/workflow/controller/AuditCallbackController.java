package com.audit.workflow.controller;

import com.audit.workflow.common.ApiResponse;
import com.audit.workflow.service.CallbackService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/audit/callbacks")
public class AuditCallbackController {

    private final CallbackService callbackService;

    public AuditCallbackController(CallbackService callbackService) {
        this.callbackService = callbackService;
    }

    @PostMapping("/{taskId}/retry")
    public ApiResponse<Map<String, Object>> retryTaskCallbacks(@PathVariable Long taskId) {
        int retryCount = callbackService.retryTaskCallbacks(taskId);
        return ApiResponse.ok(Map.of("task_id", taskId, "retried_callbacks", retryCount));
    }
}
