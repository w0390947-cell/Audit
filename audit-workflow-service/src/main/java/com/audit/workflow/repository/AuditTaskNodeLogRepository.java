package com.audit.workflow.repository;

import com.audit.workflow.enums.NodeStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class AuditTaskNodeLogRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditTaskNodeLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insertRunningLog(Long taskId, String taskNo, String workflowCode, String nodeCode,
                                 String nodeType, String inputSnapshot) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_task_node_log (
                      task_id, task_no, workflow_code, node_code, node_type, node_status,
                      input_snapshot, start_time, create_time
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, taskId);
            ps.setString(2, taskNo);
            ps.setString(3, workflowCode);
            ps.setString(4, nodeCode);
            ps.setString(5, nodeType);
            ps.setString(6, NodeStatus.RUNNING.name());
            ps.setString(7, inputSnapshot);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void markSuccess(Long logId, String outputSnapshot, long durationMs) {
        jdbcTemplate.update("""
                UPDATE audit_task_node_log
                SET node_status = 'SUCCESS',
                    output_snapshot = ?,
                    finish_time = ?,
                    duration_ms = ?
                WHERE log_id = ?
                """, outputSnapshot, LocalDateTime.now(), durationMs, logId);
    }

    public void markFailed(Long logId, String outputSnapshot, String errorCode, String errorMsg, long durationMs) {
        jdbcTemplate.update("""
                UPDATE audit_task_node_log
                SET node_status = 'FAILED',
                    output_snapshot = ?,
                    error_code = ?,
                    error_msg = ?,
                    finish_time = ?,
                    duration_ms = ?
                WHERE log_id = ?
                """, outputSnapshot, errorCode, errorMsg, LocalDateTime.now(), durationMs, logId);
    }

    public List<Map<String, Object>> findStageLogsByTaskId(Long taskId) {
        return jdbcTemplate.query("""
                SELECT l.log_id, l.task_id, l.task_no, l.workflow_code, l.node_code, l.node_type,
                       l.node_status, l.output_snapshot, l.error_code, l.error_msg,
                       l.start_time, l.finish_time, l.duration_ms,
                       n.node_name, n.node_order
                FROM audit_task_node_log l
                LEFT JOIN audit_workflow_node n
                  ON n.workflow_code = l.workflow_code
                 AND n.node_code = l.node_code
                WHERE l.task_id = ?
                ORDER BY COALESCE(n.node_order, 9999), l.log_id
                """, this::mapStageLog, taskId);
    }

    private Map<String, Object> mapStageLog(ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("log_id", rs.getLong("log_id"));
        row.put("task_id", rs.getLong("task_id"));
        row.put("task_no", rs.getString("task_no"));
        row.put("workflow_code", rs.getString("workflow_code"));
        row.put("node_code", rs.getString("node_code"));
        row.put("node_type", rs.getString("node_type"));
        row.put("node_status", rs.getString("node_status"));
        row.put("output_snapshot", rs.getString("output_snapshot"));
        row.put("error_code", rs.getString("error_code"));
        row.put("error_msg", rs.getString("error_msg"));
        row.put("start_time", toLocalDateTime(rs, "start_time"));
        row.put("finish_time", toLocalDateTime(rs, "finish_time"));
        row.put("duration_ms", rs.getLong("duration_ms"));
        row.put("node_name", rs.getString("node_name"));
        row.put("node_order", rs.getInt("node_order"));
        return row;
    }

    private LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        var value = rs.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }
}
