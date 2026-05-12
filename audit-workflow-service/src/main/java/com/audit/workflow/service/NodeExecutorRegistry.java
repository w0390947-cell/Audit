package com.audit.workflow.service;

import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.node.MockNodeExecutor;
import com.audit.workflow.node.WorkflowNodeExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NodeExecutorRegistry {

    private final Map<String, WorkflowNodeExecutor> executorMap = new HashMap<>();
    private final MockNodeExecutor mockNodeExecutor;

    public NodeExecutorRegistry(List<WorkflowNodeExecutor> executors, MockNodeExecutor mockNodeExecutor) {
        for (WorkflowNodeExecutor executor : executors) {
            executorMap.put(executor.nodeType(), executor);
        }
        this.mockNodeExecutor = mockNodeExecutor;
    }

    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        WorkflowNodeExecutor executor = executorMap.get(node.getNodeType());
        if (executor != null) {
            return executor.execute(context, node);
        }
        return mockNodeExecutor.execute(context, node);
    }
}
