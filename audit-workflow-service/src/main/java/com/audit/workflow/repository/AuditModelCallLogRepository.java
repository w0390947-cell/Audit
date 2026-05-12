package com.audit.workflow.repository;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.model.ModelRequest;
import com.audit.workflow.model.ModelResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditModelCallLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditModelCallLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertLog(AuditTask task, ModelRequest request, ModelResponse response) {
        jdbcTemplate.update("""
                INSERT INTO audit_model_call_log (
                  task_id, task_no, workflow_code, source_chunk_id, provider, model_name,
                  request_id, prompt_snapshot, response_snapshot, input_tokens, output_tokens,
                  call_status, error_code, error_msg, duration_ms, create_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """,
                task.getTaskId(),
                task.getTaskNo(),
                task.getWorkflowCode(),
                request.getSourceChunkId(),
                value(response.getProvider()),
                value(response.getModelName()),
                value(response.getRequestId()),
                value(request.getUserPrompt()),
                response.getContent(),
                response.getInputTokens(),
                response.getOutputTokens(),
                response.isSuccess() ? "SUCCESS" : "FAILED",
                value(response.getErrorCode()),
                response.getErrorMsg(),
                response.getDurationMs());
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
