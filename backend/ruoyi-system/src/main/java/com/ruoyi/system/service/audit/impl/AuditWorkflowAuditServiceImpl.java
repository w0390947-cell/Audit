package com.ruoyi.system.service.audit.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.AuditWorkflowProperties;
import com.ruoyi.system.domain.audit.AuditAiFlowStage;
import com.ruoyi.system.domain.audit.AuditAiReportPreview;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.FastGptAuditFinding;
import com.ruoyi.system.domain.audit.FastGptAuditResult;
import com.ruoyi.system.domain.audit.workflow.AuditWorkflowCallback;
import com.ruoyi.system.domain.audit.workflow.AuditWorkflowCallbackEvent;
import com.ruoyi.system.domain.audit.workflow.AuditWorkflowStage;
import com.ruoyi.system.exception.AuditWorkflowException;
import com.ruoyi.system.mapper.audit.AuditAiFlowStageMapper;
import com.ruoyi.system.mapper.audit.AuditAiMapper;
import com.ruoyi.system.mapper.audit.AuditWorkflowCallbackEventMapper;
import com.ruoyi.system.service.audit.AuditAiAnalysisPersistenceService;
import com.ruoyi.system.service.audit.AuditAiReportPreviewService;
import com.ruoyi.system.service.audit.IAuditWorkflowAuditService;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuditWorkflowAuditServiceImpl implements IAuditWorkflowAuditService
{
    private static final Logger log = LoggerFactory.getLogger(AuditWorkflowAuditServiceImpl.class);

    private static final String BIZ_ID_PREFIX = "AI-TASK-";

    private static final Pattern PAGE_TEXT_PATTERN = Pattern.compile("(?:第\\s*)?(\\d+)\\s*页");

    private static final DateTimeFormatter NORMAL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuditWorkflowProperties properties;

    private final RestTemplate auditWorkflowRestTemplate;

    private final AuditAiMapper auditAiMapper;

    private final AuditAiFlowStageMapper auditAiFlowStageMapper;

    private final AuditWorkflowCallbackEventMapper callbackEventMapper;

    private final AuditAiAnalysisPersistenceService persistenceService;

    private final AuditAiReportPreviewService reportPreviewService;

    private final AuditAiQueuePositionService auditAiQueuePositionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditWorkflowAuditServiceImpl(AuditWorkflowProperties properties,
            @Qualifier("auditWorkflowRestTemplate") RestTemplate auditWorkflowRestTemplate,
            AuditAiMapper auditAiMapper, AuditAiFlowStageMapper auditAiFlowStageMapper,
            AuditWorkflowCallbackEventMapper callbackEventMapper,
            AuditAiAnalysisPersistenceService persistenceService,
            AuditAiReportPreviewService reportPreviewService,
            AuditAiQueuePositionService auditAiQueuePositionService)
    {
        this.properties = properties;
        this.auditWorkflowRestTemplate = auditWorkflowRestTemplate;
        this.auditAiMapper = auditAiMapper;
        this.auditAiFlowStageMapper = auditAiFlowStageMapper;
        this.callbackEventMapper = callbackEventMapper;
        this.persistenceService = persistenceService;
        this.reportPreviewService = reportPreviewService;
        this.auditAiQueuePositionService = auditAiQueuePositionService;
    }

    @Override
    public boolean isEnabled()
    {
        return properties.isEnabled();
    }

    @Override
    public FastGptAuditResult analyze(AuditAiTask task, String operator)
    {
        if (!properties.isEnabled())
        {
            throw new AuditWorkflowException("AI审核工作流未启用");
        }
        long startMs = System.currentTimeMillis();
        JsonNode createdTask = createTask(task, operator);
        Long workflowTaskId = createdTask.path("taskId").asLong(0L);
        String workflowTaskNo = createdTask.path("taskNo").asText("");
        if (workflowTaskId == null || workflowTaskId <= 0)
        {
            throw new AuditWorkflowException("工作流任务创建成功但未返回 taskId");
        }
        log.info("Audit workflow task created, aiTaskId={}, workflowTaskId={}, workflowTaskNo={}",
                task.getAiTaskId(), workflowTaskId, workflowTaskNo);

        JsonNode status = pollUntilFinished(workflowTaskId);
        String taskStatus = status.path("taskStatus").asText("");
        if (!"SUCCESS".equalsIgnoreCase(taskStatus))
        {
            String errorCode = status.path("errorCode").asText("");
            String errorMsg = status.path("errorMsg").asText("");
            throw new AuditWorkflowException("工作流任务失败：" + StringUtils.defaultIfBlank(errorCode, taskStatus)
                    + " " + errorMsg);
        }

        FastGptAuditResult result = fetchAndMapResult(workflowTaskId);
        result.setChatId(StringUtils.defaultIfBlank(workflowTaskNo, String.valueOf(workflowTaskId)));
        result.setElapsedMs(System.currentTimeMillis() - startMs);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(AuditWorkflowCallback callback, String authorization)
    {
        validateCallbackToken(authorization);
        if (callback == null || StringUtils.isBlank(callback.getBizId()))
        {
            throw new AuditWorkflowException("工作流回调缺少 biz_id");
        }
        Long aiTaskId = parseAiTaskId(callback.getBizId());
        AuditAiTask task = auditAiMapper.selectAuditAiTaskById(aiTaskId);
        if (task == null)
        {
            throw new AuditWorkflowException("回调对应的AI任务不存在：" + callback.getBizId());
        }
        String workflowTaskId = resolveWorkflowTaskId(callback);
        String runId = StringUtils.defaultIfBlank(workflowTaskId, fallbackRunId(callback, aiTaskId));
        String callbackEventId = resolveCallbackEventId(callback, runId);
        if (!tryMarkCallbackProcessing(callbackEventId, aiTaskId, workflowTaskId, toJson(callback)))
        {
            log.info("Duplicate audit workflow callback ignored, callbackEventId={}, aiTaskId={}",
                    callbackEventId, aiTaskId);
            return;
        }

        try
        {
            if (isCallbackSuccess(callback))
            {
                FastGptAuditResult result = resolveCallbackResult(callback);
                result.setChatId(StringUtils.defaultIfBlank(resolveWorkflowTaskNo(callback), runId));
                boolean increaseAnalysisCount = !"completed".equals(task.getTaskStatus());
                saveFlowStages(aiTaskId, runId, workflowTaskId, resolveWorkflowTaskNo(callback), callback,
                        "workflow-callback");
                persistenceService.saveAuditResult(task, result, "workflow-callback", increaseAnalysisCount);
                callbackEventMapper.updateAuditWorkflowCallbackEventStatus(callbackEventId, "processed", null);
                return;
            }

            saveFlowStages(aiTaskId, runId, workflowTaskId, resolveWorkflowTaskNo(callback), callback,
                    "workflow-callback");
            String errorMsg = buildCallbackErrorMessage(callback);
            auditAiMapper.updateAuditAiAnalysisFailure(aiTaskId, "AI审核工作流失败：" + errorMsg, "workflow-callback");
            auditAiQueuePositionService.resortQueuePositions("workflow-callback");
            callbackEventMapper.updateAuditWorkflowCallbackEventStatus(callbackEventId, "processed", null);
        }
        catch (RuntimeException e)
        {
            callbackEventMapper.updateAuditWorkflowCallbackEventStatus(callbackEventId, "failed",
                    truncate(e.getMessage(), 1000));
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleStageCallback(AuditWorkflowCallback callback, String authorization)
    {
        validateCallbackToken(authorization);
        if (callback == null || StringUtils.isBlank(callback.getBizId()))
        {
            throw new AuditWorkflowException("工作流阶段回调缺少 biz_id");
        }
        Long aiTaskId = parseAiTaskId(callback.getBizId());
        AuditAiTask task = auditAiMapper.selectAuditAiTaskById(aiTaskId);
        if (task == null)
        {
            throw new AuditWorkflowException("阶段回调对应的AI任务不存在：" + callback.getBizId());
        }
        if (isTerminalTaskStatus(task.getTaskStatus()))
        {
            log.info("Audit workflow stage callback ignored for terminal task, aiTaskId={}, taskStatus={}",
                    aiTaskId, task.getTaskStatus());
            return;
        }

        String workflowTaskId = resolveWorkflowTaskId(callback);
        String runId = StringUtils.defaultIfBlank(workflowTaskId, fallbackRunId(callback, aiTaskId));
        String callbackEventId = resolveCallbackEventId(callback, runId);
        if (!tryMarkCallbackProcessing(callbackEventId, aiTaskId, workflowTaskId, toJson(callback)))
        {
            log.info("Duplicate audit workflow stage callback ignored, callbackEventId={}, aiTaskId={}",
                    callbackEventId, aiTaskId);
            return;
        }

        try
        {
            saveFlowStageProgress(aiTaskId, runId, workflowTaskId, resolveWorkflowTaskNo(callback), callback,
                    "workflow-stage-callback");
            updateStageCallbackTaskProgress(aiTaskId, callback, "workflow-stage-callback");
            callbackEventMapper.updateAuditWorkflowCallbackEventStatus(callbackEventId, "processed", null);
        }
        catch (RuntimeException e)
        {
            callbackEventMapper.updateAuditWorkflowCallbackEventStatus(callbackEventId, "failed",
                    truncate(e.getMessage(), 1000));
            throw e;
        }
    }

    private boolean tryMarkCallbackProcessing(String callbackEventId, Long aiTaskId, String workflowTaskId,
            String rawPayload)
    {
        AuditWorkflowCallbackEvent event = new AuditWorkflowCallbackEvent();
        event.setCallbackEventId(callbackEventId);
        event.setAiTaskId(aiTaskId);
        event.setWorkflowTaskId(workflowTaskId);
        event.setEventStatus("processing");
        event.setRawPayload(rawPayload);
        try
        {
            callbackEventMapper.insertAuditWorkflowCallbackEvent(event);
            return true;
        }
        catch (DuplicateKeyException e)
        {
            return callbackEventMapper.retryFailedAuditWorkflowCallbackEvent(callbackEventId, aiTaskId,
                    workflowTaskId, rawPayload) > 0;
        }
    }

    private FastGptAuditResult resolveCallbackResult(AuditWorkflowCallback callback)
    {
        if (callback.getResult() != null && !callback.getResult().isEmpty())
        {
            return mapResultData(objectMapper.valueToTree(callback.getResult()));
        }
        Long workflowTaskId = callback.getTaskId();
        if (workflowTaskId == null && StringUtils.isNotBlank(callback.getResultUrl()))
        {
            workflowTaskId = parseTaskIdFromResultUrl(callback.getResultUrl());
        }
        if (workflowTaskId == null)
        {
            throw new AuditWorkflowException("工作流成功回调缺少 result 或 task_id");
        }
        return fetchAndMapResult(workflowTaskId);
    }

    private void saveFlowStages(Long aiTaskId, String runId, String workflowTaskId, String workflowTaskNo,
            AuditWorkflowCallback callback, String operator)
    {
        List<AuditAiFlowStage> stages = mapFlowStages(aiTaskId, runId, workflowTaskId, workflowTaskNo,
                callback, operator, true);
        if (stages.isEmpty())
        {
            return;
        }
        auditAiFlowStageMapper.deleteAuditAiFlowStageByTaskIdAndRunId(aiTaskId, runId);
        auditAiFlowStageMapper.insertAuditAiFlowStageBatch(stages);
    }

    private void saveFlowStageProgress(Long aiTaskId, String runId, String workflowTaskId, String workflowTaskNo,
            AuditWorkflowCallback callback, String operator)
    {
        List<AuditAiFlowStage> incomingStages = mapFlowStages(aiTaskId, runId, workflowTaskId, workflowTaskNo,
                callback, operator, false);
        if (incomingStages.isEmpty())
        {
            return;
        }
        List<AuditAiFlowStage> existingStages =
                auditAiFlowStageMapper.selectAuditAiFlowStageListByTaskIdAndRunId(aiTaskId, runId);
        List<AuditAiFlowStage> mergedStages = mergeFlowStages(existingStages, incomingStages);
        auditAiFlowStageMapper.deleteAuditAiFlowStageByTaskIdAndRunId(aiTaskId, runId);
        auditAiFlowStageMapper.insertAuditAiFlowStageBatch(mergedStages);
    }

    private List<AuditAiFlowStage> mergeFlowStages(List<AuditAiFlowStage> existingStages,
            List<AuditAiFlowStage> incomingStages)
    {
        Map<String, AuditAiFlowStage> stageMap = new LinkedHashMap<>();
        for (AuditAiFlowStage stage : existingStages)
        {
            stageMap.put(flowStageMergeKey(stage), stage);
        }
        for (AuditAiFlowStage stage : incomingStages)
        {
            stageMap.put(flowStageMergeKey(stage), stage);
        }
        return new ArrayList<>(stageMap.values());
    }

    private String flowStageMergeKey(AuditAiFlowStage stage)
    {
        String key = StringUtils.defaultIfBlank(stage.getStageInstanceId(), stage.getStageCode());
        return StringUtils.defaultIfBlank(key, stage.getStageName());
    }

    private void updateStageCallbackTaskProgress(Long aiTaskId, AuditWorkflowCallback callback, String operator)
    {
        String progressText = StringUtils.defaultIfBlank(callback.getProgressText(), "工作流执行中");
        auditAiMapper.updateAuditAiTaskStageProgress(aiTaskId, callback.getProgressPercent(), progressText, operator);
    }

    private List<AuditAiFlowStage> mapFlowStages(Long aiTaskId, String runId, String workflowTaskId,
            String workflowTaskNo, AuditWorkflowCallback callback, String operator, boolean buildFailureFallback)
    {
        List<AuditWorkflowStage> source = callback.getStages();
        if (buildFailureFallback && (source == null || source.isEmpty()) && !isCallbackSuccess(callback))
        {
            source = Collections.singletonList(buildFallbackFailedStage(callback));
        }
        if (source == null || source.isEmpty())
        {
            return Collections.emptyList();
        }
        List<AuditAiFlowStage> stages = new ArrayList<>();
        int index = 1;
        for (AuditWorkflowStage item : source)
        {
            if (item == null)
            {
                continue;
            }
            AuditAiFlowStage stage = new AuditAiFlowStage();
            stage.setAiTaskId(aiTaskId);
            stage.setRunId(runId);
            stage.setWorkflowTaskId(workflowTaskId);
            stage.setWorkflowTaskNo(workflowTaskNo);
            stage.setStageCode(StringUtils.defaultIfBlank(item.getStageCode(), "stage_" + index));
            stage.setStageInstanceId(item.getStageInstanceId());
            stage.setStageName(StringUtils.defaultIfBlank(item.getStageName(), stage.getStageCode()));
            stage.setStageStatus(normalizeStageStatus(item.getStageStatus()));
            stage.setAgentName(item.getAgentName());
            stage.setStartTime(parseWorkflowTime(item.getStartedAt()));
            stage.setEndTime(parseWorkflowTime(item.getFinishedAt()));
            stage.setDurationMs(item.getDurationMs());
            stage.setStageSummary(truncate(item.getSummary(), 500));
            stage.setStageDetail(item.getDetail());
            stage.setOutputJson(toJson(item.getOutput()));
            stage.setErrorMessage(truncate(toTextOrJson(objectMapper.valueToTree(item.getError())), 1000));
            stage.setSortNum(item.getSortNum() == null ? index * 10 : item.getSortNum());
            stage.setCreateBy(operator);
            stage.setUpdateBy(operator);
            stages.add(stage);
            index++;
        }
        return stages;
    }

    private AuditWorkflowStage buildFallbackFailedStage(AuditWorkflowCallback callback)
    {
        AuditWorkflowStage stage = new AuditWorkflowStage();
        JsonNode error = objectMapper.valueToTree(callback.getError());
        stage.setStageCode(StringUtils.defaultIfBlank(error.path("stage_code").asText(""), "workflow_failed"));
        stage.setStageName("工作流执行失败");
        stage.setStageStatus("failed");
        stage.setStartedAt(callback.getStartedAt());
        stage.setFinishedAt(callback.getFinishedAt());
        stage.setDurationMs(callback.getDurationMs());
        stage.setSummary(buildCallbackErrorMessage(callback));
        stage.setDetail(toTextOrJson(error.path("detail")));
        stage.setError(callback.getError());
        stage.setSortNum(90);
        return stage;
    }

    private JsonNode createTask(AuditAiTask task, String operator)
    {
        String endpoint = properties.getBaseUrl() + "/api/audit/tasks";
        Map<String, Object> body = new HashMap<>();
        body.put("workflow_code", selectWorkflowCode(task));
        body.put("biz_id", BIZ_ID_PREFIX + task.getAiTaskId());
        if (StringUtils.isNotBlank(properties.getCallbackUrl()))
        {
            body.put("callback_url", properties.getCallbackUrl());
        }
        body.put("input", buildInput(task, operator));
        JsonNode response = exchangeJson(endpoint, HttpMethod.POST, body);
        assertSuccess(response, "创建工作流任务失败");
        return response.path("data");
    }

    private Map<String, Object> buildInput(AuditAiTask task, String operator)
    {
        if (StringUtils.isBlank(task.getReportFileUrl()))
        {
            throw new AuditWorkflowException("报告文件URL为空，无法创建工作流任务");
        }
        Map<String, Object> input = new HashMap<>();
        input.put("file_id", String.valueOf(task.getAiTaskId()));
        input.put("file_url", toPublicFileUrl(task.getReportFileUrl()));
        input.put("file_name", StringUtils.defaultIfBlank(task.getReportFileName(), task.getProductName()));
        String fileType = extractFileType(task.getReportFileName(), task.getReportFileUrl());
        input.put("file_type", fileType);
        Map<String, Object> metadata = buildMetadata(task);
        String previewPdfUrl = resolvePreviewPdfUrl(task, fileType);
        if (StringUtils.isNotBlank(previewPdfUrl))
        {
            input.put("preview_pdf_url", previewPdfUrl);
            metadata.put("preview_pdf_url", previewPdfUrl);
        }
        input.put("metadata", metadata);
        if (hasBasisFiles(task))
        {
            input.put("basis_files", buildBasisFiles(task.getBasisFileUrls()));
        }
        else
        {
            input.put("knowledge_scope", buildKnowledgeScope());
        }
        input.put("caller_context", buildCallerContext(operator));
        return input;
    }

    private String resolvePreviewPdfUrl(AuditAiTask task, String fileType)
    {
        if (!"doc".equals(fileType) && !"docx".equals(fileType))
        {
            return "";
        }
        try
        {
            AuditAiReportPreview preview = reportPreviewService.getReportPreview(task.getAiTaskId());
            if (preview == null || StringUtils.isBlank(preview.getPreviewFileUrl()))
            {
                throw new AuditWorkflowException("报告预览PDF生成失败，未返回预览文件地址");
            }
            return toPublicFileUrl(preview.getPreviewFileUrl());
        }
        catch (AuditWorkflowException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new AuditWorkflowException("报告预览PDF生成失败：" + e.getMessage());
        }
    }

    private String selectWorkflowCode(AuditAiTask task)
    {
        if (hasBasisFiles(task))
        {
            return StringUtils.defaultIfBlank(properties.getUploadedBasisWorkflowCode(), properties.getWorkflowCode());
        }
        return properties.getWorkflowCode();
    }

    private boolean hasBasisFiles(AuditAiTask task)
    {
        return task != null && !splitFileUrls(task.getBasisFileUrls()).isEmpty();
    }

    private List<Map<String, Object>> buildBasisFiles(String basisFileUrls)
    {
        List<Map<String, Object>> basisFiles = new ArrayList<>();
        int index = 1;
        for (String fileUrl : splitFileUrls(basisFileUrls))
        {
            String publicFileUrl = toPublicFileUrl(fileUrl);
            String fallbackName = String.format("basis-file-%03d", index);
            String fileName = extractFileName(publicFileUrl, fallbackName);
            Map<String, Object> basisFile = new HashMap<>();
            basisFile.put("file_id", String.format("BASIS-%03d", index));
            basisFile.put("file_url", publicFileUrl);
            basisFile.put("file_name", fileName);
            basisFile.put("file_type", extractFileType(fileName, publicFileUrl));
            basisFiles.add(basisFile);
            index++;
        }
        return basisFiles;
    }

    private List<String> splitFileUrls(String fileUrls)
    {
        List<String> values = new ArrayList<>();
        if (StringUtils.isBlank(fileUrls))
        {
            return values;
        }
        String[] parts = fileUrls.split(",");
        for (String part : parts)
        {
            if (StringUtils.isNotBlank(part))
            {
                values.add(part.trim());
            }
        }
        return values;
    }

    private Map<String, Object> buildMetadata(AuditAiTask task)
    {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("business_type", "audit_review");
        metadata.put("review_task_id", task.getReviewTaskId());
        metadata.put("review_version_id", task.getReviewVersionId());
        metadata.put("product_name", StringUtils.defaultString(task.getProductName()));
        metadata.put("delivery_unit", StringUtils.defaultString(task.getDeliveryUnit()));
        metadata.put("submitter_id", StringUtils.defaultString(task.getSubmitter()));
        return metadata;
    }

    private Map<String, Object> buildKnowledgeScope()
    {
        Map<String, Object> scope = new HashMap<>();
        if (properties.getKnowledgeBaseCodes() != null && properties.getKnowledgeBaseCodes().length > 0)
        {
            scope.put("knowledge_base_codes", properties.getKnowledgeBaseCodes());
        }
        scope.put("effective_only", true);
        return scope;
    }

    private Map<String, Object> buildCallerContext(String operator)
    {
        Map<String, Object> context = new HashMap<>();
        context.put("user_id", StringUtils.defaultString(operator));
        context.put("permission_mode", StringUtils.defaultIfBlank(properties.getPermissionMode(), "explicit_scope"));
        return context;
    }

    private JsonNode pollUntilFinished(Long workflowTaskId)
    {
        long deadline = System.currentTimeMillis() + Math.max(1000, properties.getPollTimeoutMs());
        while (System.currentTimeMillis() < deadline)
        {
            JsonNode response = exchangeJson(properties.getBaseUrl() + "/api/audit/tasks/" + workflowTaskId,
                    HttpMethod.GET, null);
            assertSuccess(response, "查询工作流任务失败");
            JsonNode data = response.path("data");
            String status = data.path("taskStatus").asText("");
            if ("SUCCESS".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status)
                    || "CANCELED".equalsIgnoreCase(status))
            {
                return data;
            }
            sleep();
        }
        throw new AuditWorkflowException("工作流任务执行超时");
    }

    private FastGptAuditResult fetchAndMapResult(Long workflowTaskId)
    {
        JsonNode response = exchangeJson(properties.getBaseUrl() + "/api/audit/tasks/" + workflowTaskId + "/result",
                HttpMethod.GET, null);
        assertSuccess(response, "查询工作流审核结果失败");
        return mapResultData(response.path("data"));
    }

    private FastGptAuditResult mapResultData(JsonNode data)
    {
        FastGptAuditResult result = new FastGptAuditResult();
        result.setSuccess(true);
        result.setSummary(toTextOrJson(data.path("summary")));
        result.setFindings(mapFindings(data));
        result.setTotalIssues(result.getFindings().size());
        result.setRawContent(toJson(data));
        return result;
    }

    private List<FastGptAuditFinding> mapFindings(JsonNode data)
    {
        List<FastGptAuditFinding> findings = mapIssueArray(data.path("findings"));
        if (!findings.isEmpty())
        {
            return findings;
        }
        return mapIssueArray(data.path("issues"));
    }

    private List<FastGptAuditFinding> mapIssueArray(JsonNode issues)
    {
        List<FastGptAuditFinding> findings = new ArrayList<>();
        if (issues == null || !issues.isArray())
        {
            return findings;
        }
        for (JsonNode issue : issues)
        {
            FastGptAuditFinding finding = new FastGptAuditFinding();
            String severity = StringUtils.defaultIfBlank(issue.path("severity").asText(""),
                    issue.path("risk_level").asText(""));
            finding.setType(StringUtils.defaultIfBlank(issue.path("type").asText(""),
                    StringUtils.defaultIfBlank(issue.path("finding_type").asText(""), "AI审核问题")));
            finding.setTitle(StringUtils.defaultIfBlank(issue.path("title").asText(""),
                    StringUtils.defaultIfBlank(issue.path("finding_title").asText(""), "AI发现问题")));
            finding.setContent(StringUtils.defaultIfBlank(issue.path("content").asText(""),
                    StringUtils.defaultIfBlank(issue.path("finding_content").asText(""),
                            StringUtils.defaultIfBlank(issue.path("problem").asText(""),
                                    issue.path("title").asText("")))));
            finding.setSeverity(severity);
            finding.setLocation(toTextOrJson(issue.path("location")));
            finding.setPageNo(resolvePageNo(issue));
            finding.setLocationJson(toJson(issue.path("location")));
            finding.setSuggestion(issue.path("suggestion").asText(""));
            findings.add(finding);
        }
        return findings;
    }

    private boolean isCallbackSuccess(AuditWorkflowCallback callback)
    {
        return "SUCCESS".equalsIgnoreCase(callback.getTaskStatus())
                || "completed".equalsIgnoreCase(callback.getStatus());
    }

    private boolean isTerminalTaskStatus(String taskStatus)
    {
        return "completed".equals(taskStatus) || "failed".equals(taskStatus);
    }

    private String resolveWorkflowTaskId(AuditWorkflowCallback callback)
    {
        if (StringUtils.isNotBlank(callback.getWorkflowTaskId()))
        {
            return callback.getWorkflowTaskId();
        }
        if (callback.getTaskId() != null)
        {
            return String.valueOf(callback.getTaskId());
        }
        if (StringUtils.isNotBlank(callback.getResultUrl()))
        {
            Long taskId = parseTaskIdFromResultUrl(callback.getResultUrl());
            return taskId == null ? "" : String.valueOf(taskId);
        }
        return "";
    }

    private String resolveWorkflowTaskNo(AuditWorkflowCallback callback)
    {
        return StringUtils.defaultIfBlank(callback.getWorkflowTaskNo(), callback.getTaskNo());
    }

    private String fallbackRunId(AuditWorkflowCallback callback, Long aiTaskId)
    {
        String time = StringUtils.defaultIfBlank(callback.getFinishedAt(),
                StringUtils.defaultIfBlank(callback.getCallbackTime(), String.valueOf(System.currentTimeMillis())));
        return BIZ_ID_PREFIX + aiTaskId + "-" + time.replaceAll("[^0-9]", "");
    }

    private String resolveCallbackEventId(AuditWorkflowCallback callback, String runId)
    {
        if (StringUtils.isNotBlank(callback.getCallbackEventId()))
        {
            return callback.getCallbackEventId();
        }
        String status = StringUtils.defaultIfBlank(callback.getStatus(), callback.getTaskStatus());
        String time = StringUtils.defaultIfBlank(callback.getFinishedAt(),
                StringUtils.defaultIfBlank(callback.getCallbackTime(), ""));
        return runId + ":" + status + ":" + time;
    }

    private String buildCallbackErrorMessage(AuditWorkflowCallback callback)
    {
        JsonNode error = objectMapper.valueToTree(callback.getError());
        String message = StringUtils.defaultIfBlank(error.path("message").asText(""), callback.getProgressText());
        if (StringUtils.isBlank(message))
        {
            message = toTextOrJson(error);
        }
        return StringUtils.defaultIfBlank(truncate(message, 200), "工作流任务失败");
    }

    private String normalizeStageStatus(String status)
    {
        if (StringUtils.isBlank(status))
        {
            return "pending";
        }
        if ("SUCCESS".equalsIgnoreCase(status))
        {
            return "completed";
        }
        if ("FAILED".equalsIgnoreCase(status) || "ERROR".equalsIgnoreCase(status))
        {
            return "failed";
        }
        if ("RUNNING".equalsIgnoreCase(status))
        {
            return "running";
        }
        if ("CANCELED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status))
        {
            return "skipped";
        }
        return status.toLowerCase();
    }

    private Date parseWorkflowTime(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return null;
        }
        try
        {
            LocalDateTime localDateTime = LocalDateTime.parse(value.trim(), NORMAL_DATE_TIME_FORMATTER);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        catch (DateTimeParseException ignored)
        {
            try
            {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(value.trim());
                return Date.from(offsetDateTime.toInstant());
            }
            catch (DateTimeParseException e)
            {
                log.warn("Failed to parse workflow time: {}", value);
                return null;
            }
        }
    }

    private JsonNode exchangeJson(String endpoint, HttpMethod method, Object body)
    {
        try
        {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.isNotBlank(properties.getApiToken()))
            {
                headers.setBearerAuth(properties.getApiToken());
            }
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = auditWorkflowRestTemplate.exchange(endpoint, method, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
            {
                throw new AuditWorkflowException("工作流 HTTP 响应异常：" + response.getStatusCode());
            }
            return objectMapper.readTree(response.getBody());
        }
        catch (JsonProcessingException e)
        {
            throw new AuditWorkflowException("工作流响应不是合法 JSON", e);
        }
        catch (RestClientException e)
        {
            throw new AuditWorkflowException("工作流调用失败：" + e.getMessage(), e);
        }
    }

    private void assertSuccess(JsonNode response, String message)
    {
        if (response.path("code").asInt(500) != 200)
        {
            throw new AuditWorkflowException(message + "：" + response.path("message").asText(""));
        }
    }

    private void validateCallbackToken(String authorization)
    {
        if (StringUtils.isBlank(properties.getCallbackToken()))
        {
            return;
        }
        String expected = "Bearer " + properties.getCallbackToken();
        if (!expected.equals(authorization))
        {
            throw new AuditWorkflowException("工作流回调 Token 无效");
        }
    }

    private Long parseAiTaskId(String bizId)
    {
        if (!bizId.startsWith(BIZ_ID_PREFIX))
        {
            throw new AuditWorkflowException("不支持的 biz_id：" + bizId);
        }
        return Long.valueOf(bizId.substring(BIZ_ID_PREFIX.length()));
    }

    private Long parseTaskIdFromResultUrl(String resultUrl)
    {
        String marker = "/api/audit/tasks/";
        int start = resultUrl.indexOf(marker);
        if (start < 0)
        {
            return null;
        }
        String tail = resultUrl.substring(start + marker.length());
        int slash = tail.indexOf('/');
        return Long.valueOf(slash < 0 ? tail : tail.substring(0, slash));
    }

    private String toPublicFileUrl(String fileUrl)
    {
        if (StringUtils.isBlank(fileUrl) || fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))
        {
            return fileUrl;
        }
        if (StringUtils.isNotBlank(properties.getPublicFileBaseUrl()) && fileUrl.startsWith("/profile/"))
        {
            return properties.getPublicFileBaseUrl() + fileUrl;
        }
        return fileUrl;
    }

    private String extractFileType(String fileName, String fileUrl)
    {
        String value = StringUtils.defaultIfBlank(fileName, fileUrl);
        int index = value == null ? -1 : value.lastIndexOf('.');
        if (index < 0 || index == value.length() - 1)
        {
            return "";
        }
        return value.substring(index + 1).toLowerCase();
    }

    private String extractFileName(String fileUrl, String fallback)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return fallback;
        }
        String value = fileUrl;
        int queryIndex = value.indexOf('?');
        if (queryIndex >= 0)
        {
            value = value.substring(0, queryIndex);
        }
        int slashIndex = value.lastIndexOf('/');
        if (slashIndex >= 0 && slashIndex < value.length() - 1)
        {
            return value.substring(slashIndex + 1);
        }
        return StringUtils.defaultIfBlank(value, fallback);
    }

    private String toJson(JsonNode node)
    {
        if (node == null || node.isMissingNode() || node.isNull())
        {
            return "";
        }
        try
        {
            return objectMapper.writeValueAsString(node);
        }
        catch (JsonProcessingException e)
        {
            return node.asText("");
        }
    }

    private String toJson(Object value)
    {
        if (value == null)
        {
            return "";
        }
        try
        {
            return objectMapper.writeValueAsString(value);
        }
        catch (JsonProcessingException e)
        {
            return String.valueOf(value);
        }
    }

    private String toTextOrJson(JsonNode node)
    {
        if (node == null || node.isMissingNode() || node.isNull())
        {
            return "";
        }
        if (node.isTextual() || node.isNumber() || node.isBoolean())
        {
            return node.asText("");
        }
        return toJson(node);
    }

    private Integer resolvePageNo(JsonNode issue)
    {
        JsonNode locationNode = issue.path("location");
        Integer pageNo = firstPositiveInt(locationNode.path("page"), locationNode.path("pageNo"),
                locationNode.path("page_no"), issue.path("page"), issue.path("pageNo"), issue.path("page_no"));
        if (pageNo != null)
        {
            return pageNo;
        }
        return parsePageNoFromText(toTextOrJson(locationNode));
    }

    private Integer firstPositiveInt(JsonNode... nodes)
    {
        for (JsonNode node : nodes)
        {
            Integer value = toPositiveInt(node);
            if (value != null)
            {
                return value;
            }
        }
        return null;
    }

    private Integer toPositiveInt(JsonNode node)
    {
        if (node == null || node.isMissingNode() || node.isNull())
        {
            return null;
        }
        if (node.isInt() || node.isLong())
        {
            int value = node.asInt();
            return value > 0 ? value : null;
        }
        String text = node.asText("");
        if (StringUtils.isBlank(text))
        {
            return null;
        }
        try
        {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : null;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private Integer parsePageNoFromText(String text)
    {
        if (StringUtils.isBlank(text))
        {
            return null;
        }
        Matcher matcher = PAGE_TEXT_PATTERN.matcher(text);
        if (!matcher.find())
        {
            return null;
        }
        try
        {
            int value = Integer.parseInt(matcher.group(1));
            return value > 0 ? value : null;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private String truncate(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private void sleep()
    {
        try
        {
            Thread.sleep(Math.max(1000, properties.getPollIntervalMs()));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new AuditWorkflowException("等待工作流任务结果时被中断", e);
        }
    }
}
