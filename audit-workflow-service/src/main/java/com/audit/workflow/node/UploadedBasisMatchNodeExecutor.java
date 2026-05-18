package com.audit.workflow.node;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.ContentChunk;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.domain.RetrievalReference;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.repository.AuditRetrievalRepository;
import com.audit.workflow.repository.AuditTaskContentChunkRepository;
import com.audit.workflow.retrieval.RetrievalRequest;
import com.audit.workflow.service.TextChunkService;
import com.audit.workflow.support.JsonSupport;
import com.audit.workflow.vector.TemporaryBasisVectorRepository;
import com.audit.workflow.vector.TemporaryBasisVectorRepository.BasisDocument;
import com.audit.workflow.vector.TemporaryBasisVectorRepository.BasisVectorChunk;
import com.audit.workflow.vector.TemporaryBasisVectorRepository.BasisVectorHit;
import com.audit.workflow.vector.WorkflowEmbeddingClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class UploadedBasisMatchNodeExecutor implements WorkflowNodeExecutor {

    private final AuditTaskContentChunkRepository contentChunkRepository;
    private final AuditRetrievalRepository retrievalRepository;
    private final TextChunkService textChunkService;
    private final WorkflowEmbeddingClient embeddingClient;
    private final TemporaryBasisVectorRepository vectorRepository;
    private final JsonSupport jsonSupport;
    private final int topK;
    private final int maxChunkChars;

    public UploadedBasisMatchNodeExecutor(AuditTaskContentChunkRepository contentChunkRepository,
                                          AuditRetrievalRepository retrievalRepository,
                                          TextChunkService textChunkService,
                                          WorkflowEmbeddingClient embeddingClient,
                                          TemporaryBasisVectorRepository vectorRepository,
                                          JsonSupport jsonSupport,
                                          @Value("${audit.audit.chunk-max-reference-count:4}") int topK,
                                          @Value("${audit.audit.chunk-max-reference-chars:12000}") int maxChunkChars) {
        this.contentChunkRepository = contentChunkRepository;
        this.retrievalRepository = retrievalRepository;
        this.textChunkService = textChunkService;
        this.embeddingClient = embeddingClient;
        this.vectorRepository = vectorRepository;
        this.jsonSupport = jsonSupport;
        this.topK = Math.max(1, topK);
        this.maxChunkChars = Math.max(0, maxChunkChars);
    }

    @Override
    public String nodeType() {
        return NodeType.UPLOADED_BASIS_MATCH;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        List<ContentChunk> targetChunks = contentChunkRepository.findByTaskId(context.getTask().getTaskId());
        if (targetChunks.isEmpty()) {
            throw new BusinessException("BASIS_MATCH_FAILED", "target chunks not found");
        }
        List<ParsedDocument> basisDocuments = basisDocuments(context.getVariables().get("uploaded_basis_documents"));
        if (basisDocuments.isEmpty()) {
            throw new BusinessException("BASIS_FILE_REQUIRED", "basis_files is required");
        }

        List<BasisDocument> basisVectorDocuments = splitBasisDocuments(basisDocuments);
        int basisChunkCount = basisVectorDocuments.stream().mapToInt(document -> document.chunks().size()).sum();
        if (basisChunkCount == 0) {
            throw new BusinessException("BASIS_FILE_PARSE_FAILED", "no basis chunks generated");
        }

        if (hasKnowledgeScope(context)) {
            retrievalRepository.deleteUploadedBasisByTaskId(context.getTask().getTaskId());
        } else {
            retrievalRepository.deleteByTaskId(context.getTask().getTaskId());
        }

        String namespace = vectorRepository.namespace(context.getTask());
        try {
            vectorRepository.resetNamespace(namespace);
            indexBasisDocuments(context, namespace, basisVectorDocuments);
        } catch (Exception ex) {
            throw new BusinessException("TEMP_BASIS_VECTOR_INDEX_FAILED", ex.getMessage());
        }

        List<float[]> targetEmbeddings;
        try {
            targetEmbeddings = embeddingClient.embed(targetChunks.stream()
                    .map(chunk -> abbreviate(chunk.getChunkText(), 1000))
                    .toList());
        } catch (Exception ex) {
            throw new BusinessException("TEMP_BASIS_QUERY_EMBEDDING_FAILED", ex.getMessage());
        }

        int referencesUsed = 0;
        int matchedTargetCount = 0;
        List<Long> unmatchedTargetChunkIds = new ArrayList<>();
        List<Map<String, Object>> usage = new ArrayList<>();
        for (int i = 0; i < targetChunks.size(); i++) {
            ContentChunk targetChunk = targetChunks.get(i);
            List<BasisVectorHit> hits;
            long started = System.currentTimeMillis();
            try {
                hits = vectorRepository.search(namespace, targetEmbeddings.get(i), topK, maxChunkChars);
            } catch (Exception ex) {
                throw new BusinessException("TEMP_BASIS_VECTOR_SEARCH_FAILED", ex.getMessage());
            }
            if (!hits.isEmpty()) {
                matchedTargetCount++;
            } else {
                unmatchedTargetChunkIds.add(targetChunk.getSourceChunkId());
            }

            List<RetrievalReference> references = toReferences(targetChunk, hits);
            referencesUsed += references.size();

            RetrievalRequest request = new RetrievalRequest();
            request.setRequestId(context.getTask().getTaskNo() + "-UPLOADED-BASIS-" + targetChunk.getSourceChunkId());
            request.setWorkflowCode(context.getTask().getWorkflowCode());
            request.setTaskId(context.getTask().getTaskId());
            request.setTaskNo(context.getTask().getTaskNo());
            request.setSourceChunkId(targetChunk.getSourceChunkId());
            request.setQuery(abbreviate(targetChunk.getChunkText(), 1000));
            request.setKnowledgeScope(Map.of("source", "uploaded_basis_files", "namespace", namespace));
            request.setRetrievalConfig(Map.of("strategy", "uploaded_basis_temp_vector", "top_k", topK));

            Long retrievalId = retrievalRepository.insertRecord(
                    context.getTask(),
                    request,
                    jsonSupport.toJson(request),
                    jsonSupport.toJson(Map.of("result_count", references.size())),
                    references.size(),
                    "SUCCESS",
                    "",
                    "",
                    System.currentTimeMillis() - started);
            retrievalRepository.insertReferences(retrievalId, context.getTask(), targetChunk.getSourceChunkId(), references);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("source_chunk_id", targetChunk.getSourceChunkId());
            item.put("source_chunk_no", targetChunk.getChunkNo());
            item.put("reference_count", references.size());
            item.put("basis_reference_count", references.size());
            item.put("matched", !references.isEmpty());
            usage.add(item);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("basis_selection_strategy", "uploaded_basis_temp_vector");
        summary.put("basis_vector_namespace", namespace);
        summary.put("embedding_model", embeddingClient.model());
        summary.put("basis_file_count", basisDocuments.size());
        summary.put("basis_chunk_count", basisChunkCount);
        summary.put("target_chunk_count", targetChunks.size());
        summary.put("matched_target_chunk_count", matchedTargetCount);
        summary.put("unmatched_target_chunk_ids", unmatchedTargetChunkIds);
        summary.put("basis_chunks_used_in_prompt", referencesUsed);
        summary.put("chunk_reference_usage", usage);
        context.putVariable("uploaded_basis_match_summary", summary);
        context.putVariable("retrieval_reference_summary", usage);

        Map<String, Object> output = new LinkedHashMap<>(summary);
        output.put("match_status", "SUCCESS");
        return NodeExecutionResult.success(output);
    }

    private List<ParsedDocument> basisDocuments(Object value) {
        List<ParsedDocument> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof ParsedDocument document) {
                    result.add(document);
                }
            }
        }
        return result;
    }

    private boolean hasKnowledgeScope(WorkflowTaskContext context) {
        Object value = context.getInput().get("knowledge_scope");
        return value instanceof Map<?, ?> map && !map.isEmpty();
    }

    private List<BasisDocument> splitBasisDocuments(List<ParsedDocument> documents) {
        List<BasisDocument> result = new ArrayList<>();
        int fileIndex = 1;
        for (ParsedDocument document : documents) {
            String fileId = firstNotBlank(document.getMetadata().get("basis_file_id"), "BASIS-" + fileIndex);
            String fileName = firstNotBlank(document.getMetadata().get("basis_file_name"), document.getFileName());
            String fileUrl = firstNotBlank(document.getMetadata().get("basis_file_url"), "");
            List<ContentChunk> chunks = textChunkService.split(document);
            result.add(new BasisDocument(fileId, fileName, fileUrl, firstNotBlank(document.getFileHash(), document.getTextHash()), chunks));
            fileIndex++;
        }
        return result;
    }

    private void indexBasisDocuments(WorkflowTaskContext context, String namespace, List<BasisDocument> documents) {
        for (BasisDocument document : documents) {
            List<String> texts = document.chunks().stream()
                    .map(chunk -> value(chunk.getChunkText()))
                    .toList();
            List<float[]> embeddings = embeddingClient.embed(texts);
            Long documentId = vectorRepository.insertDocument(context.getTask(), namespace, document,
                    embeddingClient.model(), embeddingClient.dimensions());
            List<BasisVectorChunk> chunks = new ArrayList<>();
            for (int i = 0; i < document.chunks().size(); i++) {
                ContentChunk chunk = document.chunks().get(i);
                String basisChunkId = "WB-" + documentId + "-" + chunk.getChunkNo();
                Map<String, Object> metadata = TemporaryBasisVectorRepository.metadata(
                        context.getTask().getTaskId(),
                        context.getTask().getTaskNo(),
                        document.fileId(),
                        document.fileName(),
                        document.fileUrl());
                metadata.put("basis_chunk_id", basisChunkId);
                chunks.add(new BasisVectorChunk(documentId, basisChunkId, chunk, embeddings.get(i), metadata));
            }
            vectorRepository.insertChunks(chunks);
        }
    }

    private List<RetrievalReference> toReferences(ContentChunk targetChunk, List<BasisVectorHit> hits) {
        List<RetrievalReference> references = new ArrayList<>();
        for (BasisVectorHit hit : hits) {
            RetrievalReference reference = new RetrievalReference();
            reference.setSourceChunkId(targetChunk.getSourceChunkId());
            reference.setKbChunkId(hit.chunkUid());
            reference.setKbDocumentId(String.valueOf(hit.documentId()));
            reference.setResourceId(hit.resourceId());
            reference.setResourceType("uploaded_basis_file");
            reference.setFileName(hit.fileName());
            reference.setFileUrl(hit.fileUrl());
            reference.setVersionNo(hit.versionNo());
            reference.setPageNo(hit.pageNo());
            reference.setSectionTitle(hit.sectionTitle());
            reference.setSectionPath(hit.sectionPath());
            reference.setRuleCode("");
            reference.setChunkTextSnapshot(hit.chunkText());
            reference.setScore(hit.score());
            reference.setRankScore(hit.score());
            reference.setStatus("ACTIVE");
            references.add(reference);
        }
        return references;
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            String text = value == null ? "" : String.valueOf(value).trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private String abbreviate(String value, int maxLength) {
        String text = value == null ? "" : value;
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
