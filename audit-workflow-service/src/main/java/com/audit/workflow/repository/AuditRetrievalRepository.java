package com.audit.workflow.repository;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.domain.RetrievalReference;
import com.audit.workflow.retrieval.RetrievalRequest;
import com.audit.workflow.support.JsonSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Repository
public class AuditRetrievalRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonSupport jsonSupport;

    public AuditRetrievalRepository(JdbcTemplate jdbcTemplate, JsonSupport jsonSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSupport = jsonSupport;
    }

    public void deleteByTaskId(Long taskId) {
        jdbcTemplate.update("DELETE FROM audit_retrieval_reference WHERE task_id = ?", taskId);
        jdbcTemplate.update("DELETE FROM audit_retrieval_record WHERE task_id = ?", taskId);
    }

    public void deleteUploadedBasisByTaskId(Long taskId) {
        jdbcTemplate.update("DELETE FROM audit_retrieval_reference WHERE task_id = ? AND resource_type = ?",
                taskId, "uploaded_basis_file");
        jdbcTemplate.update("""
                DELETE FROM audit_retrieval_record
                WHERE task_id = ? AND knowledge_scope LIKE ?
                """, taskId, "%uploaded_basis_files%");
    }

    public Long insertRecord(AuditTask task,
                             RetrievalRequest request,
                             String requestPayload,
                             String responseSummary,
                             int resultCount,
                             String status,
                             String errorCode,
                             String errorMsg,
                             long durationMs) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_retrieval_record (
                      task_id, task_no, workflow_code, source_chunk_id, query_text,
                      knowledge_scope, retrieval_config, kb_request_id, request_payload,
                      response_summary, result_count, retrieval_status, error_code,
                      error_msg, duration_ms, create_time
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, task.getTaskId());
            ps.setString(2, task.getTaskNo());
            ps.setString(3, task.getWorkflowCode());
            if (request.getSourceChunkId() == null) {
                ps.setObject(4, null);
            } else {
                ps.setLong(4, request.getSourceChunkId());
            }
            ps.setString(5, request.getQuery());
            ps.setString(6, jsonSupport.toJson(request.getKnowledgeScope()));
            ps.setString(7, jsonSupport.toJson(request.getRetrievalConfig()));
            ps.setString(8, value(request.getRequestId()));
            ps.setString(9, requestPayload);
            ps.setString(10, responseSummary);
            ps.setInt(11, resultCount);
            ps.setString(12, status);
            ps.setString(13, value(errorCode));
            ps.setString(14, errorMsg);
            ps.setLong(15, durationMs);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public void insertReferences(Long retrievalId, AuditTask task, Long sourceChunkId, List<RetrievalReference> references) {
        for (RetrievalReference reference : references) {
            insertReference(retrievalId, task, sourceChunkId, reference);
        }
    }

    public List<RetrievalReference> findReferencesByTaskId(Long taskId) {
        return jdbcTemplate.query("""
                SELECT * FROM audit_retrieval_reference
                WHERE task_id = ?
                ORDER BY source_chunk_id ASC, reference_id ASC
                """, this::mapReference, taskId);
    }

    public List<RetrievalReference> findReferencesByTaskIdAndSourceChunkId(Long taskId, Long sourceChunkId) {
        return jdbcTemplate.query("""
                SELECT * FROM audit_retrieval_reference
                WHERE task_id = ? AND source_chunk_id = ?
                ORDER BY reference_id ASC
                """, this::mapReference, taskId, sourceChunkId);
    }

    private void insertReference(Long retrievalId, AuditTask task, Long sourceChunkId, RetrievalReference reference) {
        jdbcTemplate.update("""
                INSERT INTO audit_retrieval_reference (
                  retrieval_id, task_id, source_chunk_id, kb_chunk_id, kb_document_id,
                  resource_id, resource_type, file_name, file_url, version_no, page_no,
                  section_title, section_path, rule_code, chunk_text_snapshot, score,
                  rank_score, effective_date, status, create_time
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """,
                retrievalId,
                task.getTaskId(),
                sourceChunkId,
                value(reference.getKbChunkId()),
                value(reference.getKbDocumentId()),
                reference.getResourceId(),
                value(reference.getResourceType()),
                value(reference.getFileName()),
                value(reference.getFileUrl()),
                value(reference.getVersionNo()),
                reference.getPageNo(),
                value(reference.getSectionTitle()),
                value(reference.getSectionPath()),
                value(reference.getRuleCode()),
                reference.getChunkTextSnapshot(),
                reference.getScore(),
                reference.getRankScore(),
                reference.getEffectiveDate() == null ? null : Date.valueOf(reference.getEffectiveDate()),
                value(reference.getStatus()));
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private RetrievalReference mapReference(ResultSet rs, int rowNum) throws SQLException {
        RetrievalReference reference = new RetrievalReference();
        reference.setReferenceId(rs.getLong("reference_id"));
        reference.setRetrievalId(rs.getLong("retrieval_id"));
        reference.setTaskId(rs.getLong("task_id"));
        long sourceChunkId = rs.getLong("source_chunk_id");
        reference.setSourceChunkId(rs.wasNull() ? null : sourceChunkId);
        reference.setKbChunkId(rs.getString("kb_chunk_id"));
        reference.setKbDocumentId(rs.getString("kb_document_id"));
        long resourceId = rs.getLong("resource_id");
        reference.setResourceId(rs.wasNull() ? null : resourceId);
        reference.setResourceType(rs.getString("resource_type"));
        reference.setFileName(rs.getString("file_name"));
        reference.setFileUrl(rs.getString("file_url"));
        reference.setVersionNo(rs.getString("version_no"));
        int pageNo = rs.getInt("page_no");
        reference.setPageNo(rs.wasNull() ? null : pageNo);
        reference.setSectionTitle(rs.getString("section_title"));
        reference.setSectionPath(rs.getString("section_path"));
        reference.setRuleCode(rs.getString("rule_code"));
        reference.setChunkTextSnapshot(rs.getString("chunk_text_snapshot"));
        reference.setScore(rs.getBigDecimal("score"));
        reference.setRankScore(rs.getBigDecimal("rank_score"));
        Date effectiveDate = rs.getDate("effective_date");
        reference.setEffectiveDate(effectiveDate == null ? null : effectiveDate.toLocalDate());
        reference.setStatus(rs.getString("status"));
        return reference;
    }
}
