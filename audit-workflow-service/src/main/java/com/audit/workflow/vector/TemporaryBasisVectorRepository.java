package com.audit.workflow.vector;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.domain.ContentChunk;
import com.audit.workflow.support.JsonSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TemporaryBasisVectorRepository {

    private static final String RESOURCE_TYPE = "workflow_basis";
    private static final String CATEGORY_CODE = "uploaded_basis";
    private static final String BUSINESS_TYPE = "workflow_task_basis";

    private final JdbcTemplate jdbcTemplate;
    private final JsonSupport jsonSupport;

    public TemporaryBasisVectorRepository(@Value("${audit.temp-vector.datasource.url:}") String url,
                                          @Value("${audit.temp-vector.datasource.username:}") String username,
                                          @Value("${audit.temp-vector.datasource.password:}") String password,
                                          @Value("${audit.temp-vector.datasource.driver-class-name:org.postgresql.Driver}") String driverClassName,
                                          JsonSupport jsonSupport) {
        this.jsonSupport = jsonSupport;
        if (url == null || url.isBlank()) {
            this.jdbcTemplate = null;
            return;
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(username == null ? "" : username);
        dataSource.setPassword(password == null ? "" : password);
        dataSource.setDriverClassName(driverClassName == null || driverClassName.isBlank()
                ? "org.postgresql.Driver"
                : driverClassName);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public String namespace(AuditTask task) {
        return "workflow_task_" + task.getTaskId();
    }

    public void resetNamespace(String namespace) {
        requireConfigured();
        jdbcTemplate.update("""
                delete from audit_vector_document
                where resource_type = ? and knowledge_base_code = ?
                """, RESOURCE_TYPE, namespace);
    }

    public Long insertDocument(AuditTask task,
                               String namespace,
                               BasisDocument document,
                               String embeddingModel,
                               int embeddingDimensions) {
        requireConfigured();
        Long documentId = jdbcTemplate.queryForObject(
                "select nextval(pg_get_serial_sequence('audit_vector_document', 'document_id'))",
                Long.class);
        if (documentId == null) {
            throw new IllegalStateException("无法获取临时向量 document_id");
        }
        jdbcTemplate.update("""
                insert into audit_vector_document (
                  document_id, resource_id, resource_type, folder_id, file_name, file_url, file_hash,
                  current_version_no, parse_status, vector_status, chunk_count, embedding_model,
                  embedding_dimensions, knowledge_base_code, category_code, business_type, status,
                  owner_dept_id, source_system, last_index_time, error_msg, create_time, update_time
                ) values (?, ?, ?, 0, ?, ?, ?, 'uploaded', 'success', 'success', ?, ?, ?, ?, ?, ?, 'effective',
                  '', 'audit_workflow', current_timestamp, null, current_timestamp, current_timestamp)
                """,
                documentId,
                documentId,
                RESOURCE_TYPE,
                abbreviate(document.fileName(), 255),
                abbreviate(document.fileUrl(), 500),
                abbreviate(document.fileHash(), 64),
                document.chunks().size(),
                embeddingModel,
                embeddingDimensions,
                namespace,
                CATEGORY_CODE,
                BUSINESS_TYPE);
        return documentId;
    }

    public void insertChunks(List<BasisVectorChunk> chunks) {
        requireConfigured();
        if (chunks == null || chunks.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("""
                insert into audit_vector_chunk (
                  document_id, resource_id, folder_id, chunk_no, chunk_text, page_no, section_title, token_count,
                  chunk_uid, rule_code, section_path, paragraph_no, content_hash, metadata, embedding
                ) values (?, ?, 0, ?, ?, ?, ?, ?, ?, '', ?, null, ?, ?::jsonb, ?::vector)
                """,
                chunks,
                100,
                (ps, chunk) -> {
                    ContentChunk content = chunk.chunk();
                    ps.setLong(1, chunk.documentId());
                    ps.setLong(2, chunk.documentId());
                    ps.setInt(3, content.getChunkNo() == null ? 0 : content.getChunkNo());
                    ps.setString(4, value(content.getChunkText()));
                    if (content.getPageNo() == null) {
                        ps.setNull(5, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(5, content.getPageNo());
                    }
                    ps.setString(6, value(content.getSectionTitle()));
                    ps.setInt(7, content.getTokenCount() == null ? 0 : content.getTokenCount());
                    ps.setString(8, chunk.basisChunkId());
                    ps.setString(9, value(content.getSectionPath()));
                    ps.setString(10, value(content.getContentHash()));
                    ps.setString(11, jsonSupport.toJson(chunk.metadata()));
                    ps.setString(12, toVectorLiteral(chunk.embedding()));
                });
    }

    public List<BasisVectorHit> search(String namespace, float[] queryEmbedding, int topK, int maxChunkChars) {
        requireConfigured();
        String vectorLiteral = toVectorLiteral(queryEmbedding);
        return jdbcTemplate.query("""
                select c.chunk_id, c.document_id, c.resource_id, c.chunk_no, c.chunk_text, c.page_no,
                  c.section_title, c.section_path, c.chunk_uid, c.content_hash, c.metadata::text as metadata,
                  d.file_name, d.file_url, d.current_version_no, c.embedding <=> ?::vector as distance
                from audit_vector_chunk c
                join audit_vector_document d on d.document_id = c.document_id
                where d.resource_type = ?
                  and d.knowledge_base_code = ?
                  and d.vector_status = 'success'
                order by c.embedding <=> ?::vector
                limit ?
                """,
                this::mapHit,
                vectorLiteral,
                RESOURCE_TYPE,
                namespace,
                vectorLiteral,
                Math.max(1, topK)).stream()
                .map(hit -> hit.withChunkText(abbreviate(hit.chunkText(), maxChunkChars)))
                .toList();
    }

    private BasisVectorHit mapHit(ResultSet rs, int rowNum) throws SQLException {
        BigDecimal distance = rs.getBigDecimal("distance");
        BigDecimal score = toScore(distance);
        return new BasisVectorHit(
                rs.getLong("chunk_id"),
                rs.getLong("document_id"),
                rs.getLong("resource_id"),
                rs.getInt("chunk_no"),
                rs.getString("chunk_text"),
                nullableInt(rs, "page_no"),
                rs.getString("section_title"),
                rs.getString("section_path"),
                rs.getString("chunk_uid"),
                rs.getString("file_name"),
                rs.getString("file_url"),
                rs.getString("current_version_no"),
                rs.getString("metadata"),
                distance,
                score);
    }

    private Integer nullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private BigDecimal toScore(BigDecimal distance) {
        if (distance == null) {
            return null;
        }
        double value = 1D - distance.doubleValue();
        value = Math.max(0D, Math.min(1D, value));
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
    }

    private String toVectorLiteral(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Embedding 向量为空");
        }
        StringBuilder builder = new StringBuilder(vector.length * 10);
        builder.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(Float.toString(vector[i]));
        }
        builder.append(']');
        return builder.toString();
    }

    private void requireConfigured() {
        if (jdbcTemplate == null) {
            throw new IllegalStateException("临时向量库 datasource url 未配置");
        }
    }

    private String abbreviate(String value, int maxLength) {
        String text = value(value);
        if (maxLength <= 0 || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    public record BasisDocument(String fileId, String fileName, String fileUrl, String fileHash, List<ContentChunk> chunks) {
    }

    public record BasisVectorChunk(Long documentId,
                                   String basisChunkId,
                                   ContentChunk chunk,
                                   float[] embedding,
                                   Map<String, Object> metadata) {
    }

    public record BasisVectorHit(Long chunkId,
                                 Long documentId,
                                 Long resourceId,
                                 Integer chunkNo,
                                 String chunkText,
                                 Integer pageNo,
                                 String sectionTitle,
                                 String sectionPath,
                                 String chunkUid,
                                 String fileName,
                                 String fileUrl,
                                 String versionNo,
                                 String metadata,
                                 BigDecimal distance,
                                 BigDecimal score) {
        public BasisVectorHit withChunkText(String value) {
            return new BasisVectorHit(chunkId, documentId, resourceId, chunkNo, value, pageNo, sectionTitle, sectionPath,
                    chunkUid, fileName, fileUrl, versionNo, metadata, distance, score);
        }
    }

    public static Map<String, Object> metadata(Long taskId, String taskNo, String fileId, String fileName, String fileUrl) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "uploaded_basis_files");
        metadata.put("task_id", taskId);
        metadata.put("task_no", taskNo);
        metadata.put("basis_file_id", fileId);
        metadata.put("basis_file_name", fileName);
        metadata.put("basis_file_url", fileUrl);
        return metadata;
    }
}
