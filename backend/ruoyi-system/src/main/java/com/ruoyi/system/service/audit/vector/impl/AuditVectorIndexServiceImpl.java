package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.AuditCommonResource;
import com.ruoyi.system.domain.audit.vector.AuditVectorChunk;
import com.ruoyi.system.domain.audit.vector.AuditVectorDocument;
import com.ruoyi.system.domain.audit.vector.AuditVectorIndexResult;
import com.ruoyi.system.domain.audit.vector.AuditVectorTask;
import com.ruoyi.system.domain.audit.vector.DocumentChunk;
import com.ruoyi.system.domain.audit.vector.DocumentParseResult;
import com.ruoyi.system.mapper.audit.AuditLibraryMapper;
import com.ruoyi.system.service.audit.vector.AuditVectorIndexService;
import com.ruoyi.system.service.audit.vector.DocumentChunkService;
import com.ruoyi.system.service.audit.vector.DocumentParserService;
import com.ruoyi.system.service.audit.vector.EmbeddingClient;
import com.ruoyi.system.service.audit.vector.VectorStoreRepository;
import com.ruoyi.system.service.audit.vector.support.AuditFileResolver;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "vector", name = "enabled", havingValue = "true")
public class AuditVectorIndexServiceImpl implements AuditVectorIndexService
{
    private static final String RESOURCE_STATUS_PENDING = "pending";
    private static final String RESOURCE_STATUS_PARSING = "parsing";
    private static final String RESOURCE_STATUS_EMBEDDING = "embedding";
    private static final String RESOURCE_STATUS_STORED = "stored";
    private static final String RESOURCE_STATUS_FAILED = "failed";
    private static final String RESOURCE_STATUS_TEXT_EMPTY = "text_empty";
    private static final Pattern RULE_CODE_PATTERN = Pattern.compile("(?i)([A-Z]{1,8}[-_]?\\d+(?:\\.\\d+)*|第\\s*\\d+(?:\\.\\d+)*\\s*条)");

    private final AuditLibraryMapper auditLibraryMapper;
    private final DocumentParserService documentParserService;
    private final DocumentChunkService documentChunkService;
    private final EmbeddingClient embeddingClient;
    private final VectorStoreRepository vectorStoreRepository;
    private final AuditFileResolver auditFileResolver;
    private final VectorProperties properties;

    public AuditVectorIndexServiceImpl(AuditLibraryMapper auditLibraryMapper,
            DocumentParserService documentParserService,
            DocumentChunkService documentChunkService,
            EmbeddingClient embeddingClient,
            VectorStoreRepository vectorStoreRepository,
            AuditFileResolver auditFileResolver,
            VectorProperties properties)
    {
        this.auditLibraryMapper = auditLibraryMapper;
        this.documentParserService = documentParserService;
        this.documentChunkService = documentChunkService;
        this.embeddingClient = embeddingClient;
        this.vectorStoreRepository = vectorStoreRepository;
        this.auditFileResolver = auditFileResolver;
        this.properties = properties;
    }

    @Override
    public AuditVectorIndexResult index(AuditVectorTask task)
    {
        String taskType = task == null ? AuditVectorTask.TYPE_INDEX : task.getTaskType();
        String operator = task == null ? "system" : StringUtils.defaultIfBlank(task.getUpdateBy(), task.getCreateBy());
        Long resourceId = task == null ? null : task.getResourceId();
        return indexResource(resourceId, taskType, StringUtils.defaultIfBlank(operator, "system"));
    }

    @Override
    public AuditVectorIndexResult indexResource(Long resourceId, String taskType, String operator)
    {
        if (resourceId == null)
        {
            throw new IllegalArgumentException("resourceId 不能为空");
        }
        AuditCommonResource resource = auditLibraryMapper.selectAuditCommonResourceById(resourceId);
        if (resource == null)
        {
            throw new IllegalArgumentException("审核文件不存在：" + resourceId);
        }

        Long documentId = null;
        try
        {
            updateResourceStatus(resourceId, RESOURCE_STATUS_PARSING, "正在解析文件", operator);
            String fileHash = calculateSha256(resource.getFileUrl());
            AuditVectorDocument existing = vectorStoreRepository.findDocument(AuditVectorDocument.RESOURCE_TYPE_COMMON, resourceId);
            boolean force = AuditVectorTask.TYPE_REINDEX.equals(taskType);
            if (!force && existing != null && fileHash.equals(existing.getFileHash())
                    && AuditVectorDocument.STATUS_SUCCESS.equals(existing.getVectorStatus())
                    && existing.getChunkCount() != null && existing.getChunkCount() > 0)
            {
                updateResourceStatus(resourceId, RESOURCE_STATUS_STORED, "文件未变化，跳过向量化", operator);
                return AuditVectorIndexResult.of(AuditVectorTask.STATUS_SKIPPED, existing.getDocumentId(),
                        existing.getChunkCount(), "文件未变化，跳过向量化");
            }

            AuditVectorDocument document = buildDocument(resource, fileHash);
            documentId = vectorStoreRepository.upsertDocument(document);
            document.setDocumentId(documentId);

            DocumentParseResult parseResult = documentParserService.parse(resource);
            if (!parseResult.isSuccess())
            {
                String errorMsg = safeError(parseResult.getErrorMsg());
                if (parseResult.isTextEmpty())
                {
                    updateResourceStatus(resourceId, RESOURCE_STATUS_TEXT_EMPTY, DocumentParseResult.TEXT_EMPTY_ERROR, operator);
                }
                else
                {
                    updateResourceStatus(resourceId, RESOURCE_STATUS_FAILED, errorMsg, operator);
                }
                vectorStoreRepository.deleteChunksByDocumentId(documentId);
                vectorStoreRepository.markDocumentFailed(documentId, errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            List<DocumentChunk> chunks = documentChunkService.chunk(parseResult);
            if (chunks.isEmpty())
            {
                updateResourceStatus(resourceId, RESOURCE_STATUS_TEXT_EMPTY, DocumentParseResult.TEXT_EMPTY_ERROR, operator);
                vectorStoreRepository.deleteChunksByDocumentId(documentId);
                vectorStoreRepository.markDocumentFailed(documentId, DocumentParseResult.TEXT_EMPTY_ERROR);
                throw new IllegalStateException(DocumentParseResult.TEXT_EMPTY_ERROR);
            }

            updateResourceStatus(resourceId, RESOURCE_STATUS_PARSING, "已切分 " + chunks.size() + " 个片段", operator);
            List<float[]> embeddings = embedChunks(resourceId, chunks, operator);
            updateResourceStatus(resourceId, RESOURCE_STATUS_EMBEDDING, "正在写入向量库", operator);
            if (auditLibraryMapper.selectAuditCommonResourceById(resourceId) == null)
            {
                vectorStoreRepository.deleteByResourceIds(AuditVectorDocument.RESOURCE_TYPE_COMMON,
                        java.util.Arrays.asList(resourceId));
                return AuditVectorIndexResult.of(AuditVectorTask.STATUS_SKIPPED, documentId, 0, "文件已删除，跳过向量写入");
            }

            List<AuditVectorChunk> vectorChunks = buildVectorChunks(resource, documentId, chunks, embeddings);
            vectorStoreRepository.deleteChunksByDocumentId(documentId);
            vectorStoreRepository.insertChunks(vectorChunks);
            vectorStoreRepository.markDocumentIndexed(documentId, vectorChunks.size());
            updateResourceStatus(resourceId, RESOURCE_STATUS_STORED, "向量入库完成，共 " + vectorChunks.size() + " 个片段", operator);
            return AuditVectorIndexResult.of(AuditVectorTask.STATUS_SUCCESS, documentId, vectorChunks.size(), "向量入库完成");
        }
        catch (RuntimeException e)
        {
            String errorMsg = safeError(e.getMessage());
            if (DocumentParseResult.TEXT_EMPTY_ERROR.equals(errorMsg))
            {
                updateResourceStatus(resourceId, RESOURCE_STATUS_TEXT_EMPTY, errorMsg, operator);
            }
            else
            {
                updateResourceStatus(resourceId, RESOURCE_STATUS_FAILED, errorMsg, operator);
            }
            if (documentId != null)
            {
                vectorStoreRepository.markDocumentFailed(documentId, errorMsg);
            }
            throw e;
        }
    }

    private List<float[]> embedChunks(Long resourceId, List<DocumentChunk> chunks, String operator)
    {
        int batchSize = Math.max(1, properties.getEmbedding().getBatchSize());
        List<float[]> embeddings = new ArrayList<>();
        for (int from = 0; from < chunks.size(); from += batchSize)
        {
            int to = Math.min(from + batchSize, chunks.size());
            List<String> texts = new ArrayList<>();
            for (int i = from; i < to; i++)
            {
                texts.add(chunks.get(i).getChunkText());
            }
            embeddings.addAll(embeddingClient.embed(texts));
            updateResourceStatus(resourceId, RESOURCE_STATUS_EMBEDDING,
                    "正在生成向量 " + embeddings.size() + "/" + chunks.size(), operator);
        }
        if (embeddings.size() != chunks.size())
        {
            throw new IllegalStateException("Embedding 返回数量不匹配");
        }
        return embeddings;
    }

    private AuditVectorDocument buildDocument(AuditCommonResource resource, String fileHash)
    {
        AuditVectorDocument document = new AuditVectorDocument();
        document.setResourceType(AuditVectorDocument.RESOURCE_TYPE_COMMON);
        document.setResourceId(resource.getResourceId());
        document.setFolderId(resource.getFolderId());
        document.setDocumentName(resource.getDocumentName());
        document.setFileName(resource.getFileName());
        document.setFileUrl(resource.getFileUrl());
        document.setFileHash(fileHash);
        document.setCurrentVersionNo(StringUtils.defaultIfBlank(resource.getCurrentVersionNo(), "v1.0"));
        document.setFileType(extensionOf(resource.getFileName()));
        document.setParseStatus(AuditVectorDocument.STATUS_PENDING);
        document.setVectorStatus(AuditVectorDocument.STATUS_PENDING);
        document.setChunkCount(0);
        document.setEmbeddingModel(properties.getEmbedding().getModel());
        document.setEmbeddingDimensions(properties.getEmbedding().getDimensions());
        document.setKnowledgeBaseCode("default");
        document.setCategoryCode("");
        document.setBusinessType("");
        document.setStatus("effective");
        document.setEffectiveDate(null);
        document.setExpireDate(null);
        document.setOwnerDeptId("");
        document.setSourceSystem("audit");
        return document;
    }

    private List<AuditVectorChunk> buildVectorChunks(AuditCommonResource resource, Long documentId,
            List<DocumentChunk> chunks, List<float[]> embeddings)
    {
        List<AuditVectorChunk> vectorChunks = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++)
        {
            DocumentChunk chunk = chunks.get(i);
            AuditVectorChunk vectorChunk = new AuditVectorChunk();
            vectorChunk.setDocumentId(documentId);
            vectorChunk.setResourceId(resource.getResourceId());
            vectorChunk.setFolderId(resource.getFolderId());
            vectorChunk.setChunkNo(chunk.getChunkNo());
            vectorChunk.setChunkText(chunk.getChunkText());
            vectorChunk.setPageNo(chunk.getPageNo());
            vectorChunk.setSectionTitle(chunk.getSectionTitle());
            vectorChunk.setTokenCount(chunk.getTokenCount());
            vectorChunk.setEmbedding(embeddings.get(i));
            vectorChunk.setChunkUid("KB-CHUNK-" + documentId + "-" + chunk.getChunkNo());
            vectorChunk.setRuleCode(extractRuleCode(chunk));
            vectorChunk.setSectionPath(StringUtils.defaultString(chunk.getSectionTitle()));
            vectorChunk.setParagraphNo(null);
            vectorChunk.setContentHash(calculateSha256Text(chunk.getChunkText()));
            vectorChunk.setMetadata("{}");
            vectorChunks.add(vectorChunk);
        }
        return vectorChunks;
    }

    private String calculateSha256(String fileUrl)
    {
        try (InputStream inputStream = auditFileResolver.openFile(fileUrl))
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1)
            {
                digest.update(buffer, 0, read);
            }
            byte[] hash = digest.digest();
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash)
            {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        }
        catch (IOException e)
        {
            throw new IllegalStateException("文件读取失败：" + e.getMessage(), e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    private String calculateSha256Text(String text)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(StringUtils.defaultString(text).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash)
            {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    private String extractRuleCode(DocumentChunk chunk)
    {
        String source = StringUtils.defaultString(chunk.getSectionTitle()) + "\n" + StringUtils.defaultString(chunk.getChunkText());
        Matcher matcher = RULE_CODE_PATTERN.matcher(source);
        return matcher.find() ? matcher.group(1).replaceAll("\\s+", "") : "";
    }

    private void updateResourceStatus(Long resourceId, String storageStatus, String progressText, String operator)
    {
        AuditCommonResource update = new AuditCommonResource();
        update.setResourceId(resourceId);
        update.setStorageStatus(storageStatus);
        update.setProgressText(safeError(progressText));
        update.setUpdateBy(StringUtils.defaultIfBlank(operator, "system"));
        auditLibraryMapper.updateAuditCommonResource(update);
    }

    private String safeError(String message)
    {
        if (StringUtils.isBlank(message))
        {
            return "向量入库失败";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private String extensionOf(String fileName)
    {
        if (StringUtils.isBlank(fileName))
        {
            return "";
        }
        int index = fileName.lastIndexOf('.');
        return index >= 0 && index < fileName.length() - 1 ? fileName.substring(index + 1).toLowerCase() : "";
    }
}
