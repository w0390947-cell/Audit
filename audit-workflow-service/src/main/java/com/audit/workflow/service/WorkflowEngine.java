package com.audit.workflow.service;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.repository.AuditTaskNodeLogRepository;
import com.audit.workflow.repository.AuditTaskRepository;
import com.audit.workflow.repository.AuditWorkflowRepository;
import com.audit.workflow.support.JsonSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkflowEngine {

    private static final Logger log = LoggerFactory.getLogger(WorkflowEngine.class);

    private final AuditTaskRepository taskRepository;
    private final AuditWorkflowRepository workflowRepository;
    private final AuditTaskNodeLogRepository nodeLogRepository;
    private final NodeExecutorRegistry nodeExecutorRegistry;
    private final JsonSupport jsonSupport;
    private final CallbackService callbackService;

    public WorkflowEngine(AuditTaskRepository taskRepository,
                          AuditWorkflowRepository workflowRepository,
                          AuditTaskNodeLogRepository nodeLogRepository,
                          NodeExecutorRegistry nodeExecutorRegistry,
                          JsonSupport jsonSupport,
                          CallbackService callbackService) {
        this.taskRepository = taskRepository;
        this.workflowRepository = workflowRepository;
        this.nodeLogRepository = nodeLogRepository;
        this.nodeExecutorRegistry = nodeExecutorRegistry;
        this.jsonSupport = jsonSupport;
        this.callbackService = callbackService;
    }

    @Transactional
    public void executeTask(Long taskId) {
        AuditTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("task not found: " + taskId));
        List<AuditWorkflowNode> nodes = workflowRepository.findEnabledNodes(task.getWorkflowCode());
        if (nodes.isEmpty()) {
            taskRepository.markFailed(taskId, "WORKFLOW_NODE_EMPTY", "workflow has no enabled nodes");
            callbackService.sendFinalCallback(task, "FAILED", "WORKFLOW_NODE_EMPTY", "workflow has no enabled nodes");
            return;
        }

        WorkflowTaskContext context = new WorkflowTaskContext(task, jsonSupport.toMap(task.getInputSnapshot()));
        for (AuditWorkflowNode node : nodes) {
            taskRepository.markCurrentNode(taskId, node.getNodeCode());
            long start = System.currentTimeMillis();
            LocalDateTime stageStartedAt = LocalDateTime.now();
            Long logId = nodeLogRepository.insertRunningLog(
                    taskId,
                    task.getTaskNo(),
                    task.getWorkflowCode(),
                    node.getNodeCode(),
                    node.getNodeType(),
                    nodeInputSnapshot(context));
            try {
                callbackService.sendStageCallback(task, node, "RUNNING", null, stageStartedAt, 0);
                NodeExecutionResult result = nodeExecutorRegistry.execute(context, node);
                long durationMs = System.currentTimeMillis() - start;
                if (!result.isSuccess()) {
                    nodeLogRepository.markFailed(
                            logId,
                            jsonSupport.toJson(result.getOutput()),
                            result.getErrorCode(),
                            result.getErrorMsg(),
                            durationMs);
                    callbackService.sendStageCallback(task, node, "FAILED", result, stageStartedAt, durationMs);
                    taskRepository.markFailed(taskId, result.getErrorCode(), result.getErrorMsg());
                    callbackService.sendFinalCallback(task, "FAILED", result.getErrorCode(), result.getErrorMsg());
                    return;
                }
                context.putVariable(node.getNodeCode() + "_output", result.getOutput());
                nodeLogRepository.markSuccess(logId, jsonSupport.toJson(result.getOutput()), durationMs);
                callbackService.sendStageCallback(task, node, "SUCCESS", result, stageStartedAt, durationMs);
            } catch (Exception ex) {
                long durationMs = System.currentTimeMillis() - start;
                log.warn("Workflow node failed. taskId={}, nodeCode={}", taskId, node.getNodeCode(), ex);
                String errorCode = ex instanceof BusinessException businessException
                        ? businessException.getErrorCode()
                        : "NODE_EXECUTE_FAILED";
                NodeExecutionResult failedResult = NodeExecutionResult.failure(errorCode, ex.getMessage());
                nodeLogRepository.markFailed(logId, "{}", errorCode, ex.getMessage(), durationMs);
                callbackService.sendStageCallback(task, node, "FAILED", failedResult, stageStartedAt, durationMs);
                taskRepository.markFailed(taskId, errorCode, ex.getMessage());
                callbackService.sendFinalCallback(task, "FAILED", errorCode, ex.getMessage());
                return;
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        Object taskSummary = context.getVariables().get("task_summary");
        if (taskSummary instanceof Map<?, ?> rawSummary) {
            for (Map.Entry<?, ?> entry : rawSummary.entrySet()) {
                if (entry.getKey() != null) {
                    summary.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        } else {
            summary.put("phase", "phase5");
            summary.put("partially_mock", true);
            summary.put("node_count", nodes.size());
            summary.put("message", "phase5 workflow executed successfully");
        }
        taskRepository.markSuccess(taskId, jsonSupport.toJson(summary));
        callbackService.sendFinalCallback(task, "SUCCESS", null, null);
    }

    private String nodeInputSnapshot(WorkflowTaskContext context) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("input_keys", context.getInput().keySet());
        snapshot.put("variable_keys", context.getVariables().keySet());
        return jsonSupport.toJson(snapshot);
    }
}
