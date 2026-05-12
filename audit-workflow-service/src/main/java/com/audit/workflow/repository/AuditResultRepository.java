package com.audit.workflow.repository;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.domain.RetrievalReference;
import com.audit.workflow.support.JsonSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class AuditResultRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonSupport jsonSupport;

    public AuditResultRepository(JdbcTemplate jdbcTemplate, JsonSupport jsonSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSupport = jsonSupport;
    }

    public void deleteByTaskId(Long taskId) {
        jdbcTemplate.update("DELETE FROM audit_result_reference WHERE task_id = ?", taskId);
        jdbcTemplate.update("DELETE FROM audit_result_issue WHERE task_id = ?", taskId);
        jdbcTemplate.update("DELETE FROM audit_result WHERE task_id = ?", taskId);
    }

    public Long insertResult(AuditTask task, Map<String, Object> result) {
        Map<String, Object> sanitizedResult = removeModelUsed(result);
        Map<String, Object> summary = buildSummary(sanitizedResult);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_result (
                      task_id, task_no, workflow_code, overall_result, risk_level, total_issues,
                      summary, result_json, validate_status, create_time, update_time
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SUCCESS', NOW(), NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, task.getTaskId());
            ps.setString(2, task.getTaskNo());
            ps.setString(3, task.getWorkflowCode());
            ps.setString(4, value(summary.get("overall_result")));
            ps.setString(5, value(summary.get("risk_level")));
            ps.setInt(6, intValue(summary.get("total_issues")));
            ps.setString(7, jsonSupport.toJson(summary));
            ps.setString(8, jsonSupport.toJson(sanitizedResult));
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private Map<String, Object> removeModelUsed(Map<String, Object> result) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : result.entrySet()) {
            if (!"model_used".equals(entry.getKey())) {
                sanitized.put(entry.getKey(), removeModelUsedValue(entry.getValue()));
            }
        }
        return sanitized;
    }

    private Object removeModelUsedValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() != null && !"model_used".equals(String.valueOf(entry.getKey()))) {
                    sanitized.put(String.valueOf(entry.getKey()), removeModelUsedValue(entry.getValue()));
                }
            }
            return sanitized;
        }
        if (value instanceof List<?> list) {
            List<Object> sanitized = new ArrayList<>();
            for (Object item : list) {
                sanitized.add(removeModelUsedValue(item));
            }
            return sanitized;
        }
        return value;
    }

    private Map<String, Object> buildSummary(Map<String, Object> result) {
        Map<String, Object> summary = mapValue(result.get("summary"));
        if (!summary.isEmpty()) {
            return summary;
        }
        int totalIssues = intValue(first(result, "total_issues", "totalIssues"));
        summary.put("overall_result", totalIssues == 0 ? "通过" : "需要整改");
        summary.put("risk_level", highestFindingRisk(result.get("findings")));
        summary.put("total_issues", totalIssues);
        summary.put("summary", value(result.get("summary")));
        return summary;
    }

    private String highestFindingRisk(Object findings) {
        String risk = "low";
        if (findings instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> finding = mapValue(item);
                risk = higherRisk(risk, value(finding.get("severity")));
            }
        }
        return risk;
    }

    private String higherRisk(String left, String right) {
        return riskRank(right) > riskRank(left) ? right : left;
    }

    private int riskRank(String risk) {
        return switch (risk == null ? "" : risk) {
            case "high" -> 3;
            case "medium" -> 2;
            default -> 1;
        };
    }

    public Long insertIssue(Long resultId, AuditTask task, Map<String, Object> issue, int index) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_result_issue (
                      task_id, result_id, issue_no, source_chunk_id, title, risk_level,
                      problem, suggestion, confidence, location, issue_status,
                      create_time, update_time
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'AI_GENERATED', NOW(), NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, task.getTaskId());
            ps.setLong(2, resultId);
            ps.setString(3, "ISSUE-" + String.format("%03d", index));
            Long sourceChunkId = longValue(issue.get("source_chunk_id"));
            if (sourceChunkId == null) {
                sourceChunkId = longValue(mapValue(issue.get("location")).get("source_chunk_id"));
            }
            ps.setObject(4, sourceChunkId);
            ps.setString(5, value(issue.get("title")));
            ps.setString(6, value(issue.get("risk_level")));
            ps.setString(7, value(issue.get("problem")));
            ps.setString(8, value(issue.get("suggestion")));
            ps.setBigDecimal(9, decimalValue(issue.get("confidence")));
            ps.setString(10, jsonSupport.toJson(mapValue(issue.get("location"))));
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void insertResultReference(AuditTask task, Long issueId, RetrievalReference reference, String quoteText) {
        jdbcTemplate.update("""
                INSERT INTO audit_result_reference (
                  task_id, issue_id, retrieval_reference_id, kb_chunk_id, file_name, file_url,
                  version_no, page_no, section_title, quote_text, create_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """,
                task.getTaskId(),
                issueId,
                reference == null ? null : reference.getReferenceId(),
                reference == null ? "" : value(reference.getKbChunkId()),
                reference == null ? "" : value(reference.getFileName()),
                reference == null ? "" : value(reference.getFileUrl()),
                reference == null ? "" : value(reference.getVersionNo()),
                reference == null ? null : reference.getPageNo(),
                reference == null ? "" : value(reference.getSectionTitle()),
                quoteText);
    }

    public Map<String, Object> getResultJson(Long taskId) {
        List<Map<String, Object>> rows = jdbcTemplate.query("""
                SELECT result_json FROM audit_result WHERE task_id = ?
                """, (rs, rowNum) -> jsonSupport.toMap(rs.getString("result_json")), taskId);
        return rows.stream().findFirst().orElse(null);
    }

    public boolean issueBelongsToTask(Long taskId, Long issueId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1) FROM audit_result_issue
                WHERE task_id = ? AND issue_id = ?
                """, Integer.class, taskId, issueId);
        return count != null && count > 0;
    }

    public void updateIssueStatus(Long taskId, Long issueId, String issueStatus) {
        jdbcTemplate.update("""
                UPDATE audit_result_issue
                SET issue_status = ?, update_time = NOW()
                WHERE task_id = ? AND issue_id = ?
                """, issueStatus, taskId, issueId);
    }

    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> rawMap) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() != null) {
                    map.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return map;
        }
        return new LinkedHashMap<>();
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Object first(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
    }

    private int intValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Long longValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
