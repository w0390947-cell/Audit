package com.audit.workflow.node;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditWorkflow;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.ContentChunk;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.domain.RetrievalReference;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.model.ModelGateway;
import com.audit.workflow.model.ModelRequest;
import com.audit.workflow.model.ModelResponse;
import com.audit.workflow.repository.AuditModelCallLogRepository;
import com.audit.workflow.repository.AuditRetrievalRepository;
import com.audit.workflow.repository.AuditTaskContentChunkRepository;
import com.audit.workflow.repository.AuditWorkflowRepository;
import com.audit.workflow.support.JsonSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class AiAuditNodeExecutor implements WorkflowNodeExecutor {

    private final AuditTaskContentChunkRepository contentChunkRepository;
    private final AuditRetrievalRepository retrievalRepository;
    private final AuditWorkflowRepository workflowRepository;
    private final AuditModelCallLogRepository modelCallLogRepository;
    private final ModelGateway modelGateway;
    private final JsonSupport jsonSupport;
    private final String defaultModel;
    private final String businessReportStrategy;
    private final boolean includeAllReferences;
    private final int maxReferenceCount;
    private final int maxReferenceChars;
    private final int chunkMaxReferenceCount;
    private final int chunkMaxReferenceChars;
    private final int chunkModelParallelism;
    private final int chunkModelMaxRetries;
    private final int chunkModelTimeoutRetries;
    private final double chunkFailureFatalRatio;
    private final int aiAuditTimeoutSeconds;

    public AiAuditNodeExecutor(AuditTaskContentChunkRepository contentChunkRepository,
                               AuditRetrievalRepository retrievalRepository,
                               AuditWorkflowRepository workflowRepository,
                               AuditModelCallLogRepository modelCallLogRepository,
                               ModelGateway modelGateway,
                               JsonSupport jsonSupport,
                               @Value("${audit.model.default-chat-model:qwen-plus}") String defaultModel,
                               @Value("${audit.audit.business-report-strategy:chunk_then_merge}") String businessReportStrategy,
                               @Value("${audit.audit.include-all-references:false}") boolean includeAllReferences,
                               @Value("${audit.audit.max-reference-count:8}") int maxReferenceCount,
                               @Value("${audit.audit.max-reference-chars:8000}") int maxReferenceChars,
                               @Value("${audit.audit.chunk-max-reference-count:4}") int chunkMaxReferenceCount,
                               @Value("${audit.audit.chunk-max-reference-chars:12000}") int chunkMaxReferenceChars,
                               @Value("${audit.audit.chunk-model-parallelism:3}") int chunkModelParallelism,
                               @Value("${audit.audit.chunk-model-max-retries:2}") int chunkModelMaxRetries,
                               @Value("${audit.audit.chunk-model-timeout-retries:0}") int chunkModelTimeoutRetries,
                               @Value("${audit.audit.chunk-failure-fatal-ratio:0.2}") double chunkFailureFatalRatio,
                               @Value("${audit.audit.ai-audit-timeout-seconds:840}") int aiAuditTimeoutSeconds) {
        this.contentChunkRepository = contentChunkRepository;
        this.retrievalRepository = retrievalRepository;
        this.workflowRepository = workflowRepository;
        this.modelCallLogRepository = modelCallLogRepository;
        this.modelGateway = modelGateway;
        this.jsonSupport = jsonSupport;
        this.defaultModel = defaultModel;
        this.businessReportStrategy = businessReportStrategy;
        this.includeAllReferences = includeAllReferences;
        this.maxReferenceCount = maxReferenceCount;
        this.maxReferenceChars = maxReferenceChars;
        this.chunkMaxReferenceCount = chunkMaxReferenceCount;
        this.chunkMaxReferenceChars = chunkMaxReferenceChars;
        this.chunkModelParallelism = chunkModelParallelism;
        this.chunkModelMaxRetries = chunkModelMaxRetries;
        this.chunkModelTimeoutRetries = chunkModelTimeoutRetries;
        this.chunkFailureFatalRatio = chunkFailureFatalRatio;
        this.aiAuditTimeoutSeconds = aiAuditTimeoutSeconds;
    }

    @Override
    public String nodeType() {
        return NodeType.AI_AUDIT;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        if (isBusinessReportFindingsMode(node)) {
            return executeBusinessReportAudit(context);
        }
        List<ContentChunk> chunks = contentChunkRepository.findByTaskId(context.getTask().getTaskId());
        if (chunks.isEmpty()) {
            throw new BusinessException("MODEL_RESPONSE_INVALID", "no source chunks for AI audit");
        }
        AuditWorkflow workflow = workflowRepository.findByCode(context.getTask().getWorkflowCode())
                .orElseThrow(() -> new BusinessException("WORKFLOW_NOT_FOUND", "workflow not found"));

        List<Map<String, Object>> chunkResults = new ArrayList<>();
        int referenceCount = 0;
        int referencesUsedInPrompt = 0;
        int modelCallCount = 0;
        for (ContentChunk chunk : chunks) {
            List<RetrievalReference> references = retrievalRepository.findReferencesByTaskIdAndSourceChunkId(
                    context.getTask().getTaskId(), chunk.getSourceChunkId());
            referenceCount += references.size();
            referencesUsedInPrompt += references.size();
            String prompt = buildPrompt(workflow, chunk, references);

            ModelRequest request = new ModelRequest();
            request.setTaskId(context.getTask().getTaskId());
            request.setTaskNo(context.getTask().getTaskNo());
            request.setWorkflowCode(context.getTask().getWorkflowCode());
            request.setSourceChunkId(chunk.getSourceChunkId());
            request.setModelName(defaultModel);
            request.setSystemPrompt("");
            request.setUserPrompt(prompt);
            ModelResponse response = modelGateway.chat(request);
            modelCallCount++;
            modelCallLogRepository.insertLog(context.getTask(), request, response);
            if (!response.isSuccess()) {
                throw new BusinessException(response.getErrorCode(), response.getErrorMsg());
            }

            Map<String, Object> result = parseModelJson(response.getContent());
            result.put("source_chunk_id", chunk.getSourceChunkId());
            chunkResults.add(result);
        }

        context.putVariable("chunk_audit_results", chunkResults);
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("audit_status", "SUCCESS");
        output.put("chunk_result_count", chunkResults.size());
        output.put("reference_count", referenceCount);
        output.put("references_used_in_prompt", referencesUsedInPrompt);
        output.put("basis_chunks_used_in_prompt", referencesUsedInPrompt);
        output.put("model_call_count", modelCallCount);
        output.put("finding_count", countResultFindings(chunkResults));
        return NodeExecutionResult.success(output);
    }

    private NodeExecutionResult executeBusinessReportAudit(WorkflowTaskContext context) {
        if ("chunk_then_merge".equalsIgnoreCase(businessReportStrategy)) {
            return executeChunkThenMergeBusinessReportAudit(context);
        }
        Object parsed = context.getVariables().get("parsed_document");
        if (!(parsed instanceof ParsedDocument document)) {
            throw new BusinessException("MODEL_RESPONSE_INVALID", "parsed document not found for business report audit");
        }
        AuditWorkflow workflow = workflowRepository.findByCode(context.getTask().getWorkflowCode())
                .orElseThrow(() -> new BusinessException("WORKFLOW_NOT_FOUND", "workflow not found"));
        List<RetrievalReference> references = retrievalRepository.findReferencesByTaskId(context.getTask().getTaskId());
        ReferenceSelection selection = selectBusinessReportReferences(context, references);

        ModelRequest request = new ModelRequest();
        request.setTaskId(context.getTask().getTaskId());
        request.setTaskNo(context.getTask().getTaskNo());
        request.setWorkflowCode(context.getTask().getWorkflowCode());
        request.setModelName(defaultModel);
        request.setSystemPrompt("");
        request.setUserPrompt(buildBusinessReportPrompt(workflow, document, selection.references()));
        ModelResponse response = modelGateway.chat(request);
        modelCallLogRepository.insertLog(context.getTask(), request, response);
        if (!response.isSuccess()) {
            throw new BusinessException(response.getErrorCode(), response.getErrorMsg());
        }

        Map<String, Object> result = parseModelJson(response.getContent());
        context.putVariable("model_result", result);
        context.putVariable("retrieval_used_summary", selection.summary());

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("audit_status", "SUCCESS");
        output.put("audit_mode", "business_report_findings");
        output.put("reference_count", selection.summary().get("reference_count"));
        output.put("references_used_in_prompt", selection.summary().get("references_used_in_prompt"));
        output.put("basis_chunks_used_in_prompt", selection.summary().get("references_used_in_prompt"));
        output.put("model_call_count", 1);
        output.put("finding_count", countResultFindings(result));
        output.put("retrieval_used_summary", selection.summary());
        output.put("model_result_keys", result.keySet());
        return NodeExecutionResult.success(output);
    }

    private NodeExecutionResult executeChunkThenMergeBusinessReportAudit(WorkflowTaskContext context) {
        AuditWorkflow workflow = workflowRepository.findByCode(context.getTask().getWorkflowCode())
                .orElseThrow(() -> new BusinessException("WORKFLOW_NOT_FOUND", "workflow not found"));
        List<ContentChunk> chunks = contentChunkRepository.findByTaskId(context.getTask().getTaskId());
        if (chunks.isEmpty()) {
            throw new BusinessException("MODEL_RESPONSE_INVALID", "no source chunks for business report audit");
        }

        List<Map<String, Object>> rawFindings = new ArrayList<>();
        List<Map<String, Object>> warnings = new ArrayList<>();
        Set<Long> coveredChunkIds = new HashSet<>();
        List<Long> sourceChunkIds = new ArrayList<>();
        List<Map<String, Object>> chunkReferenceUsage = new ArrayList<>();
        int totalReferenceCount = 0;
        int referencesUsedInPrompt = 0;
        int totalInputTokens = 0;
        int totalOutputTokens = 0;
        long totalDurationMs = 0;
        int modelCallCount = 0;
        int chunkSuccessCount = 0;
        int chunkFailedCount = 0;
        long auditStartedNanos = System.nanoTime();
        long auditTimeoutNanos = TimeUnit.SECONDS.toNanos(Math.max(1, aiAuditTimeoutSeconds));

        List<RetrievalReference> allReferences = retrievalRepository.findReferencesByTaskId(context.getTask().getTaskId());
        Map<Long, List<RetrievalReference>> referencesByChunk = groupReferencesByChunk(allReferences);
        int expectedReferenceCount = expectedReferenceCount(context);
        if (expectedReferenceCount > 0 && allReferences.isEmpty()) {
            throw new BusinessException("KB_REFERENCES_NOT_VISIBLE_TO_CHUNK_AUDIT",
                    "knowledge references were created by retrieval node but are not visible to chunk audit");
        }

        boolean uploadedBasisWorkflow = isUploadedBasisWorkflow(context);
        String auditStrategy = uploadedBasisWorkflow ? "uploaded_basis_chunk_then_merge" : "chunk_then_merge";
        int parallelism = Math.max(1, Math.min(chunkModelParallelism, chunks.size()));
        ExecutorService executorService = Executors.newFixedThreadPool(parallelism);
        List<Future<ChunkAuditOutput>> futures = new ArrayList<>();
        for (ContentChunk chunk : chunks) {
            sourceChunkIds.add(chunk.getSourceChunkId());
            List<RetrievalReference> chunkReferences = referencesByChunk.getOrDefault(chunk.getSourceChunkId(), List.of());
            futures.add(executorService.submit(chunkAuditCallable(context, workflow, chunk, chunkReferences, uploadedBasisWorkflow)));
        }
        try {
            for (Future<ChunkAuditOutput> future : futures) {
                ChunkAuditOutput chunkOutput = future.get(remainingAuditTimeoutMillis(auditStartedNanos, auditTimeoutNanos),
                        TimeUnit.MILLISECONDS);
                totalReferenceCount += chunkOutput.totalReferenceCount();
                referencesUsedInPrompt += chunkOutput.referencesUsedInPrompt();
                if (chunkOutput.coveredSourceChunkId() != null) {
                    coveredChunkIds.add(chunkOutput.coveredSourceChunkId());
                }
                modelCallCount += chunkOutput.modelCallCount();
                totalInputTokens += chunkOutput.inputTokens();
                totalOutputTokens += chunkOutput.outputTokens();
                totalDurationMs += chunkOutput.durationMs();
                if (chunkOutput.success()) {
                    chunkSuccessCount++;
                } else {
                    chunkFailedCount++;
                }
                rawFindings.addAll(chunkOutput.findings());
                warnings.addAll(chunkOutput.warnings());
                chunkReferenceUsage.add(chunkOutput.referenceUsage());
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException("MODEL_INTERRUPTED", "chunk audit interrupted");
        } catch (TimeoutException ex) {
            cancelUnfinishedFutures(futures);
            throw new BusinessException("AI_AUDIT_TIMEOUT",
                    "AI审核节点执行超过 " + aiAuditTimeoutSeconds + " 秒，已取消未完成的分片模型调用");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("MODEL_RESPONSE_INVALID", cause == null ? ex.getMessage() : cause.getMessage());
        } finally {
            executorService.shutdownNow();
        }

        if (totalReferenceCount > 0 && referencesUsedInPrompt == 0) {
            throw new BusinessException("KB_REFERENCES_NOT_VISIBLE_TO_CHUNK_AUDIT",
                    "knowledge references exist but no references were used in chunk model prompts");
        }

        List<Map<String, Object>> findings = deduplicateFindings(rawFindings);
        List<Long> uncoveredChunkIds = new ArrayList<>();
        for (Long sourceChunkId : sourceChunkIds) {
            if (!coveredChunkIds.contains(sourceChunkId)) {
                uncoveredChunkIds.add(sourceChunkId);
            }
        }

        Map<String, Object> retrievalSummary = new LinkedHashMap<>();
        retrievalSummary.put("reference_selection_strategy", auditStrategy);
        retrievalSummary.put("source_chunk_count", sourceChunkIds.size());
        retrievalSummary.put("retrieval_count", sourceChunkIds.size());
        retrievalSummary.put("reference_count", totalReferenceCount);
        retrievalSummary.put("references_used_in_prompt", referencesUsedInPrompt);
        retrievalSummary.put("covered_source_chunk_count", coveredChunkIds.size());
        retrievalSummary.put("uncovered_source_chunk_ids", uncoveredChunkIds);
        retrievalSummary.put("chunk_max_reference_count", chunkMaxReferenceCount);
        retrievalSummary.put("chunk_max_reference_chars", chunkMaxReferenceChars);
        retrievalSummary.put("chunk_model_parallelism", parallelism);
        retrievalSummary.put("chunk_reference_usage", chunkReferenceUsage);
        retrievalSummary.put("reference_truncated", referencesUsedInPrompt < totalReferenceCount);

        Map<String, Object> usageSummary = new LinkedHashMap<>();
        usageSummary.put("model", defaultModel);
        usageSummary.put("model_call_count", modelCallCount);
        usageSummary.put("parallelism", parallelism);
        usageSummary.put("max_retries", chunkModelMaxRetries);
        usageSummary.put("timeout_retries", chunkModelTimeoutRetries);
        usageSummary.put("ai_audit_timeout_seconds", aiAuditTimeoutSeconds);
        usageSummary.put("chunk_success_count", chunkSuccessCount);
        usageSummary.put("chunk_failed_count", chunkFailedCount);
        usageSummary.put("input_tokens", totalInputTokens);
        usageSummary.put("output_tokens", totalOutputTokens);
        usageSummary.put("duration_ms", totalDurationMs);

        if (chunkSuccessCount == 0) {
            throw new BusinessException("MODEL_CHUNK_AUDIT_ALL_FAILED", "all chunk model audits failed");
        }
        if (isChunkFailureFatal(chunkFailedCount, chunks.size())) {
            throw new BusinessException("CHUNK_AUDIT_INCOMPLETE",
                    "chunk audit failed ratio reached fatal threshold: " + chunkFailedCount + "/" + chunks.size());
        }
        warnings.addAll(detectRiskWarnings(context, chunks, findings));
        List<Map<String, Object>> failedChunks = failedChunks(warnings);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("partial_success", chunkFailedCount > 0);
        result.put("summary", businessReportSummary(findings, warnings, chunkFailedCount));
        result.put("totalIssues", findings.size());
        result.put("findings", findings);
        if (!warnings.isEmpty()) {
            result.put("warnings", warnings);
        }
        if (!failedChunks.isEmpty()) {
            result.put("failed_chunks", failedChunks);
        }
        result.put("audit_strategy", auditStrategy);
        result.put("chunk_success_count", chunkSuccessCount);
        result.put("chunk_failed_count", chunkFailedCount);
        result.put("model_usage_summary", usageSummary);
        if (uploadedBasisWorkflow) {
            result.put("basis_used_summary", retrievalSummary);
        }

        context.putVariable("model_result", result);
        context.putVariable("retrieval_used_summary", retrievalSummary);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("audit_status", "SUCCESS");
        output.put("audit_mode", "business_report_findings");
        output.put("audit_strategy", auditStrategy);
        output.put("chunk_result_count", chunks.size());
        output.put("chunk_success_count", chunkSuccessCount);
        output.put("chunk_failed_count", chunkFailedCount);
        output.put("reference_count", totalReferenceCount);
        output.put("references_used_in_prompt", referencesUsedInPrompt);
        output.put("basis_chunks_used_in_prompt", referencesUsedInPrompt);
        output.put("model_call_count", modelCallCount);
        output.put("finding_count", findings.size());
        output.put("candidate_count", findings.size());
        output.put("retrieval_used_summary", retrievalSummary);
        output.put("model_usage_summary", usageSummary);
        return NodeExecutionResult.success(output);
    }

    private long remainingAuditTimeoutMillis(long auditStartedNanos, long auditTimeoutNanos) {
        long elapsedNanos = System.nanoTime() - auditStartedNanos;
        long remainingNanos = auditTimeoutNanos - elapsedNanos;
        if (remainingNanos <= 0) {
            return 1;
        }
        return Math.max(1, TimeUnit.NANOSECONDS.toMillis(remainingNanos));
    }

    private void cancelUnfinishedFutures(List<Future<ChunkAuditOutput>> futures) {
        for (Future<ChunkAuditOutput> future : futures) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }

    private Callable<ChunkAuditOutput> chunkAuditCallable(WorkflowTaskContext context,
                                                          AuditWorkflow workflow,
                                                          ContentChunk chunk,
                                                          List<RetrievalReference> references,
                                                          boolean uploadedBasisWorkflow) {
        return () -> {
            List<RetrievalReference> selectedReferences = selectChunkReferences(references);
            Map<String, Object> referenceUsage = new LinkedHashMap<>();
            referenceUsage.put("source_chunk_id", chunk.getSourceChunkId());
            referenceUsage.put("source_chunk_no", chunk.getChunkNo());
            referenceUsage.put("reference_count_before_prompt", references.size());
            referenceUsage.put("references_used_in_prompt", selectedReferences.size());
            Long coveredSourceChunkId = selectedReferences.isEmpty() ? null : chunk.getSourceChunkId();

            int modelCallCount = 0;
            int inputTokens = 0;
            int outputTokens = 0;
            long durationMs = 0;
            String lastErrorCode = "";
            String lastErrorMsg = "";
            String lastResponseContent = "";
            Map<String, Object> chunkResult = null;
            int maxAttempts = 1 + Math.max(chunkModelMaxRetries, chunkModelTimeoutRetries);
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                ModelRequest request = new ModelRequest();
                request.setTaskId(context.getTask().getTaskId());
                request.setTaskNo(context.getTask().getTaskNo());
                request.setWorkflowCode(context.getTask().getWorkflowCode());
                request.setSourceChunkId(chunk.getSourceChunkId());
                request.setModelName(defaultModel);
                request.setSystemPrompt("");
                request.setUserPrompt(buildBusinessReportChunkPrompt(workflow, context, chunk, selectedReferences, uploadedBasisWorkflow));

                ModelResponse response = modelGateway.chat(request);
                modelCallLogRepository.insertLog(context.getTask(), request, response);
                modelCallCount++;
                inputTokens += response.getInputTokens();
                outputTokens += response.getOutputTokens();
                durationMs += response.getDurationMs();
                if (!response.isSuccess()) {
                    lastErrorCode = response.getErrorCode();
                    lastErrorMsg = response.getErrorMsg();
                    if (attempt < maxRetriesForModelError(lastErrorCode) && isRetryableModelError(lastErrorCode)) {
                        continue;
                    }
                    return failedChunkOutput(chunk, references.size(), selectedReferences.size(), coveredSourceChunkId,
                            modelCallCount, inputTokens, outputTokens, durationMs,
                            lastErrorCode, lastErrorMsg, null, attempt, referenceUsage);
                }

                try {
                    chunkResult = parseModelJson(response.getContent());
                    break;
                } catch (BusinessException ex) {
                    lastErrorCode = ex.getErrorCode();
                    lastErrorMsg = ex.getMessage();
                    lastResponseContent = response.getContent();
                    if (attempt < maxRetriesForModelError(lastErrorCode) && isRetryableModelError(lastErrorCode)) {
                        continue;
                    }
                    return failedChunkOutput(chunk, references.size(), selectedReferences.size(), coveredSourceChunkId,
                            modelCallCount, inputTokens, outputTokens, durationMs,
                            lastErrorCode, lastErrorMsg, lastResponseContent, attempt, referenceUsage);
                }
            }

            List<Map<String, Object>> findings = new ArrayList<>();
            for (Map<String, Object> finding : listOfMaps(chunkResult.get("findings"))) {
                Map<String, Object> enrichedFinding = new LinkedHashMap<>(finding);
                enrichedFinding.put("source_chunk_id", chunk.getSourceChunkId());
                enrichedFinding.put("source_chunk_no", chunk.getChunkNo());
                enrichFindingLocation(enrichedFinding, chunk);
                findings.add(enrichedFinding);
            }

            return new ChunkAuditOutput(
                    references.size(),
                    selectedReferences.size(),
                    coveredSourceChunkId,
                    modelCallCount,
                    inputTokens,
                    outputTokens,
                    durationMs,
                    true,
                    findings,
                    List.of(),
                    referenceUsage);
        };
    }

    private ChunkAuditOutput failedChunkOutput(ContentChunk chunk,
                                               int referenceCount,
                                               int referencesUsed,
                                               Long coveredSourceChunkId,
                                               int modelCallCount,
                                               int inputTokens,
                                               int outputTokens,
                                               long durationMs,
                                               String errorCode,
                                               String errorMsg,
                                               String responseContent,
                                               int retryCount,
                                               Map<String, Object> referenceUsage) {
        Map<String, Object> warning = new LinkedHashMap<>();
        warning.put("type", "chunk_audit_failed");
        warning.put("source_chunk_id", chunk.getSourceChunkId());
        warning.put("source_chunk_no", chunk.getChunkNo());
        warning.put("error_code", stringValue(errorCode).isBlank() ? "MODEL_RESPONSE_INVALID" : errorCode);
        warning.put("message", stringValue(errorMsg).isBlank() ? "分片模型调用或响应解析失败" : errorMsg);
        warning.put("retry_count", retryCount);
        warning.put("final_status", "FAILED");
        if (!stringValue(responseContent).isBlank()) {
            warning.put("response_excerpt", abbreviate(stripJson(responseContent), "", 300));
        }
        return new ChunkAuditOutput(referenceCount, referencesUsed, coveredSourceChunkId, modelCallCount,
                inputTokens, outputTokens, durationMs, false, List.of(), List.of(warning), referenceUsage);
    }

    private String buildPrompt(AuditWorkflow workflow, ContentChunk chunk, List<RetrievalReference> references) {
        StringBuilder builder = new StringBuilder();
        builder.append(requirePromptTemplate(workflow)).append("\n\n");
        builder.append("\n\n待审核内容元信息：\n");
        builder.append("- source_chunk_id: ").append(chunk.getSourceChunkId()).append("\n");
        builder.append("- chunk_no: ").append(chunk.getChunkNo()).append("\n");
        builder.append("- page_no: ").append(chunk.getPageNo()).append("\n");
        builder.append("- section_title: ").append(chunk.getSectionTitle()).append("\n\n");
        builder.append("待审核内容：\n");
        builder.append(chunk.getChunkText()).append("\n\n");
        builder.append("检索依据：\n");
        builder.append(formatReferences(references)).append("\n\n");
        return builder.toString();
    }

    private String buildBusinessReportPrompt(AuditWorkflow workflow, ParsedDocument document, List<RetrievalReference> references) {
        StringBuilder builder = new StringBuilder();
        builder.append(requirePromptTemplate(workflow)).append("\n\n");
        builder.append("【待审阅报告】：\n");
        builder.append(document.getFullText()).append("\n\n");
        builder.append("【知识库搜索结果】：\n");
        builder.append(formatReferences(references)).append("\n");
        return builder.toString();
    }

    private String buildBusinessReportChunkPrompt(AuditWorkflow workflow, WorkflowTaskContext context, ContentChunk chunk, List<RetrievalReference> references) {
        return buildBusinessReportChunkPrompt(workflow, context, chunk, references, false);
    }

    private String buildBusinessReportChunkPrompt(AuditWorkflow workflow,
                                                  WorkflowTaskContext context,
                                                  ContentChunk chunk,
                                                  List<RetrievalReference> references,
                                                  boolean uploadedBasisWorkflow) {
        StringBuilder builder = new StringBuilder();
        builder.append(requirePromptTemplate(workflow)).append("\n\n");
        builder.append("【待审阅报告片段元信息】：\n");
        builder.append("- source_chunk_id: ").append(chunk.getSourceChunkId()).append("\n");
        builder.append("- chunk_no: ").append(chunk.getChunkNo()).append("\n");
        builder.append("- page_no: ").append(chunk.getPageNo()).append("\n");
        String productName = productName(context);
        if (!productName.isBlank()) {
            builder.append("- product_name: ").append(productName).append("\n");
        }
        builder.append("- section_title: ").append(chunk.getSectionTitle()).append("\n");
        builder.append("- section_path: ").append(chunk.getSectionPath()).append("\n\n");
        builder.append("【待审阅报告】：\n");
        builder.append(chunk.getChunkText()).append("\n\n");
        builder.append(uploadedBasisWorkflow ? "【本次上传依据文件匹配结果】：\n" : "【知识库搜索结果】：\n");
        builder.append(formatReferences(references, chunkMaxReferenceCount, chunkMaxReferenceChars)).append("\n");
        return builder.toString();
    }

    private String requirePromptTemplate(AuditWorkflow workflow) {
        if (workflow.getPromptTemplate() == null || workflow.getPromptTemplate().isBlank()) {
            throw new BusinessException("PROMPT_TEMPLATE_REQUIRED",
                    "audit_workflow.prompt_template is required for workflow " + workflow.getWorkflowCode());
        }
        return workflow.getPromptTemplate().trim();
    }

    private String formatReferences(List<RetrievalReference> references) {
        return formatReferences(references, isReferenceCountLimited() ? maxReferenceCount : 0, maxReferenceChars);
    }

    private String formatReferences(List<RetrievalReference> references, int maxCount, int maxChars) {
        if (references == null || references.isEmpty()) {
            return "未检索到依据。";
        }
        StringBuilder builder = new StringBuilder();
        int totalChars = 0;
        int count = 0;
        for (RetrievalReference reference : references) {
            if (maxCount > 0 && count >= maxCount) {
                break;
            }
            String text = reference.getChunkTextSnapshot() == null ? "" : reference.getChunkTextSnapshot();
            if (maxChars > 0 && totalChars + text.length() > maxChars) {
                text = text.substring(0, Math.max(0, maxChars - totalChars));
            }
            builder.append("- source_chunk_id: ").append(reference.getSourceChunkId()).append("\n");
            builder.append("- kb_chunk_id: ").append(reference.getKbChunkId()).append("\n");
            builder.append("  source: ").append(reference.getFileName()).append("\n");
            builder.append("  version_no: ").append(reference.getVersionNo()).append("\n");
            builder.append("  section: ").append(reference.getSectionTitle()).append("\n");
            builder.append("  quote: ").append(text).append("\n");
            totalChars += text.length();
            count++;
            if (maxChars > 0 && totalChars >= maxChars) {
                break;
            }
        }
        return builder.toString();
    }

    private ReferenceSelection selectBusinessReportReferences(WorkflowTaskContext context, List<RetrievalReference> references) {
        List<Long> sourceChunkIds = longList(context.getVariables().get("source_chunk_ids"));
        Map<Long, List<RetrievalReference>> referencesByChunk = new LinkedHashMap<>();
        for (Long sourceChunkId : sourceChunkIds) {
            referencesByChunk.put(sourceChunkId, new ArrayList<>());
        }
        for (RetrievalReference reference : references) {
            referencesByChunk.computeIfAbsent(reference.getSourceChunkId(), ignored -> new ArrayList<>()).add(reference);
        }

        List<RetrievalReference> selected = new ArrayList<>();
        Set<String> selectedKeys = new HashSet<>();
        int chars = 0;
        int maxDepth = referencesByChunk.values().stream().mapToInt(List::size).max().orElse(0);
        for (int rank = 0; rank < maxDepth && canSelectMoreReferences(selected.size(), chars); rank++) {
            for (Map.Entry<Long, List<RetrievalReference>> entry : referencesByChunk.entrySet()) {
                List<RetrievalReference> chunkReferences = entry.getValue();
                if (rank >= chunkReferences.size()) {
                    continue;
                }
                RetrievalReference reference = chunkReferences.get(rank);
                String key = reference.getSourceChunkId() + "|" + stringValue(reference.getKbChunkId()) + "|" + stringValue(reference.getChunkTextSnapshot());
                if (!selectedKeys.add(key)) {
                    continue;
                }
                int estimatedChars = estimateReferenceChars(reference);
                if (isReferenceCharsLimited() && !selected.isEmpty() && chars + estimatedChars > maxReferenceChars) {
                    continue;
                }
                selected.add(reference);
                chars += estimatedChars;
                if (!canSelectMoreReferences(selected.size(), chars)) {
                    break;
                }
            }
        }

        Set<Long> coveredChunkIds = new HashSet<>();
        for (RetrievalReference reference : selected) {
            if (reference.getSourceChunkId() != null) {
                coveredChunkIds.add(reference.getSourceChunkId());
            }
        }
        List<Long> uncoveredChunkIds = new ArrayList<>();
        for (Long sourceChunkId : sourceChunkIds) {
            if (!coveredChunkIds.contains(sourceChunkId)) {
                uncoveredChunkIds.add(sourceChunkId);
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source_chunk_count", sourceChunkIds.size());
        summary.put("retrieval_count", referencesByChunk.size());
        summary.put("reference_count", references == null ? 0 : references.size());
        summary.put("references_used_in_prompt", selected.size());
        summary.put("covered_source_chunk_count", coveredChunkIds.size());
        summary.put("uncovered_source_chunk_ids", uncoveredChunkIds);
        summary.put("include_all_references", includeAllReferences);
        summary.put("max_reference_count", maxReferenceCount);
        summary.put("max_reference_chars", maxReferenceChars);
        summary.put("reference_truncated", selected.size() < selectedKeys.size()
                || (!includeAllReferences && references != null && selected.size() < references.size()));
        return new ReferenceSelection(selected, summary);
    }

    private List<RetrievalReference> selectChunkReferences(List<RetrievalReference> references) {
        List<RetrievalReference> selected = new ArrayList<>();
        if (references == null || references.isEmpty()) {
            return selected;
        }
        Set<String> selectedKeys = new HashSet<>();
        int chars = 0;
        for (RetrievalReference reference : references) {
            String key = reference.getSourceChunkId() + "|" + stringValue(reference.getKbChunkId()) + "|" + stringValue(reference.getChunkTextSnapshot());
            if (!selectedKeys.add(key)) {
                continue;
            }
            if (chunkMaxReferenceCount > 0 && selected.size() >= chunkMaxReferenceCount) {
                break;
            }
            int estimatedChars = estimateReferenceChars(reference);
            if (chunkMaxReferenceChars > 0 && !selected.isEmpty() && chars + estimatedChars > chunkMaxReferenceChars) {
                continue;
            }
            selected.add(reference);
            chars += estimatedChars;
            if (chunkMaxReferenceChars > 0 && chars >= chunkMaxReferenceChars) {
                break;
            }
        }
        return selected;
    }

    private Map<Long, List<RetrievalReference>> groupReferencesByChunk(List<RetrievalReference> references) {
        Map<Long, List<RetrievalReference>> grouped = new LinkedHashMap<>();
        for (RetrievalReference reference : references) {
            grouped.computeIfAbsent(reference.getSourceChunkId(), ignored -> new ArrayList<>()).add(reference);
        }
        return grouped;
    }

    private int expectedReferenceCount(WorkflowTaskContext context) {
        int count = 0;
        Object value = context.getVariables().get("retrieval_reference_summary");
        if (value instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> summary = mapValue(item);
                count += intValue(summary.get("reference_count"));
            }
        }
        return count;
    }

    private List<Map<String, Object>> deduplicateFindings(List<Map<String, Object>> rawFindings) {
        Map<String, Map<String, Object>> findingsByKey = new LinkedHashMap<>();
        for (Map<String, Object> finding : rawFindings) {
            String key = normalizeFindingKey(finding);
            findingsByKey.merge(key, finding, this::mergeFinding);
        }
        return new ArrayList<>(findingsByKey.values());
    }

    private Map<String, Object> mergeFinding(Map<String, Object> left, Map<String, Object> right) {
        String severity = higherSeverity(stringValue(left.get("severity")), stringValue(right.get("severity")));
        left.put("severity", severity);
        if (stringValue(left.get("source_chunk_id")).isBlank()) {
            left.put("source_chunk_id", right.get("source_chunk_id"));
        }
        if (stringValue(left.get("source_chunk_no")).isBlank()) {
            left.put("source_chunk_no", right.get("source_chunk_no"));
        }
        return left;
    }

    private String normalizeFindingKey(Map<String, Object> finding) {
        String title = normalizeKeyText(firstNotBlank(finding.get("title"), finding.get("content")));
        String content = normalizeKeyText(firstNotBlank(finding.get("content"), finding.get("problem")));
        String location = normalizeKeyText(finding.get("location"));
        return title + "|" + abbreviate(content, "", 80) + "|" + location;
    }

    private String normalizeKeyText(Object value) {
        return stringValue(value)
                .trim()
                .replaceAll("\\s+", "")
                .replace("。", "")
                .replace("，", "")
                .toLowerCase();
    }

    private String higherSeverity(String left, String right) {
        return severityRank(right) > severityRank(left) ? right : left;
    }

    private int severityRank(String severity) {
        return switch (severity == null ? "" : severity.toLowerCase()) {
            case "high" -> 3;
            case "medium" -> 2;
            default -> 1;
        };
    }

    private List<Map<String, Object>> detectRiskWarnings(WorkflowTaskContext context,
                                                         List<ContentChunk> chunks,
                                                         List<Map<String, Object>> findings) {
        List<Map<String, Object>> warnings = new ArrayList<>();
        if (!findings.isEmpty()) {
            return warnings;
        }
        String text = documentText(context, chunks);
        List<String> matchedSignals = new ArrayList<>();
        for (String signal : List.of("缺失", "未填写", "未提供", "不一致", "不符合", "错误", "异常", "禁止", "不得")) {
            if (text.contains(signal)) {
                matchedSignals.add(signal);
            }
        }
        if (!matchedSignals.isEmpty()) {
            Map<String, Object> warning = new LinkedHashMap<>();
            warning.put("type", "manual_review_recommended");
            warning.put("message", "模型未返回问题，但待审阅报告包含风险信号词，建议业务侧人工复核。");
            warning.put("signals", matchedSignals);
            warnings.add(warning);
        }
        return warnings;
    }

    private String documentText(WorkflowTaskContext context, List<ContentChunk> chunks) {
        Object parsed = context.getVariables().get("parsed_document");
        if (parsed instanceof ParsedDocument document && document.getFullText() != null) {
            return document.getFullText();
        }
        StringBuilder builder = new StringBuilder();
        for (ContentChunk chunk : chunks) {
            builder.append(chunk.getChunkText()).append('\n');
        }
        return builder.toString();
    }

    private String businessReportSummary(List<Map<String, Object>> findings,
                                         List<Map<String, Object>> warnings,
                                         int chunkFailedCount) {
        if (!findings.isEmpty()) {
            if (chunkFailedCount > 0) {
                return "本次审核发现" + findings.size() + "个问题，" + chunkFailedCount + "个片段需人工复核";
            }
            if (!warnings.isEmpty()) {
                return "本次审核发现" + findings.size() + "个问题，部分内容需人工复核";
            }
            return "本次审核发现" + findings.size() + "个问题";
        }
        if (!warnings.isEmpty()) {
            return "未发现关键问题，但存在需人工复核的风险信号";
        }
        return "未发现关键问题";
    }

    private boolean isRetryableModelError(String errorCode) {
        return "MODEL_TIMEOUT".equals(errorCode)
                || "MODEL_UNAVAILABLE".equals(errorCode)
                || "MODEL_RESPONSE_INVALID".equals(errorCode);
    }

    private int maxRetriesForModelError(String errorCode) {
        if ("MODEL_TIMEOUT".equals(errorCode)) {
            return Math.max(0, chunkModelTimeoutRetries);
        }
        return Math.max(0, chunkModelMaxRetries);
    }

    private boolean isChunkFailureFatal(int chunkFailedCount, int totalChunks) {
        return totalChunks > 0
                && chunkFailedCount > 0
                && ((double) chunkFailedCount / totalChunks) >= chunkFailureFatalRatio;
    }

    private List<Map<String, Object>> failedChunks(List<Map<String, Object>> warnings) {
        List<Map<String, Object>> failedChunks = new ArrayList<>();
        for (Map<String, Object> warning : warnings) {
            if (!"chunk_audit_failed".equals(stringValue(warning.get("type")))) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("source_chunk_id", warning.get("source_chunk_id"));
            item.put("source_chunk_no", warning.get("source_chunk_no"));
            item.put("error_code", warning.get("error_code"));
            item.put("message", warning.get("message"));
            item.put("retry_count", warning.get("retry_count"));
            item.put("final_status", warning.get("final_status"));
            failedChunks.add(item);
        }
        return failedChunks;
    }

    private void enrichFindingLocation(Map<String, Object> finding, ContentChunk chunk) {
        Object rawLocation = finding.get("location");
        Map<String, Object> location = mapValue(rawLocation);
        String locationText = rawLocation instanceof Map<?, ?> ? "" : stringValue(rawLocation);

        Integer page = positiveInt(firstNotBlank(
                location.get("page"),
                location.get("pageNo"),
                location.get("page_no"),
                finding.get("page"),
                finding.get("pageNo"),
                finding.get("page_no")));
        if (page == null && chunk.getPageNo() != null && chunk.getPageNo() > 0) {
            page = chunk.getPageNo();
        }
        if (page != null) {
            location.put("page", page);
            location.put("pageNo", page);
            location.put("page_no", page);
            finding.put("page", page);
            finding.put("pageNo", page);
            finding.put("page_no", page);
        }

        String section = firstNotBlank(
                location.get("section"),
                finding.get("section"),
                chunk.getSectionTitle(),
                chunk.getSectionPath(),
                locationText);
        if (!section.isBlank()) {
            location.put("section", section);
        }

        String quote = firstNotBlank(location.get("quote"), finding.get("quote"));
        if (!quote.isBlank()) {
            location.put("quote", quote);
        }

        location.put("source_chunk_id", chunk.getSourceChunkId());
        location.put("source_chunk_no", chunk.getChunkNo());
        if (location.isEmpty()) {
            location.put("section", chunk.getChunkNo() == null ? "未明确位置" : "片段" + chunk.getChunkNo());
        }
        finding.put("location", location);
    }

    private boolean canSelectMoreReferences(int selectedCount, int selectedChars) {
        return (!isReferenceCountLimited() || selectedCount < maxReferenceCount)
                && (!isReferenceCharsLimited() || selectedChars < maxReferenceChars);
    }

    private boolean isReferenceCountLimited() {
        return !includeAllReferences && maxReferenceCount > 0;
    }

    private boolean isReferenceCharsLimited() {
        return maxReferenceChars > 0;
    }

    private int estimateReferenceChars(RetrievalReference reference) {
        return 160 + stringValue(reference.getChunkTextSnapshot()).length()
                + stringValue(reference.getFileName()).length()
                + stringValue(reference.getSectionTitle()).length();
    }

    private List<Long> longList(Object value) {
        List<Long> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    result.add(Long.valueOf(String.valueOf(item)));
                }
            }
        }
        return result;
    }

    private List<Map<String, Object>> listOfMaps(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                result.add(mapValue(item));
            }
        }
        return result;
    }

    private int countResultFindings(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return 0;
        }
        Object totalIssues = firstNotBlank(result.get("totalIssues"), result.get("total_issues"));
        if (!stringValue(totalIssues).isBlank()) {
            return intValue(totalIssues);
        }
        List<Map<String, Object>> findings = listOfMaps(result.get("findings"));
        if (!findings.isEmpty()) {
            return findings.size();
        }
        return listOfMaps(result.get("issues")).size();
    }

    private int countResultFindings(List<Map<String, Object>> results) {
        int count = 0;
        for (Map<String, Object> result : results) {
            count += countResultFindings(result);
        }
        return count;
    }

    private Map<String, Object> mapValue(Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> rawMap) {
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() != null) {
                    map.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }
        return map;
    }

    private int intValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private Integer positiveInt(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(String.valueOf(value).trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String firstNotBlank(Object... values) {
        for (Object value : values) {
            String text = stringValue(value).trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private String productName(WorkflowTaskContext context) {
        Map<String, Object> metadata = mapValue(context.getInput().get("metadata"));
        return firstNotBlank(
                context.getInput().get("product_name"),
                context.getInput().get("productName"),
                metadata.get("product_name"),
                metadata.get("productName"));
    }

    private String abbreviate(String value, String fallback, int maxLength) {
        String text = value == null || value.isBlank() ? fallback : value.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private Map<String, Object> parseModelJson(String content) {
        String json = stripJson(content);
        try {
            return jsonSupport.toMap(json);
        } catch (Exception ex) {
            String repairedJson = repairModelJson(json);
            try {
                return jsonSupport.toMap(repairedJson);
            } catch (Exception repairEx) {
                throw new BusinessException("MODEL_RESPONSE_INVALID",
                        "model response is not valid json: " + abbreviate(repairEx.getMessage(), "", 200));
            }
        }
    }

    private String repairModelJson(String json) {
        String text = json == null ? "" : json;
        text = text.replace('\u201c', '"')
                .replace('\u201d', '"')
                .replace('\u2018', '\'')
                .replace('\u2019', '\'');
        text = text.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        text = text.replaceAll(",\\s*([}\\]])", "$1");
        return escapeInnerQuotes(text);
    }

    private String escapeInnerQuotes(String json) {
        StringBuilder builder = new StringBuilder(json.length() + 32);
        boolean inString = false;
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (ch == '"' && !isEscaped(json, i)) {
                if (!inString) {
                    inString = true;
                    builder.append(ch);
                } else if (isClosingJsonQuote(json, i)) {
                    inString = false;
                    builder.append(ch);
                } else {
                    builder.append('\\').append(ch);
                }
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private boolean isClosingJsonQuote(String json, int quoteIndex) {
        for (int i = quoteIndex + 1; i < json.length(); i++) {
            char next = json.charAt(i);
            if (Character.isWhitespace(next)) {
                continue;
            }
            return next == ':' || next == ',' || next == '}' || next == ']';
        }
        return true;
    }

    private boolean isEscaped(String text, int index) {
        int slashCount = 0;
        for (int i = index - 1; i >= 0 && text.charAt(i) == '\\'; i--) {
            slashCount++;
        }
        return slashCount % 2 == 1;
    }

    private boolean isBusinessReportFindingsMode(AuditWorkflowNode node) {
        Map<String, Object> config = jsonSupport.toMap(node.getNodeConfig());
        return "business_report_findings".equals(String.valueOf(config.get("audit_mode")));
    }

    private boolean isUploadedBasisWorkflow(WorkflowTaskContext context) {
        return "uploaded_basis_document_audit".equals(context.getTask().getWorkflowCode());
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private record ReferenceSelection(List<RetrievalReference> references, Map<String, Object> summary) {
    }

    private record ChunkAuditOutput(int totalReferenceCount,
                                    int referencesUsedInPrompt,
                                    Long coveredSourceChunkId,
                                    int modelCallCount,
                                    int inputTokens,
                                    int outputTokens,
                                    long durationMs,
                                    boolean success,
                                    List<Map<String, Object>> findings,
                                    List<Map<String, Object>> warnings,
                                    Map<String, Object> referenceUsage) {
    }

    private String stripJson(String content) {
        if (content == null) {
            return "";
        }
        String text = content.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json\\s*", "").replaceFirst("^```\\s*", "");
            int end = text.lastIndexOf("```");
            if (end >= 0) {
                text = text.substring(0, end);
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}
