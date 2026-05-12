package com.audit.workflow.repository;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.domain.ContentChunk;
import com.audit.workflow.support.JsonSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class AuditTaskContentChunkRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonSupport jsonSupport;

    public AuditTaskContentChunkRepository(JdbcTemplate jdbcTemplate, JsonSupport jsonSupport) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonSupport = jsonSupport;
    }

    public void deleteByTaskId(Long taskId) {
        jdbcTemplate.update("DELETE FROM audit_task_content_chunk WHERE task_id = ?", taskId);
    }

    public List<ContentChunk> findByTaskId(Long taskId) {
        return jdbcTemplate.query("""
                SELECT * FROM audit_task_content_chunk
                WHERE task_id = ?
                ORDER BY chunk_no ASC
                """, this::mapChunk, taskId);
    }

    public List<Long> insertChunks(AuditTask task, List<ContentChunk> chunks) {
        List<Long> ids = new ArrayList<>();
        for (ContentChunk chunk : chunks) {
            ids.add(insertChunk(task, chunk));
        }
        return ids;
    }

    private Long insertChunk(AuditTask task, ContentChunk chunk) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO audit_task_content_chunk (
                      task_id, task_no, chunk_no, chunk_text, page_no, section_title,
                      section_path, token_count, char_count, content_hash, metadata, create_time
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, task.getTaskId());
            ps.setString(2, task.getTaskNo());
            ps.setInt(3, chunk.getChunkNo());
            ps.setString(4, chunk.getChunkText());
            if (chunk.getPageNo() == null) {
                ps.setObject(5, null);
            } else {
                ps.setInt(5, chunk.getPageNo());
            }
            ps.setString(6, value(chunk.getSectionTitle()));
            ps.setString(7, value(chunk.getSectionPath()));
            ps.setInt(8, chunk.getTokenCount() == null ? 0 : chunk.getTokenCount());
            ps.setInt(9, chunk.getCharCount() == null ? 0 : chunk.getCharCount());
            ps.setString(10, value(chunk.getContentHash()));
            ps.setString(11, jsonSupport.toJson(chunk.getMetadata()));
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private ContentChunk mapChunk(ResultSet rs, int rowNum) throws SQLException {
        ContentChunk chunk = new ContentChunk();
        chunk.setSourceChunkId(rs.getLong("source_chunk_id"));
        chunk.setChunkNo(rs.getInt("chunk_no"));
        chunk.setChunkText(rs.getString("chunk_text"));
        int pageNo = rs.getInt("page_no");
        chunk.setPageNo(rs.wasNull() ? null : pageNo);
        chunk.setSectionTitle(rs.getString("section_title"));
        chunk.setSectionPath(rs.getString("section_path"));
        chunk.setTokenCount(rs.getInt("token_count"));
        chunk.setCharCount(rs.getInt("char_count"));
        chunk.setContentHash(rs.getString("content_hash"));
        chunk.setMetadata(jsonSupport.toMap(rs.getString("metadata")));
        return chunk;
    }
}
