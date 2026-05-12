package com.audit.workflow.repository;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.enums.TaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AuditTaskRepository {

    private final JdbcTemplate jdbcTemplate;

    public AuditTaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insertTask(String taskNo, String workflowCode, String bizId, String inputSnapshot, String callbackUrl) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_task (
                      task_no, workflow_code, biz_id, task_status, current_node_code,
                      input_snapshot, retry_count, max_retry_count, callback_url,
                      create_time, update_time
                    ) VALUES (?, ?, ?, ?, '', ?, 0, 3, ?, NOW(), NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, taskNo);
            ps.setString(2, workflowCode);
            ps.setString(3, bizId == null ? "" : bizId);
            ps.setString(4, TaskStatus.PENDING.name());
            ps.setString(5, inputSnapshot);
            ps.setString(6, callbackUrl == null ? "" : callbackUrl);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public long findMaxDailyTaskSequence(String dayText) {
        Long value = jdbcTemplate.queryForObject("""
                SELECT COALESCE(MAX(CAST(SUBSTRING(task_no, 16) AS UNSIGNED)), 0)
                FROM audit_task
                WHERE task_no LIKE ?
                """, Long.class, "AUDIT-" + dayText + "-%");
        return value == null ? 0L : value;
    }

    public Optional<AuditTask> findById(Long taskId) {
        List<AuditTask> tasks = jdbcTemplate.query("""
                SELECT * FROM audit_task WHERE task_id = ?
                """, this::mapTask, taskId);
        return tasks.stream().findFirst();
    }

    public List<AuditTask> findExecutableTasks(int limit) {
        return jdbcTemplate.query("""
                SELECT * FROM audit_task
                WHERE task_status IN ('PENDING', 'RETRYING')
                ORDER BY task_id ASC
                LIMIT ?
                """, this::mapTask, limit);
    }

    public boolean claimTask(Long taskId) {
        int updated = jdbcTemplate.update("""
                UPDATE audit_task
                SET task_status = 'RUNNING',
                    start_time = COALESCE(start_time, NOW()),
                    update_time = NOW()
                WHERE task_id = ? AND task_status IN ('PENDING', 'RETRYING')
                """, taskId);
        return updated == 1;
    }

    public void markCurrentNode(Long taskId, String nodeCode) {
        jdbcTemplate.update("""
                UPDATE audit_task
                SET current_node_code = ?, update_time = NOW()
                WHERE task_id = ?
                """, nodeCode, taskId);
    }

    public void markSuccess(Long taskId, String summaryJson) {
        jdbcTemplate.update("""
                UPDATE audit_task
                SET task_status = 'SUCCESS',
                    summary = ?,
                    error_code = '',
                    error_msg = NULL,
                    finish_time = ?,
                    update_time = ?
                WHERE task_id = ?
                """, summaryJson, LocalDateTime.now(), LocalDateTime.now(), taskId);
    }

    public void markFailed(Long taskId, String errorCode, String errorMsg) {
        jdbcTemplate.update("""
                UPDATE audit_task
                SET task_status = 'FAILED',
                    error_code = ?,
                    error_msg = ?,
                    finish_time = ?,
                    update_time = ?
                WHERE task_id = ?
                """, errorCode, errorMsg, LocalDateTime.now(), LocalDateTime.now(), taskId);
    }

    public boolean markRetrying(Long taskId) {
        int updated = jdbcTemplate.update("""
                UPDATE audit_task
                SET task_status = 'RETRYING',
                    current_node_code = '',
                    error_code = '',
                    error_msg = NULL,
                    retry_count = retry_count + 1,
                    finish_time = NULL,
                    update_time = NOW()
                WHERE task_id = ?
                  AND task_status = 'FAILED'
                  AND retry_count < max_retry_count
                """, taskId);
        return updated == 1;
    }

    private AuditTask mapTask(ResultSet rs, int rowNum) throws SQLException {
        AuditTask task = new AuditTask();
        task.setTaskId(rs.getLong("task_id"));
        task.setTaskNo(rs.getString("task_no"));
        task.setWorkflowCode(rs.getString("workflow_code"));
        task.setBizId(rs.getString("biz_id"));
        task.setTaskStatus(rs.getString("task_status"));
        task.setCurrentNodeCode(rs.getString("current_node_code"));
        task.setInputSnapshot(rs.getString("input_snapshot"));
        task.setSummary(rs.getString("summary"));
        task.setErrorCode(rs.getString("error_code"));
        task.setErrorMsg(rs.getString("error_msg"));
        task.setRetryCount(rs.getInt("retry_count"));
        task.setMaxRetryCount(rs.getInt("max_retry_count"));
        task.setCallbackUrl(rs.getString("callback_url"));
        task.setCreateTime(rs.getTimestamp("create_time") == null ? null : rs.getTimestamp("create_time").toLocalDateTime());
        task.setStartTime(rs.getTimestamp("start_time") == null ? null : rs.getTimestamp("start_time").toLocalDateTime());
        task.setFinishTime(rs.getTimestamp("finish_time") == null ? null : rs.getTimestamp("finish_time").toLocalDateTime());
        task.setUpdateTime(rs.getTimestamp("update_time") == null ? null : rs.getTimestamp("update_time").toLocalDateTime());
        return task;
    }
}
