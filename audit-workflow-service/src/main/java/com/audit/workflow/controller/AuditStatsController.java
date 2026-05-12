package com.audit.workflow.controller;

import com.audit.workflow.common.ApiResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audit/stats")
public class AuditStatsController {

    private final JdbcTemplate jdbcTemplate;

    public AuditStatsController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("task_total", queryLong("SELECT COUNT(1) FROM audit_task"));
        stats.put("task_success", queryLong("SELECT COUNT(1) FROM audit_task WHERE task_status = 'SUCCESS'"));
        stats.put("task_failed", queryLong("SELECT COUNT(1) FROM audit_task WHERE task_status = 'FAILED'"));
        stats.put("model_call_total", queryLong("SELECT COUNT(1) FROM audit_model_call_log"));
        stats.put("retrieval_total", queryLong("SELECT COUNT(1) FROM audit_retrieval_record"));
        stats.put("callback_success", queryLong("SELECT COUNT(1) FROM audit_callback_log WHERE callback_status = 'SUCCESS'"));
        stats.put("callback_failed", queryLong("SELECT COUNT(1) FROM audit_callback_log WHERE callback_status = 'FAILED'"));
        stats.put("review_feedback_total", queryLong("SELECT COUNT(1) FROM audit_review_feedback"));
        return ApiResponse.ok(stats);
    }

    private Long queryLong(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
