package com.audit.workflow.node;

import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.WorkflowTaskContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MockNodeExecutor {

    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("mock", true);
        output.put("node_code", node.getNodeCode());
        output.put("node_type", node.getNodeType());
        output.put("message", "phase1 mock node executed");
        context.putVariable(node.getNodeCode(), output);
        return NodeExecutionResult.success(output);
    }
}
