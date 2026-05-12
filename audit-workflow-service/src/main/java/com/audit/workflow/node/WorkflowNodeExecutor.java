package com.audit.workflow.node;

import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.WorkflowTaskContext;

public interface WorkflowNodeExecutor {

    String nodeType();

    NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node);
}
