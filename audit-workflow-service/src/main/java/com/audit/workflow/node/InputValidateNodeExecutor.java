package com.audit.workflow.node;

import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class InputValidateNodeExecutor implements WorkflowNodeExecutor {

    @Override
    public String nodeType() {
        return NodeType.INPUT_VALIDATE;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        if (context.getTask().getWorkflowCode() == null || context.getTask().getWorkflowCode().isBlank()) {
            return NodeExecutionResult.failure("TASK_INPUT_INVALID", "workflow_code is required");
        }
        if (context.getTask().getBizId() == null || context.getTask().getBizId().isBlank()) {
            return NodeExecutionResult.failure("TASK_INPUT_INVALID", "biz_id is required");
        }
        if (context.getInput().isEmpty()) {
            return NodeExecutionResult.failure("TASK_INPUT_INVALID", "input is required");
        }
        if ("uploaded_basis_document_audit".equals(context.getTask().getWorkflowCode())) {
            if (stringValue(context.getInput().get("file_url")).isBlank()
                    && stringValue(context.getInput().get("fileUrl")).isBlank()
                    && stringValue(context.getInput().get("text")).isBlank()) {
                return NodeExecutionResult.failure("TASK_INPUT_INVALID", "file_url is required");
            }
            if (basisFileCount(context.getInput()) <= 0) {
                return NodeExecutionResult.failure("BASIS_FILE_REQUIRED", "basis_files is required");
            }
        }
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("validated", true);
        output.put("input_keys", context.getInput().keySet());
        output.put("report_file_count", hasReportInput(context.getInput()) ? 1 : 0);
        output.put("basis_file_count", basisFileCount(context.getInput()));
        context.putVariable("input_validated", true);
        return NodeExecutionResult.success(output);
    }

    private boolean hasReportInput(Map<String, Object> input) {
        return !stringValue(input.get("file_url")).isBlank()
                || !stringValue(input.get("fileUrl")).isBlank()
                || !stringValue(input.get("file_id")).isBlank()
                || !stringValue(input.get("fileId")).isBlank()
                || !stringValue(input.get("text")).isBlank();
    }

    private int basisFileCount(Map<String, Object> input) {
        Object basisFiles = input.get("basis_files");
        if (basisFiles instanceof java.util.List<?> list && !list.isEmpty()) {
            int count = 0;
            for (Object item : list) {
                if (item instanceof Map<?, ?> map
                        && (!stringValue(map.get("file_url")).isBlank() || !stringValue(map.get("fileUrl")).isBlank())) {
                    count++;
                }
            }
            return count;
        }
        Object urls = input.get("basis_file_urls");
        if (urls instanceof java.util.List<?> list) {
            int count = 0;
            for (Object item : list) {
                if (!stringValue(item).isBlank()) {
                    count++;
                }
            }
            return count;
        }
        return 0;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
