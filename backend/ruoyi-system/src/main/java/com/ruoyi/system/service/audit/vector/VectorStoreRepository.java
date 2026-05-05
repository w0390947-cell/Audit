package com.ruoyi.system.service.audit.vector;

import com.ruoyi.system.domain.audit.vector.AuditVectorChunk;
import com.ruoyi.system.domain.audit.vector.AuditVectorDocument;
import java.util.List;

public interface VectorStoreRepository
{
    AuditVectorDocument findDocument(String resourceType, Long resourceId);

    Long upsertDocument(AuditVectorDocument document);

    int deleteChunksByDocumentId(Long documentId);

    void insertChunks(List<AuditVectorChunk> chunks);

    int markDocumentIndexed(Long documentId, int chunkCount);

    int markDocumentFailed(Long documentId, String errorMsg);

    void updateFolderId(String resourceType, Long resourceId, Long folderId);

    void updateDocumentMetadata(String resourceType, Long resourceId, String fileName, String fileUrl, String versionNo);

    void deleteByResourceIds(String resourceType, List<Long> resourceIds);

    boolean existsDocument(String resourceType, Long resourceId);
}
