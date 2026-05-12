package com.audit.workflow.repository;

import com.audit.workflow.domain.CallbackLog;
import org.springframework.beans.factory.annotation.Value;
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

@Repository
public class AuditCallbackLogRepository {

    private final JdbcTemplate jdbcTemplate;
    private final int maxRetryCount;

    public AuditCallbackLogRepository(JdbcTemplate jdbcTemplate,
                                      @Value("${audit.callback.max-retry-count:3}") int maxRetryCount) {
        this.jdbcTemplate = jdbcTemplate;
        this.maxRetryCount = maxRetryCount;
    }

    public Long insertPending(Long taskId, String taskNo, String callbackUrl, String requestPayload) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_callback_log (
                      task_id, task_no, callback_url, callback_status, request_payload,
                      retry_count, max_retry_count, create_time, update_time
                    ) VALUES (?, ?, ?, 'PENDING', ?, 0, ?, NOW(), NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, taskId);
            ps.setString(2, taskNo);
            ps.setString(3, callbackUrl);
            ps.setString(4, requestPayload);
            ps.setInt(5, maxRetryCount);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void markSuccess(Long callbackId, int responseStatus, String responseBody) {
        jdbcTemplate.update("""
                UPDATE audit_callback_log
                SET callback_status = 'SUCCESS',
                    response_status = ?,
                    response_body = ?,
                    error_msg = NULL,
                    next_retry_time = NULL,
                    update_time = NOW()
                WHERE callback_id = ?
                """, responseStatus, responseBody, callbackId);
    }

    public void markFailed(Long callbackId, Integer responseStatus, String responseBody, String errorMsg) {
        jdbcTemplate.update("""
                UPDATE audit_callback_log
                SET callback_status = 'FAILED',
                    response_status = ?,
                    response_body = ?,
                    error_msg = ?,
                    retry_count = retry_count + 1,
                    next_retry_time = DATE_ADD(NOW(), INTERVAL
                      CASE
                        WHEN retry_count = 0 THEN 1
                        WHEN retry_count = 1 THEN 5
                        ELSE 15
                      END MINUTE),
                    update_time = NOW()
                WHERE callback_id = ?
                """, responseStatus, responseBody, errorMsg, callbackId);
    }

    public List<CallbackLog> findRetryable(int limit) {
        return jdbcTemplate.query("""
                SELECT * FROM audit_callback_log
                WHERE callback_status = 'FAILED'
                  AND retry_count < max_retry_count
                  AND (next_retry_time IS NULL OR next_retry_time <= NOW())
                ORDER BY callback_id ASC
                LIMIT ?
                """, this::mapLog, limit);
    }

    public List<CallbackLog> findFailedByTaskId(Long taskId) {
        return jdbcTemplate.query("""
                SELECT * FROM audit_callback_log
                WHERE task_id = ?
                  AND callback_status = 'FAILED'
                  AND retry_count < max_retry_count
                ORDER BY callback_id ASC
                """, this::mapLog, taskId);
    }

    private CallbackLog mapLog(ResultSet rs, int rowNum) throws SQLException {
        CallbackLog log = new CallbackLog();
        log.setCallbackId(rs.getLong("callback_id"));
        log.setTaskId(rs.getLong("task_id"));
        log.setTaskNo(rs.getString("task_no"));
        log.setCallbackUrl(rs.getString("callback_url"));
        log.setCallbackStatus(rs.getString("callback_status"));
        log.setRequestPayload(rs.getString("request_payload"));
        int responseStatus = rs.getInt("response_status");
        log.setResponseStatus(rs.wasNull() ? null : responseStatus);
        log.setResponseBody(rs.getString("response_body"));
        log.setErrorMsg(rs.getString("error_msg"));
        log.setRetryCount(rs.getInt("retry_count"));
        log.setMaxRetryCount(rs.getInt("max_retry_count"));
        log.setNextRetryTime(rs.getTimestamp("next_retry_time") == null ? null : rs.getTimestamp("next_retry_time").toLocalDateTime());
        return log;
    }
}
