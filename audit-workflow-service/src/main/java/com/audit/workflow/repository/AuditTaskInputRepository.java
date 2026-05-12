package com.audit.workflow.repository;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.service.AuditInputFetchResult;
import com.audit.workflow.support.JsonSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@Repository
public class AuditTaskInputRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonSupport jsonSupport;

    public AuditTaskInputRepository(JdbcTemplate jdbcTemplate, JsonSupport jsonSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSupport = jsonSupport;
    }

    public void deleteByTaskId(Long taskId) {
        jdbcTemplate.update("DELETE FROM audit_task_input WHERE task_id = ?", taskId);
    }

    public Long insertSuccess(AuditTask task, AuditInputFetchResult input, ParsedDocument document) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_task_input (
                      task_id, task_no, input_type, file_id, file_url, file_name, file_type,
                      file_hash, text_hash, metadata, raw_input_snapshot, parse_status,
                      create_time, update_time
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'SUCCESS', NOW(), NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, task.getTaskId());
            ps.setString(2, task.getTaskNo());
            ps.setString(3, input.getInputType());
            ps.setString(4, value(input.getFileId()));
            ps.setString(5, value(input.getFileUrl()));
            ps.setString(6, value(input.getFileName()));
            ps.setString(7, value(input.getFileType()));
            ps.setString(8, value(input.getFileHash()));
            ps.setString(9, value(document.getTextHash()));
            ps.setString(10, jsonSupport.toJson(input.getMetadata()));
            ps.setString(11, task.getInputSnapshot());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
