package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.system.domain.audit.vector.AuditVectorChunk;
import com.ruoyi.system.domain.audit.vector.AuditVectorDocument;
import com.ruoyi.system.domain.audit.vector.AuditVectorSearchHit;
import com.ruoyi.system.service.audit.vector.VectorStoreRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
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
                        "embedding_dimensions, last_index_time, error_msg, knowledge_base_code, category_code, " +
                        "business_type, status, effective_date::text as effective_date, " +
                        "expire_date::text as expire_date, owner_dept_id, source_system " +
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
                        "knowledge_base_code, category_code, business_type, status, effective_date, expire_date, " +
                        "owner_dept_id, source_system, " +
                        "create_time, update_time) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::date, ?::date, ?, ?, " +
                        "current_timestamp, current_timestamp) " +
                        "on conflict (resource_type, resource_id) do update set " +
                        "folder_id = excluded.folder_id, file_name = excluded.file_name, file_url = excluded.file_url, " +
                        "file_hash = excluded.file_hash, current_version_no = excluded.current_version_no, " +
                        "parse_status = excluded.parse_status, vector_status = excluded.vector_status, " +
                        "chunk_count = excluded.chunk_count, embedding_model = excluded.embedding_model, " +
                        "embedding_dimensions = excluded.embedding_dimensions, error_msg = excluded.error_msg, " +
                        "knowledge_base_code = excluded.knowledge_base_code, category_code = excluded.category_code, " +
                        "business_type = excluded.business_type, status = excluded.status, " +
                        "effective_date = excluded.effective_date, expire_date = excluded.expire_date, " +
                        "owner_dept_id = excluded.owner_dept_id, source_system = excluded.source_system, " +
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
                document.getErrorMsg(),
                defaultString(document.getKnowledgeBaseCode(), "default"),
                defaultString(document.getCategoryCode(), ""),
                defaultString(document.getBusinessType(), ""),
                defaultString(document.getStatus(), "effective"),
                document.getEffectiveDate(),
                document.getExpireDate(),
                defaultString(document.getOwnerDeptId(), ""),
                defaultString(document.getSourceSystem(), "audit"));
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
                        "(document_id, resource_id, folder_id, chunk_no, chunk_text, page_no, section_title, token_count, " +
                        "chunk_uid, rule_code, section_path, paragraph_no, content_hash, metadata, embedding) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::vector)",
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
                    ps.setString(9, defaultString(chunk.getChunkUid(), ""));
                    ps.setString(10, defaultString(chunk.getRuleCode(), ""));
                    ps.setString(11, defaultString(chunk.getSectionPath(), ""));
                    if (chunk.getParagraphNo() == null)
                    {
                        ps.setNull(12, java.sql.Types.INTEGER);
                    }
                    else
                    {
                        ps.setInt(12, chunk.getParagraphNo());
                    }
                    ps.setString(13, defaultString(chunk.getContentHash(), ""));
                    ps.setString(14, defaultString(chunk.getMetadata(), "{}"));
                    ps.setString(15, toVectorLiteral(chunk.getEmbedding()));
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

    @Override
    public List<AuditVectorSearchHit> searchChunks(float[] queryEmbedding, Collection<Long> resourceIds, int topK,
            BigDecimal minScore, boolean includeChunkText, int maxChunkChars, String[] knowledgeBaseCodes,
            String[] categoryCodes, String businessType, Boolean effectiveOnly, String asOfDate)
    {
        String vectorLiteral = toVectorLiteral(queryEmbedding);
        String chunkTextSql = includeChunkText ? "substring(c.chunk_text from 1 for ?) as chunk_text" : "'' as chunk_text";
        boolean hasKnowledgeBaseFilter = hasTextValues(knowledgeBaseCodes);
        boolean hasCategoryFilter = hasTextValues(categoryCodes);
        boolean hasBusinessTypeFilter = businessType != null && !businessType.trim().isEmpty();
        boolean effectiveOnlyFilter = Boolean.TRUE.equals(effectiveOnly);
        String safeAsOfDate = effectiveOnlyFilter ? normalizeAsOfDate(asOfDate) : null;
        StringBuilder sql = new StringBuilder();
        sql.append("select c.chunk_id, c.document_id, c.resource_id, d.resource_type, c.folder_id, c.chunk_no, ")
                .append("c.page_no, c.section_title, c.chunk_uid, c.rule_code, c.section_path, c.paragraph_no, ")
                .append("c.content_hash, c.metadata::text as metadata, ")
                .append(chunkTextSql)
                .append(", d.file_name, d.file_url, d.file_hash, d.current_version_no, ")
                .append("d.knowledge_base_code, d.category_code, d.business_type, d.status, ")
                .append("d.effective_date::text as effective_date, d.expire_date::text as expire_date, d.owner_dept_id, ")
                .append("c.embedding <=> ?::vector as distance ")
                .append("from audit_vector_chunk c ")
                .append("join audit_vector_document d on d.document_id = c.document_id ")
                .append("where d.resource_type = ? ")
                .append("and d.vector_status = ? ")
                .append("and c.resource_id = any (?) ");
        if (hasKnowledgeBaseFilter)
        {
            sql.append("and d.knowledge_base_code = any (?) ");
        }
        if (hasCategoryFilter)
        {
            sql.append("and d.category_code = any (?) ");
        }
        if (hasBusinessTypeFilter)
        {
            sql.append("and d.business_type = ? ");
        }
        if (effectiveOnlyFilter)
        {
            sql.append("and d.status = 'effective' ")
                    .append("and (d.effective_date is null or d.effective_date <= ?::date) ")
                    .append("and (d.expire_date is null or d.expire_date >= ?::date) ");
        }
        if (minScore != null)
        {
            sql.append("and (c.embedding <=> ?::vector) <= ? ");
        }
        sql.append("order by c.embedding <=> ?::vector limit ?");

        return jdbcTemplate.query(sql.toString(), ps -> {
            int index = 1;
            if (includeChunkText)
            {
                ps.setInt(index++, Math.max(0, maxChunkChars));
            }
            ps.setString(index++, vectorLiteral);
            ps.setString(index++, AuditVectorDocument.RESOURCE_TYPE_COMMON);
            ps.setString(index++, AuditVectorDocument.STATUS_SUCCESS);
            ps.setArray(index++, ps.getConnection().createArrayOf("bigint", resourceIds.toArray(new Long[0])));
            if (hasKnowledgeBaseFilter)
            {
                ps.setArray(index++, ps.getConnection().createArrayOf("varchar", trimTextValues(knowledgeBaseCodes)));
            }
            if (hasCategoryFilter)
            {
                ps.setArray(index++, ps.getConnection().createArrayOf("varchar", trimTextValues(categoryCodes)));
            }
            if (hasBusinessTypeFilter)
            {
                ps.setString(index++, businessType.trim());
            }
            if (effectiveOnlyFilter)
            {
                ps.setString(index++, safeAsOfDate);
                ps.setString(index++, safeAsOfDate);
            }
            if (minScore != null)
            {
                ps.setString(index++, vectorLiteral);
                ps.setBigDecimal(index++, BigDecimal.ONE.subtract(minScore));
            }
            ps.setString(index++, vectorLiteral);
            ps.setInt(index, topK);
        }, this::mapSearchHit);
    }

    @Override
    public List<AuditVectorSearchHit> searchKeywordChunks(List<String> keywords, Collection<Long> resourceIds, int topK,
            boolean includeChunkText, int maxChunkChars, String[] knowledgeBaseCodes, String[] categoryCodes,
            String businessType, Boolean effectiveOnly, String asOfDate)
    {
        if (CollectionUtils.isEmpty(keywords) || CollectionUtils.isEmpty(resourceIds))
        {
            return new ArrayList<>();
        }
        String chunkTextSql = includeChunkText ? "substring(c.chunk_text from 1 for ?) as chunk_text" : "'' as chunk_text";
        boolean hasKnowledgeBaseFilter = hasTextValues(knowledgeBaseCodes);
        boolean hasCategoryFilter = hasTextValues(categoryCodes);
        boolean hasBusinessTypeFilter = businessType != null && !businessType.trim().isEmpty();
        boolean effectiveOnlyFilter = Boolean.TRUE.equals(effectiveOnly);
        String safeAsOfDate = effectiveOnlyFilter ? normalizeAsOfDate(asOfDate) : null;
        StringBuilder sql = new StringBuilder();
        sql.append("select c.chunk_id, c.document_id, c.resource_id, d.resource_type, c.folder_id, c.chunk_no, ")
                .append("c.page_no, c.section_title, c.chunk_uid, c.rule_code, c.section_path, c.paragraph_no, ")
                .append("c.content_hash, c.metadata::text as metadata, ")
                .append(chunkTextSql)
                .append(", d.file_name, d.file_url, d.file_hash, d.current_version_no, ")
                .append("d.knowledge_base_code, d.category_code, d.business_type, d.status, ")
                .append("d.effective_date::text as effective_date, d.expire_date::text as expire_date, d.owner_dept_id, ")
                .append("null::numeric as distance ")
                .append("from audit_vector_chunk c ")
                .append("join audit_vector_document d on d.document_id = c.document_id ")
                .append("where d.resource_type = ? ")
                .append("and d.vector_status = ? ")
                .append("and c.resource_id = any (?) ");
        if (hasKnowledgeBaseFilter)
        {
            sql.append("and d.knowledge_base_code = any (?) ");
        }
        if (hasCategoryFilter)
        {
            sql.append("and d.category_code = any (?) ");
        }
        if (hasBusinessTypeFilter)
        {
            sql.append("and d.business_type = ? ");
        }
        if (effectiveOnlyFilter)
        {
            sql.append("and d.status = 'effective' ")
                    .append("and (d.effective_date is null or d.effective_date <= ?::date) ")
                    .append("and (d.expire_date is null or d.expire_date >= ?::date) ");
        }
        sql.append("and (");
        for (int i = 0; i < keywords.size(); i++)
        {
            if (i > 0)
            {
                sql.append(" or ");
            }
            sql.append("c.chunk_text ilike ? or c.rule_code ilike ? or c.section_path ilike ? or d.file_name ilike ?");
        }
        sql.append(") order by c.chunk_id desc limit ?");

        return jdbcTemplate.query(sql.toString(), ps -> {
            int index = 1;
            if (includeChunkText)
            {
                ps.setInt(index++, Math.max(0, maxChunkChars));
            }
            ps.setString(index++, AuditVectorDocument.RESOURCE_TYPE_COMMON);
            ps.setString(index++, AuditVectorDocument.STATUS_SUCCESS);
            ps.setArray(index++, ps.getConnection().createArrayOf("bigint", resourceIds.toArray(new Long[0])));
            if (hasKnowledgeBaseFilter)
            {
                ps.setArray(index++, ps.getConnection().createArrayOf("varchar", trimTextValues(knowledgeBaseCodes)));
            }
            if (hasCategoryFilter)
            {
                ps.setArray(index++, ps.getConnection().createArrayOf("varchar", trimTextValues(categoryCodes)));
            }
            if (hasBusinessTypeFilter)
            {
                ps.setString(index++, businessType.trim());
            }
            if (effectiveOnlyFilter)
            {
                ps.setString(index++, safeAsOfDate);
                ps.setString(index++, safeAsOfDate);
            }
            for (String keyword : keywords)
            {
                String pattern = "%" + keyword + "%";
                ps.setString(index++, pattern);
                ps.setString(index++, pattern);
                ps.setString(index++, pattern);
                ps.setString(index++, pattern);
            }
            ps.setInt(index, topK);
        }, this::mapSearchHit);
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
        document.setKnowledgeBaseCode(rs.getString("knowledge_base_code"));
        document.setCategoryCode(rs.getString("category_code"));
        document.setBusinessType(rs.getString("business_type"));
        document.setStatus(rs.getString("status"));
        document.setEffectiveDate(rs.getString("effective_date"));
        document.setExpireDate(rs.getString("expire_date"));
        document.setOwnerDeptId(rs.getString("owner_dept_id"));
        document.setSourceSystem(rs.getString("source_system"));
        return document;
    }

    private AuditVectorSearchHit mapSearchHit(ResultSet rs, int rowNum) throws SQLException
    {
        AuditVectorSearchHit hit = new AuditVectorSearchHit();
        hit.setChunkId(rs.getLong("chunk_id"));
        hit.setDocumentId(rs.getLong("document_id"));
        hit.setResourceId(rs.getLong("resource_id"));
        hit.setResourceType(rs.getString("resource_type"));
        hit.setFolderId(rs.getLong("folder_id"));
        hit.setChunkNo(rs.getInt("chunk_no"));
        int pageNo = rs.getInt("page_no");
        hit.setPageNo(rs.wasNull() ? null : pageNo);
        hit.setSectionTitle(rs.getString("section_title"));
        hit.setChunkUid(rs.getString("chunk_uid"));
        hit.setRuleCode(rs.getString("rule_code"));
        hit.setSectionPath(rs.getString("section_path"));
        int paragraphNo = rs.getInt("paragraph_no");
        hit.setParagraphNo(rs.wasNull() ? null : paragraphNo);
        hit.setContentHash(rs.getString("content_hash"));
        hit.setMetadata(rs.getString("metadata"));
        hit.setChunkText(rs.getString("chunk_text"));
        hit.setFileName(rs.getString("file_name"));
        hit.setFileUrl(rs.getString("file_url"));
        hit.setFileHash(rs.getString("file_hash"));
        hit.setVersionNo(rs.getString("current_version_no"));
        hit.setKnowledgeBaseCode(defaultString(rs.getString("knowledge_base_code"), "default"));
        hit.setCategoryCode(defaultString(rs.getString("category_code"), ""));
        hit.setBusinessType(defaultString(rs.getString("business_type"), ""));
        hit.setStatus(defaultString(rs.getString("status"), "effective"));
        hit.setEffectiveDate(rs.getString("effective_date"));
        hit.setExpireDate(rs.getString("expire_date"));
        hit.setOwnerDeptId(defaultString(rs.getString("owner_dept_id"), ""));
        BigDecimal distance = rs.getBigDecimal("distance");
        hit.setDistance(distance);
        hit.setScore(toScore(distance));
        return hit;
    }

    private BigDecimal toScore(BigDecimal distance)
    {
        if (distance == null)
        {
            return null;
        }
        double value = 1D - distance.doubleValue();
        value = Math.max(0D, Math.min(1D, value));
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP);
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

    private String defaultString(String value, String defaultValue)
    {
        return value == null ? defaultValue : value;
    }

    private boolean hasTextValues(String[] values)
    {
        if (values == null || values.length == 0)
        {
            return false;
        }
        for (String value : values)
        {
            if (value != null && !value.trim().isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    private String[] trimTextValues(String[] values)
    {
        return java.util.Arrays.stream(values)
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .toArray(String[]::new);
    }

    private String normalizeAsOfDate(String asOfDate)
    {
        if (asOfDate == null || asOfDate.trim().isEmpty())
        {
            return LocalDate.now().toString();
        }
        try
        {
            return LocalDate.parse(asOfDate.trim()).toString();
        }
        catch (DateTimeParseException e)
        {
            throw new IllegalArgumentException("asOfDate 日期格式必须为 yyyy-MM-dd");
        }
    }
}
