package com.audit.workflow.service;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.repository.AuditTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditTaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuditTaskScheduler.class);

    private final AuditTaskRepository taskRepository;
    private final WorkflowEngine workflowEngine;
    private final CallbackService callbackService;
    private final int batchSize;

    public AuditTaskScheduler(AuditTaskRepository taskRepository,
                              WorkflowEngine workflowEngine,
                              CallbackService callbackService,
                              @Value("${audit.task.batch-size:10}") int batchSize) {
        this.taskRepository = taskRepository;
        this.workflowEngine = workflowEngine;
        this.callbackService = callbackService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${audit.task.scan-interval-ms:5000}")
    public void scanAndExecute() {
        List<AuditTask> tasks = taskRepository.findExecutableTasks(batchSize);
        for (AuditTask task : tasks) {
            try {
                if (taskRepository.claimTask(task.getTaskId())) {
                    workflowEngine.executeTask(task.getTaskId());
                }
            } catch (Exception ex) {
                log.warn("Failed to execute audit task. taskId={}", task.getTaskId(), ex);
                taskRepository.markFailed(task.getTaskId(), "TASK_EXECUTE_FAILED", ex.getMessage());
                callbackService.sendFinalCallback(task, "FAILED", "TASK_EXECUTE_FAILED", ex.getMessage());
            }
        }
    }
}
