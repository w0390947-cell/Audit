package com.audit.workflow.domain;

import java.util.HashMap;
import java.util.Map;

public class WorkflowTaskContext {

    private final AuditTask task;
    private final Map<String, Object> input;
    private final Map<String, Object> variables = new HashMap<>();

    public WorkflowTaskContext(AuditTask task, Map<String, Object> input) {
        this.task = task;
        this.input = input == null ? new HashMap<>() : new HashMap<>(input);
    }

    public AuditTask getTask() {
        return task;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void putVariable(String key, Object value) {
        variables.put(key, value);
    }
}
