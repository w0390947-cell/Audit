package com.audit.workflow.service;

import com.audit.workflow.domain.AuditWorkflow;
import com.audit.workflow.dto.WorkflowSummaryResponse;
import com.audit.workflow.repository.AuditWorkflowRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkflowDefinitionService {

    private final AuditWorkflowRepository workflowRepository;

    public WorkflowDefinitionService(AuditWorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    public List<WorkflowSummaryResponse> listEnabledWorkflows() {
        return workflowRepository.findEnabledWorkflows().stream()
                .map(this::toSummary)
                .toList();
    }

    private WorkflowSummaryResponse toSummary(AuditWorkflow workflow) {
        WorkflowSummaryResponse response = new WorkflowSummaryResponse();
        response.setWorkflowCode(workflow.getWorkflowCode());
        response.setWorkflowName(workflow.getWorkflowName());
        response.setDescription(workflow.getDescription());
        response.setEnabled(workflow.getEnabled());
        return response;
    }
}
