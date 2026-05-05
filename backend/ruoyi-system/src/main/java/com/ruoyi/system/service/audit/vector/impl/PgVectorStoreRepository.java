package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.system.domain.audit.vector.AuditVectorChunk;
import com.ruoyi.system.domain.audit.vector.AuditVectorDocument;
import com.ruoyi.system.service.audit.vector.VectorStoreRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
@ConditionalOnProperty(prefix = "vector", name = "enabled", havingValue = "true")
public class PgVectorStoreRepository implements VectorStoreRepository
{
    private final JdbcTemplate jdbcTemplate;

    public PgVectorStoreRepository(@Qualifier("vectorJdbcTemplate") JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuditVectorDocument findDocument(String resourceType, Long resourceId)
    {
        List<AuditVectorDocument> documents = jdbcTemplate.query(
                "select document_id, resource_id, resource_type, folder_id, file_name, file_url, file_hash, " +
                        "current_version_no, parse_status, vector_status, chunk_count, embedding_model, " +
                        "embedding_dimensions, last_index_time, error_msg " +
                        "from audit_vector_document where resource_type = ? and resource_id = ?",
                this::mapDocument,
                resourceType,
                resourceId);
        return documents.isEmpty() ? null : documents.get(0);
    }

    @Override
    public Long upsertDocument(AuditVectorDocument document)
    {
        return jdbcTemplate.queryForObject(
                "insert into audit_vector_document " +
                        "(resource_id, resource_type, folder_id, file_name, file_url, file_hash, current_version_no, " +
                        "parse_status, vector_status, chunk_count, embedding_model, embedding_dimensions, error_msg, " +
                        "create_time, update_time) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp, current_timestamp) " +
                        "on conflict (resource_type, resource_id) do update set " +
                        "folder_id = excluded.folder_id, file_name = excluded.file_name, file_url = excluded.file_url, " +
                        "file_hash = excluded.file_hash, current_version_no = excluded.current_version_no, " +
                        "parse_status = excluded.parse_status, vector_status = excluded.vector_status, " +
                        "chunk_count = excluded.chunk_count, embedding_model = excluded.embedding_model, " +
                        "embedding_dimensions = excluded.embedding_dimensions, error_msg = excluded.error_msg, " +
                        "update_time = current_timestamp " +
                        "returning document_id",
                Long.class,
                document.getResourceId(),
                document.getResourceType(),
                valueOrZero(document.getFolderId()),
                document.getFileName(),
                document.getFileUrl(),
                document.getFileHash(),
                document.getCurrentVersionNo(),
                document.getParseStatus(),
                document.getVectorStatus(),
                valueOrZero(document.getChunkCount()),
                document.getEmbeddingModel(),
                document.getEmbeddingDimensions(),
                document.getErrorMsg());
    }

    @Override
    public int deleteChunksByDocumentId(Long documentId)
    {
        return jdbcTemplate.update("delete from audit_vector_chunk where document_id = ?", documentId);
    }

    @Override
    public void insertChunks(List<AuditVectorChunk> chunks)
    {
        if (CollectionUtils.isEmpty(chunks))
        {
            return;
        }
        jdbcTemplate.batchUpdate(
                "insert into audit_vector_chunk " +
                        "(document_id, resource_id, folder_id, chunk_no, chunk_text, page_no, section_title, token_count, embedding) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?::vector)",
                chunks,
                100,
                (ps, chunk) -> {
                    ps.setLong(1, chunk.getDocumentId());
                    ps.setLong(2, chunk.getResourceId());
                    ps.setLong(3, valueOrZero(chunk.getFolderId()));
                    ps.setInt(4, chunk.getChunkNo());
                    ps.setString(5, chunk.getChunkText());
                    if (chunk.getPageNo() == null)
                    {
                        ps.setNull(6, java.sql.Types.INTEGER);
                    }
                    else
                    {
                        ps.setInt(6, chunk.getPageNo());
                    }
                    ps.setString(7, chunk.getSectionTitle() == null ? "" : chunk.getSectionTitle());
                    ps.setInt(8, valueOrZero(chunk.getTokenCount()));
                    ps.setString(9, toVectorLiteral(chunk.getEmbedding()));
                });
    }

    @Override
    public int markDocumentIndexed(Long documentId, int chunkCount)
    {
        return jdbcTemplate.update(
                "update audit_vector_document set parse_status = 'success', vector_status = 'success', " +
                        "chunk_count = ?, last_index_time = current_timestamp, error_msg = null, update_time = current_timestamp " +
                        "where document_id = ?",
                chunkCount,
                documentId);
    }

    @Override
    public int markDocumentFailed(Long documentId, String errorMsg)
    {
        return jdbcTemplate.update(
                "update audit_vector_document set vector_status = 'failed', error_msg = ?, update_time = current_timestamp " +
                        "where document_id = ?",
                errorMsg,
                documentId);
    }

    @Override
    public void updateFolderId(String resourceType, Long resourceId, Long folderId)
    {
        long safeFolderId = valueOrZero(folderId);
        jdbcTemplate.update(
                "update audit_vector_document set folder_id = ?, update_time = current_timestamp " +
                        "where resource_type = ? and resource_id = ?",
                safeFolderId,
                resourceType,
                resourceId);
        jdbcTemplate.update(
                "update audit_vector_chunk set folder_id = ? where resource_id = ?",
                safeFolderId,
                resourceId);
    }

    @Override
    public void updateDocumentMetadata(String resourceType, Long resourceId, String fileName, String fileUrl, String versionNo)
    {
        jdbcTemplate.update(
                "update audit_vector_document set file_name = coalesce(?, file_name), file_url = coalesce(?, file_url), " +
                        "current_version_no = coalesce(?, current_version_no), update_time = current_timestamp " +
                        "where resource_type = ? and resource_id = ?",
                fileName,
                fileUrl,
                versionNo,
                resourceType,
                resourceId);
    }

    @Override
    public void deleteByResourceIds(String resourceType, List<Long> resourceIds)
    {
        if (CollectionUtils.isEmpty(resourceIds))
        {
            return;
        }
        jdbcTemplate.update(
                "delete from audit_vector_document where resource_type = ? and resource_id = any (?)",
                ps -> {
                    ps.setString(1, resourceType);
                    ps.setArray(2, ps.getConnection().createArrayOf("bigint", resourceIds.toArray(new Long[0])));
                });
    }

    @Override
    public boolean existsDocument(String resourceType, Long resourceId)
    {
        Integer count = jdbcTemplate.queryForObject(
                "select count(1) from audit_vector_document where resource_type = ? and resource_id = ?",
                Integer.class,
                resourceType,
                resourceId);
        return count != null && count > 0;
    }

    private AuditVectorDocument mapDocument(ResultSet rs, int rowNum) throws SQLException
    {
        AuditVectorDocument document = new AuditVectorDocument();
        document.setDocumentId(rs.getLong("document_id"));
        document.setResourceId(rs.getLong("resource_id"));
        document.setResourceType(rs.getString("resource_type"));
        document.setFolderId(rs.getLong("folder_id"));
        document.setFileName(rs.getString("file_name"));
        document.setFileUrl(rs.getString("file_url"));
        document.setFileHash(rs.getString("file_hash"));
        document.setCurrentVersionNo(rs.getString("current_version_no"));
        document.setParseStatus(rs.getString("parse_status"));
        document.setVectorStatus(rs.getString("vector_status"));
        document.setChunkCount(rs.getInt("chunk_count"));
        document.setEmbeddingModel(rs.getString("embedding_model"));
        document.setEmbeddingDimensions(rs.getInt("embedding_dimensions"));
        document.setIndexedAt(rs.getTimestamp("last_index_time"));
        document.setErrorMsg(rs.getString("error_msg"));
        return document;
    }

    private String toVectorLiteral(float[] vector)
    {
        if (vector == null || vector.length == 0)
        {
            throw new IllegalArgumentException("Embedding 向量为空");
        }
        StringBuilder builder = new StringBuilder(vector.length * 10);
        builder.append('[');
        for (int i = 0; i < vector.length; i++)
        {
            if (i > 0)
            {
                builder.append(',');
            }
            builder.append(Float.toString(vector[i]));
        }
        builder.append(']');
        return builder.toString();
    }

    private int valueOrZero(Integer value)
    {
        return value == null ? 0 : value;
    }

    private long valueOrZero(Long value)
    {
        return value == null ? 0L : value;
    }
}
