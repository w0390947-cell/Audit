package com.audit.workflow.node;

import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CallbackNodeExecutor implements WorkflowNodeExecutor {

    @Override
    public String nodeType() {
        return NodeType.CALLBACK;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("callback_status", "DEFERRED_UNTIL_TASK_TERMINAL");
        return NodeExecutionResult.success(output);
    }
}
