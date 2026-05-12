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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class UploadedBasisMatchNodeExecutor implements WorkflowNodeExecutor {

    private static final int MAX_BASIS_CHUNKS_PER_TARGET = 4;

    private final AuditTaskContentChunkRepository contentChunkRepository;
    private final AuditRetrievalRepository retrievalRepository;
    private final TextChunkService textChunkService;
    private final JsonSupport jsonSupport;

    public UploadedBasisMatchNodeExecutor(AuditTaskContentChunkRepository contentChunkRepository,
                                          AuditRetrievalRepository retrievalRepository,
                                          TextChunkService textChunkService,
                                          JsonSupport jsonSupport) {
        this.contentChunkRepository = contentChunkRepository;
        this.retrievalRepository = retrievalRepository;
        this.textChunkService = textChunkService;
        this.jsonSupport = jsonSupport;
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

        List<BasisChunk> basisChunks = splitBasisDocuments(basisDocuments);
        if (basisChunks.isEmpty()) {
            throw new BusinessException("BASIS_FILE_PARSE_FAILED", "no basis chunks generated");
        }

        retrievalRepository.deleteByTaskId(context.getTask().getTaskId());

        int referencesUsed = 0;
        int matchedTargetCount = 0;
        List<Long> unmatchedTargetChunkIds = new ArrayList<>();
        List<Map<String, Object>> usage = new ArrayList<>();
        for (ContentChunk targetChunk : targetChunks) {
            List<ScoredBasisChunk> selected = selectBasisChunks(targetChunk, basisChunks);
            if (selected.stream().anyMatch(item -> item.score() > 0)) {
                matchedTargetCount++;
            } else {
                unmatchedTargetChunkIds.add(targetChunk.getSourceChunkId());
            }

            List<RetrievalReference> references = toReferences(targetChunk, selected);
            referencesUsed += references.size();

            RetrievalRequest request = new RetrievalRequest();
            request.setRequestId(context.getTask().getTaskNo() + "-UPLOADED-BASIS-" + targetChunk.getSourceChunkId());
            request.setWorkflowCode(context.getTask().getWorkflowCode());
            request.setTaskId(context.getTask().getTaskId());
            request.setTaskNo(context.getTask().getTaskNo());
            request.setSourceChunkId(targetChunk.getSourceChunkId());
            request.setQuery(abbreviate(targetChunk.getChunkText(), 1000));
            request.setKnowledgeScope(Map.of("source", "uploaded_basis_files"));
            request.setRetrievalConfig(Map.of("strategy", "uploaded_basis_local_match", "top_k", MAX_BASIS_CHUNKS_PER_TARGET));

            Long retrievalId = retrievalRepository.insertRecord(
                    context.getTask(),
                    request,
                    jsonSupport.toJson(request),
                    jsonSupport.toJson(Map.of("result_count", references.size())),
                    references.size(),
                    "SUCCESS",
                    "",
                    "",
                    0);
            retrievalRepository.insertReferences(retrievalId, context.getTask(), targetChunk.getSourceChunkId(), references);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("source_chunk_id", targetChunk.getSourceChunkId());
            item.put("source_chunk_no", targetChunk.getChunkNo());
            item.put("reference_count", references.size());
            item.put("basis_reference_count", references.size());
            item.put("matched", selected.stream().anyMatch(score -> score.score() > 0));
            usage.add(item);
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("basis_selection_strategy", "uploaded_basis_local_match");
        summary.put("basis_file_count", basisDocuments.size());
        summary.put("basis_chunk_count", basisChunks.size());
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

    private List<BasisChunk> splitBasisDocuments(List<ParsedDocument> documents) {
        List<BasisChunk> result = new ArrayList<>();
        int fileIndex = 1;
        for (ParsedDocument document : documents) {
            String fileId = firstNotBlank(document.getMetadata().get("basis_file_id"), "BASIS-" + fileIndex);
            String fileName = firstNotBlank(document.getMetadata().get("basis_file_name"), document.getFileName());
            String fileUrl = firstNotBlank(document.getMetadata().get("basis_file_url"), "");
            List<ContentChunk> chunks = textChunkService.split(document);
            for (ContentChunk chunk : chunks) {
                String basisChunkId = fileId + "-" + chunk.getChunkNo();
                result.add(new BasisChunk(basisChunkId, fileId, fileName, fileUrl, chunk));
            }
            fileIndex++;
        }
        return result;
    }

    private List<ScoredBasisChunk> selectBasisChunks(ContentChunk targetChunk, List<BasisChunk> basisChunks) {
        Set<String> targetTerms = terms(targetChunk.getChunkText());
        List<ScoredBasisChunk> scored = new ArrayList<>();
        for (BasisChunk basisChunk : basisChunks) {
            double score = score(targetTerms, terms(basisChunk.chunk().getChunkText()));
            scored.add(new ScoredBasisChunk(basisChunk, score));
        }
        scored.sort(Comparator.comparingDouble(ScoredBasisChunk::score).reversed());
        return scored.subList(0, Math.min(MAX_BASIS_CHUNKS_PER_TARGET, scored.size()));
    }

    private List<RetrievalReference> toReferences(ContentChunk targetChunk, List<ScoredBasisChunk> selected) {
        List<RetrievalReference> references = new ArrayList<>();
        for (ScoredBasisChunk scored : selected) {
            BasisChunk basisChunk = scored.basisChunk();
            ContentChunk chunk = basisChunk.chunk();
            RetrievalReference reference = new RetrievalReference();
            reference.setSourceChunkId(targetChunk.getSourceChunkId());
            reference.setKbChunkId(basisChunk.basisChunkId());
            reference.setKbDocumentId(basisChunk.fileId());
            reference.setResourceType("uploaded_basis_file");
            reference.setFileName(basisChunk.fileName());
            reference.setFileUrl(basisChunk.fileUrl());
            reference.setVersionNo("uploaded");
            reference.setPageNo(chunk.getPageNo());
            reference.setSectionTitle(chunk.getSectionTitle());
            reference.setSectionPath(chunk.getSectionPath());
            reference.setRuleCode("");
            reference.setChunkTextSnapshot(chunk.getChunkText());
            reference.setScore(BigDecimal.valueOf(scored.score()));
            reference.setRankScore(BigDecimal.valueOf(scored.score()));
            reference.setStatus("ACTIVE");
            references.add(reference);
        }
        return references;
    }

    private double score(Set<String> targetTerms, Set<String> basisTerms) {
        if (targetTerms.isEmpty() || basisTerms.isEmpty()) {
            return 0;
        }
        int hit = 0;
        for (String term : targetTerms) {
            if (basisTerms.contains(term)) {
                hit++;
            }
        }
        return (double) hit / Math.max(1, targetTerms.size());
    }

    private Set<String> terms(String text) {
        Set<String> terms = new HashSet<>();
        String normalized = text == null ? "" : text.toLowerCase();
        for (String token : normalized.split("[^\\p{IsHan}a-z0-9./-]+")) {
            if (token.length() >= 2) {
                terms.add(token);
                if (containsHan(token)) {
                    for (int i = 0; i < token.length() - 1; i++) {
                        terms.add(token.substring(i, i + 2));
                    }
                }
            }
        }
        return terms;
    }

    private boolean containsHan(String value) {
        for (int i = 0; i < value.length(); i++) {
            Character.UnicodeScript script = Character.UnicodeScript.of(value.charAt(i));
            if (script == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
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

    private record BasisChunk(String basisChunkId, String fileId, String fileName, String fileUrl, ContentChunk chunk) {
    }

    private record ScoredBasisChunk(BasisChunk basisChunk, double score) {
    }
}
