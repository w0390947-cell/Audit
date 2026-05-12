package com.audit.workflow.service;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.domain.AuditWorkflow;
import com.audit.workflow.dto.AuditTaskResponse;
import com.audit.workflow.dto.CreateAuditTaskRequest;
import com.audit.workflow.dto.CreateAuditTaskResponse;
import com.audit.workflow.dto.RetryTaskResponse;
import com.audit.workflow.dto.ReviewFeedbackRequest;
import com.audit.workflow.enums.TaskStatus;
import com.audit.workflow.repository.AuditResultRepository;
import com.audit.workflow.repository.AuditTaskRepository;
import com.audit.workflow.repository.AuditWorkflowRepository;
import com.audit.workflow.repository.AuditReviewFeedbackRepository;
import com.audit.workflow.support.JsonSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class AuditTaskService {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int TASK_NO_GENERATE_MAX_ATTEMPTS = 10;

    private final Object taskNoLock = new Object();
    private final AuditTaskRepository taskRepository;
    private final AuditWorkflowRepository workflowRepository;
    private final AuditResultRepository resultRepository;
    private final AuditReviewFeedbackRepository reviewFeedbackRepository;
    private final JsonSupport jsonSupport;
    private final boolean reviewEnabled;

    public AuditTaskService(AuditTaskRepository taskRepository,
                            AuditWorkflowRepository workflowRepository,
                            AuditResultRepository resultRepository,
                            AuditReviewFeedbackRepository reviewFeedbackRepository,
                            JsonSupport jsonSupport,
                            @Value("${audit.review.enabled:true}") boolean reviewEnabled) {
        this.taskRepository = taskRepository;
        this.workflowRepository = workflowRepository;
        this.resultRepository = resultRepository;
        this.reviewFeedbackRepository = reviewFeedbackRepository;
        this.jsonSupport = jsonSupport;
        this.reviewEnabled = reviewEnabled;
    }

    public CreateAuditTaskResponse createTask(CreateAuditTaskRequest request) {
        AuditWorkflow workflow = workflowRepository.findByCode(request.getWorkflowCode())
                .orElseThrow(() -> new BusinessException("WORKFLOW_NOT_FOUND", "workflow not found"));
        if (!Boolean.TRUE.equals(workflow.getEnabled())) {
            throw new BusinessException("WORKFLOW_DISABLED", "workflow disabled");
        }
        String inputSnapshot = jsonSupport.toJson(request.getInput());
        DuplicateKeyException lastDuplicate = null;
        for (int attempt = 1; attempt <= TASK_NO_GENERATE_MAX_ATTEMPTS; attempt++) {
            synchronized (taskNoLock) {
                String taskNo = nextTaskNo();
                try {
                    Long taskId = taskRepository.insertTask(
                            taskNo,
                            request.getWorkflowCode(),
                            request.getBizId(),
                            inputSnapshot,
                            request.getCallbackUrl());
                    return new CreateAuditTaskResponse(taskId, taskNo, TaskStatus.PENDING.name());
                } catch (DuplicateKeyException ex) {
                    lastDuplicate = ex;
                }
            }
        }
        throw new BusinessException("TASK_NO_GENERATE_CONFLICT",
                "failed to generate unique task_no after " + TASK_NO_GENERATE_MAX_ATTEMPTS + " attempts: "
                        + (lastDuplicate == null ? "" : lastDuplicate.getMostSpecificCause().getMessage()));
    }

    public AuditTaskResponse getTask(Long taskId) {
        AuditTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("TASK_NOT_FOUND", "task not found"));
        return toResponse(task);
    }

    public Object getTaskResult(Long taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("TASK_NOT_FOUND", "task not found"));
        Object result = resultRepository.getResultJson(taskId);
        if (result == null) {
            throw new BusinessException("RESULT_NOT_FOUND", "result not found");
        }
        return result;
    }

    @Transactional
    public RetryTaskResponse retryTask(Long taskId) {
        AuditTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("TASK_NOT_FOUND", "task not found"));
        if (!TaskStatus.FAILED.name().equals(task.getTaskStatus())) {
            throw new BusinessException("TASK_RETRY_NOT_ALLOWED", "only failed task can be retried");
        }
        if (!taskRepository.markRetrying(taskId)) {
            throw new BusinessException("TASK_RETRY_NOT_ALLOWED", "task has reached max retry count");
        }
        AuditTask retried = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("TASK_NOT_FOUND", "task not found"));
        return new RetryTaskResponse(taskId, retried.getTaskStatus(), retried.getRetryCount());
    }

    @Transactional
    public Object reviewTask(Long taskId, ReviewFeedbackRequest request) {
        if (!reviewEnabled) {
            throw new BusinessException("REVIEW_DISABLED", "review feedback is disabled");
        }
        AuditTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("TASK_NOT_FOUND", "task not found"));
        if (request.getFeedbacks() == null || request.getFeedbacks().isEmpty()) {
            throw new BusinessException("REVIEW_FEEDBACK_EMPTY", "feedbacks is empty");
        }
        int saved = 0;
        for (ReviewFeedbackRequest.FeedbackItem item : request.getFeedbacks()) {
            if (item.getIssueId() != null && !resultRepository.issueBelongsToTask(taskId, item.getIssueId())) {
                throw new BusinessException("REVIEW_ISSUE_INVALID", "issue does not belong to task: " + item.getIssueId());
            }
            reviewFeedbackRepository.insertFeedback(task, request, item);
            if (item.getIssueId() != null) {
                resultRepository.updateIssueStatus(taskId, item.getIssueId(), toIssueStatus(item.getReviewStatus()));
            }
            saved++;
        }
        return java.util.Map.of("task_id", taskId, "saved_feedbacks", saved);
    }

    private String nextTaskNo() {
        String dayText = LocalDate.now().format(DAY_FORMAT);
        long value = taskRepository.findMaxDailyTaskSequence(dayText) + 1;
        return "AUDIT-" + dayText + "-" + String.format("%06d", value);
    }

    private AuditTaskResponse toResponse(AuditTask task) {
        AuditTaskResponse response = new AuditTaskResponse();
        response.setTaskId(task.getTaskId());
        response.setTaskNo(task.getTaskNo());
        response.setWorkflowCode(task.getWorkflowCode());
        response.setBizId(task.getBizId());
        response.setTaskStatus(task.getTaskStatus());
        response.setCurrentNodeCode(task.getCurrentNodeCode());
        response.setSummary(task.getSummary());
        response.setErrorCode(task.getErrorCode());
        response.setErrorMsg(task.getErrorMsg());
        response.setRetryCount(Optional.ofNullable(task.getRetryCount()).orElse(0));
        response.setCreateTime(task.getCreateTime());
        response.setStartTime(task.getStartTime());
        response.setFinishTime(task.getFinishTime());
        return response;
    }

    private String toIssueStatus(String reviewStatus) {
        if (reviewStatus == null || reviewStatus.isBlank()) {
            return "REVIEWED";
        }
        return switch (reviewStatus.toLowerCase()) {
            case "confirmed" -> "CONFIRMED";
            case "rejected" -> "REJECTED";
            case "modified" -> "MODIFIED";
            default -> "REVIEWED";
        };
    }
}
