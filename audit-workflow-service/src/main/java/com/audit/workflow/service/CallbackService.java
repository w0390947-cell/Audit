package com.audit.workflow.service;

import com.audit.workflow.domain.AuditTask;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.CallbackLog;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.repository.AuditCallbackLogRepository;
import com.audit.workflow.repository.AuditResultRepository;
import com.audit.workflow.repository.AuditTaskNodeLogRepository;
import com.audit.workflow.support.JsonSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CallbackService {

    private final AuditCallbackLogRepository callbackLogRepository;
    private final AuditResultRepository resultRepository;
    private final AuditTaskNodeLogRepository nodeLogRepository;
    private final JsonSupport jsonSupport;
    private final HttpClient httpClient;
    private final boolean enabled;
    private final String callbackToken;
    private final int timeoutSeconds;

    public CallbackService(AuditCallbackLogRepository callbackLogRepository,
                           AuditResultRepository resultRepository,
                           AuditTaskNodeLogRepository nodeLogRepository,
                           JsonSupport jsonSupport,
                           @Value("${audit.callback.enabled:true}") boolean enabled,
                           @Value("${audit.callback.token:}") String callbackToken,
                           @Value("${audit.callback.timeout-seconds:20}") int timeoutSeconds) {
        this.callbackLogRepository = callbackLogRepository;
        this.resultRepository = resultRepository;
        this.nodeLogRepository = nodeLogRepository;
        this.jsonSupport = jsonSupport;
        this.enabled = enabled;
        this.callbackToken = callbackToken;
        this.timeoutSeconds = timeoutSeconds;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    public Map<String, Object> sendFinalCallback(AuditTask task, String finalStatus, String errorCode, String errorMsg) {
        Map<String, Object> result = resultRepository.getResultJson(task.getTaskId());
        Map<String, Object> payload = buildPayload(task, finalStatus, result, errorCode, errorMsg);
        if (!enabled || task.getCallbackUrl() == null || task.getCallbackUrl().isBlank()) {
            return Map.of("callback_status", "SKIPPED");
        }
        Long callbackId = callbackLogRepository.insertPending(task.getTaskId(), task.getTaskNo(), task.getCallbackUrl(), jsonSupport.toJson(payload));
        send(callbackId, task.getCallbackUrl(), jsonSupport.toJson(payload), task.getTaskNo());
        return Map.of("callback_status", "SUBMITTED", "callback_id", callbackId);
    }

    public Map<String, Object> sendStageCallback(AuditTask task, AuditWorkflowNode node, String stageStatus,
                                                 NodeExecutionResult result, LocalDateTime startedAt, long durationMs) {
        if (!enabled || task.getCallbackUrl() == null || task.getCallbackUrl().isBlank()) {
            return Map.of("callback_status", "SKIPPED");
        }
        String stageCallbackUrl = stageCallbackUrl(task.getCallbackUrl());
        Map<String, Object> payload = buildStagePayload(task, node, stageStatus, result, startedAt, durationMs);
        Long callbackId = callbackLogRepository.insertPending(task.getTaskId(), task.getTaskNo(), stageCallbackUrl, jsonSupport.toJson(payload));
        send(callbackId, stageCallbackUrl, jsonSupport.toJson(payload), task.getTaskNo());
        return Map.of("callback_status", "SUBMITTED", "callback_id", callbackId);
    }

    public int retryDueCallbacks(int limit) {
        List<CallbackLog> logs = callbackLogRepository.findRetryable(limit);
        for (CallbackLog log : logs) {
            send(log.getCallbackId(), log.getCallbackUrl(), log.getRequestPayload(), log.getTaskNo());
        }
        return logs.size();
    }

    public int retryTaskCallbacks(Long taskId) {
        List<CallbackLog> logs = callbackLogRepository.findFailedByTaskId(taskId);
        for (CallbackLog log : logs) {
            send(log.getCallbackId(), log.getCallbackUrl(), log.getRequestPayload(), log.getTaskNo());
        }
        return logs.size();
    }

    private void send(Long callbackId, String callbackUrl, String payload, String taskNo) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(callbackUrl))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("X-Request-Id", "CALLBACK-" + callbackId)
                    .header("X-Audit-Task-No", taskNo == null ? "" : taskNo)
                    .POST(HttpRequest.BodyPublishers.ofString(payload));
            if (callbackToken != null && !callbackToken.isBlank()) {
                builder.header("Authorization", "Bearer " + callbackToken);
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                callbackLogRepository.markSuccess(callbackId, response.statusCode(), response.body());
            } else {
                callbackLogRepository.markFailed(callbackId, response.statusCode(), response.body(), "http status " + response.statusCode());
            }
        } catch (Exception ex) {
            callbackLogRepository.markFailed(callbackId, null, null, ex.getMessage());
        }
    }

    private Map<String, Object> buildPayload(AuditTask task, String finalStatus, Map<String, Object> result,
                                             String errorCode, String errorMsg) {
        boolean success = "SUCCESS".equalsIgnoreCase(finalStatus);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("callback_event_id", callbackEventId(task, success ? "success" : "failed"));
        payload.put("callback_time", nowIso());
        payload.put("task_id", task.getTaskId());
        payload.put("task_no", task.getTaskNo());
        payload.put("workflow_code", task.getWorkflowCode());
        payload.put("workflow_name", task.getWorkflowCode());
        payload.put("workflow_task_id", String.valueOf(task.getTaskId()));
        payload.put("workflow_task_no", task.getTaskNo());
        payload.put("biz_id", task.getBizId());
        payload.put("task_status", finalStatus);
        payload.put("status", success ? "completed" : "failed");
        payload.put("progress_percent", success ? 100 : 0);
        payload.put("progress_text", success ? "AI审核工作流执行完成" : "AI审核工作流失败");
        payload.put("started_at", formatTime(task.getStartTime()));
        payload.put("summary", result == null ? null : result.get("summary"));
        payload.put("result_url", success ? "/api/audit/tasks/" + task.getTaskId() + "/result" : null);
        payload.put("finished_at", nowIso());
        payload.put("duration_ms", durationMs(task.getStartTime(), LocalDateTime.now()));
        List<Map<String, Object>> stages = buildFinalStages(task, finalStatus, errorCode, errorMsg);
        String failedStageCode = failedStageCode(stages, failureStageCode(task));
        payload.put("stages", stages);
        payload.put("result", success ? result : null);
        if (success) {
            payload.put("error", null);
        } else {
            payload.put("error", errorBody(errorCode, errorMsg, failedStageCode));
        }
        return payload;
    }

    private Map<String, Object> buildStagePayload(AuditTask task, AuditWorkflowNode node, String stageStatus,
                                                  NodeExecutionResult result, LocalDateTime startedAt, long durationMs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("callback_event_id", callbackEventId(task, "stage-" + node.getNodeCode() + "-" + stageStatus.toLowerCase()));
        payload.put("callback_time", nowIso());
        payload.put("biz_id", task.getBizId());
        payload.put("workflow_code", task.getWorkflowCode());
        payload.put("workflow_name", task.getWorkflowCode());
        payload.put("workflow_task_id", String.valueOf(task.getTaskId()));
        payload.put("workflow_task_no", task.getTaskNo());
        payload.put("task_status", "RUNNING");
        payload.put("status", "running");
        payload.put("progress_percent", progressPercent(node));
        payload.put("progress_text", stageProgressText(node, stageStatus));
        payload.put("stages", List.of(buildStage(node, stageStatus, result, startedAt, durationMs)));
        payload.put("result", null);
        payload.put("error", null);
        return payload;
    }

    private List<Map<String, Object>> buildFinalStages(AuditTask task, String finalStatus, String errorCode, String errorMsg) {
        List<Map<String, Object>> stages = new ArrayList<>();
        for (Map<String, Object> row : nodeLogRepository.findStageLogsByTaskId(task.getTaskId())) {
            stages.add(stageFromLog(row));
        }
        if ("SUCCESS".equalsIgnoreCase(finalStatus)) {
            for (Map<String, Object> stage : stages) {
                if ("callback".equals(stage.get("stage_code"))) {
                    stage.put("stage_status", "SUCCESS");
                    stage.put("finished_at", nowIso());
                    stage.put("summary", "回调业务系统");
                    stage.put("detail", "发送AI审核终态回调");
                    stage.put("output", Map.of("callback_status", "submitted"));
                    stage.put("error", null);
                }
            }
        }
        if ("SUCCESS".equalsIgnoreCase(finalStatus) && stages.stream().noneMatch(stage -> "callback".equals(stage.get("stage_code")))) {
            Map<String, Object> callbackStage = new LinkedHashMap<>();
            callbackStage.put("stage_code", "callback");
            callbackStage.put("stage_instance_id", "callback_" + task.getTaskId());
            callbackStage.put("stage_name", "回调通知");
            callbackStage.put("stage_status", "SUCCESS");
            callbackStage.put("agent_name", "工作流回调服务");
            callbackStage.put("started_at", nowIso());
            callbackStage.put("finished_at", nowIso());
            callbackStage.put("duration_ms", 0);
            callbackStage.put("summary", "回调业务系统");
            callbackStage.put("detail", "发送AI审核终态回调");
            callbackStage.put("output", Map.of("callback_status", "submitted"));
            callbackStage.put("error", null);
            callbackStage.put("sort_num", 999);
            stages.add(callbackStage);
        }
        if (!"SUCCESS".equalsIgnoreCase(finalStatus) && stages.stream().noneMatch(stage -> "FAILED".equals(stage.get("stage_status")))) {
            Map<String, Object> failedStage = new LinkedHashMap<>();
            failedStage.put("stage_code", failureStageCode(task));
            failedStage.put("stage_instance_id", failureStageCode(task) + "_" + task.getTaskId());
            failedStage.put("stage_name", "工作流执行失败");
            failedStage.put("stage_status", "FAILED");
            failedStage.put("agent_name", "工作流引擎");
            failedStage.put("started_at", formatTime(task.getStartTime()));
            failedStage.put("finished_at", nowIso());
            failedStage.put("duration_ms", durationMs(task.getStartTime(), LocalDateTime.now()));
            failedStage.put("summary", errorMsg == null || errorMsg.isBlank() ? "工作流执行失败" : errorMsg);
            failedStage.put("detail", errorMsg == null ? "" : errorMsg);
            failedStage.put("output", Map.of());
            failedStage.put("error", errorBody(errorCode, errorMsg, failureStageCode(task)));
            failedStage.put("sort_num", 900);
            stages.add(failedStage);
        }
        return stages;
    }

    private Map<String, Object> stageFromLog(Map<String, Object> row) {
        String stageCode = stringValue(row.get("node_code"));
        String nodeStatus = stringValue(row.get("node_status"));
        Map<String, Object> output = jsonSupport.toMap(stringValue(row.get("output_snapshot")));
        Map<String, Object> stage = new LinkedHashMap<>();
        stage.put("stage_code", stageCode);
        stage.put("stage_instance_id", stageCode + "_" + row.get("log_id"));
        stage.put("stage_name", firstNotBlank(row.get("node_name"), stageCode));
        stage.put("stage_status", nodeStatus);
        stage.put("agent_name", firstNotBlank(row.get("node_type"), "工作流节点"));
        stage.put("started_at", formatTime((LocalDateTime) row.get("start_time")));
        stage.put("finished_at", formatTime((LocalDateTime) row.get("finish_time")));
        stage.put("duration_ms", row.get("duration_ms"));
        stage.put("summary", stageSummary(stageCode, nodeStatus, output, stringValue(row.get("error_msg"))));
        stage.put("detail", stageDetail(row, output));
        stage.put("output", output);
        stage.put("error", errorForStage(row));
        stage.put("sort_num", intValue(row.get("node_order"), 0));
        return stage;
    }

    private Map<String, Object> buildStage(AuditWorkflowNode node, String stageStatus, NodeExecutionResult result,
                                           LocalDateTime startedAt, long durationMs) {
        Map<String, Object> output = result == null || result.getOutput() == null ? new LinkedHashMap<>() : result.getOutput();
        Map<String, Object> stage = new LinkedHashMap<>();
        stage.put("stage_code", node.getNodeCode());
        stage.put("stage_instance_id", node.getNodeCode() + "_" + node.getNodeOrder());
        stage.put("stage_name", node.getNodeName());
        stage.put("stage_status", stageStatus);
        stage.put("agent_name", node.getNodeType());
        stage.put("started_at", formatTime(startedAt));
        stage.put("finished_at", "RUNNING".equalsIgnoreCase(stageStatus) ? null : nowIso());
        stage.put("duration_ms", durationMs);
        stage.put("summary", stageSummary(node.getNodeCode(), stageStatus, output, result == null ? "" : result.getErrorMsg()));
        stage.put("detail", result == null || result.getErrorMsg() == null ? "" : result.getErrorMsg());
        stage.put("output", output);
        stage.put("error", result == null || result.isSuccess() ? null : errorBody(result.getErrorCode(), result.getErrorMsg(), node.getNodeCode()));
        stage.put("sort_num", node.getNodeOrder());
        return stage;
    }

    private Object errorForStage(Map<String, Object> row) {
        String status = stringValue(row.get("node_status"));
        if (!"FAILED".equalsIgnoreCase(status)) {
            return null;
        }
        return errorBody(stringValue(row.get("error_code")), stringValue(row.get("error_msg")), stringValue(row.get("node_code")));
    }

    private Map<String, Object> errorBody(String errorCode, String errorMsg, String stageCode) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", errorCode == null ? "" : errorCode);
        error.put("message", errorMsg == null ? "" : errorMsg);
        error.put("stage_code", stageCode == null ? "" : stageCode);
        error.put("error_code", errorCode == null ? "" : errorCode);
        error.put("error_msg", errorMsg == null ? "" : errorMsg);
        return error;
    }

    private String stageSummary(String stageCode, String stageStatus, Map<String, Object> output, String errorMsg) {
        if ("FAILED".equalsIgnoreCase(stageStatus)) {
            return errorMsg == null || errorMsg.isBlank() ? "阶段执行失败" : errorMsg;
        }
        if ("RUNNING".equalsIgnoreCase(stageStatus)) {
            return "正在执行" + stageDisplayName(stageCode);
        }
        if ("ai_audit".equals(stageCode) && output.containsKey("candidate_count")) {
            return "AI审核完成，生成" + output.get("candidate_count") + "个候选问题";
        }
        if ("ai_audit".equals(stageCode) && output.containsKey("finding_count")) {
            return "AI审核完成，生成" + output.get("finding_count") + "个候选问题";
        }
        if ("result_validate".equals(stageCode) && output.containsKey("candidate_count")) {
            return "结果校验完成，候选问题" + output.get("candidate_count")
                    + "个，最终有效问题" + output.getOrDefault("valid_issue_count", output.get("totalIssues")) + "个";
        }
        if ("summary_generate".equals(stageCode) && "LLM_GENERATED".equals(output.get("summary_status"))) {
            return "AI总结生成完成";
        }
        if ("summary_generate".equals(stageCode) && "RULE_FALLBACK".equals(output.get("summary_status"))) {
            return "AI总结生成失败，已使用规则摘要";
        }
        if ("result_save".equals(stageCode) && output.containsKey("issue_count")) {
            return "审核结果保存完成，保存" + output.get("issue_count") + "条问题记录";
        }
        return stageDisplayName(stageCode) + "完成";
    }

    private String stageDetail(Map<String, Object> row, Map<String, Object> output) {
        String errorMsg = stringValue(row.get("error_msg"));
        if (!errorMsg.isBlank()) {
            return errorMsg;
        }
        return output.isEmpty() ? "" : jsonSupport.toJson(output);
    }

    private String stageDisplayName(String stageCode) {
        return switch (stageCode == null ? "" : stageCode) {
            case "input_validate" -> "输入校验";
            case "file_parse", "target_file_parse" -> "报告文件解析";
            case "basis_file_parse" -> "依据文件解析";
            case "text_split", "target_text_split" -> "文本切分";
            case "knowledge_retrieve" -> "知识库检索";
            case "uploaded_basis_match", "basis_pack_or_match" -> "上传依据匹配";
            case "ai_audit" -> "AI审核分析";
            case "result_validate" -> "结果校验";
            case "summary_generate" -> "AI总结生成";
            case "result_save" -> "结果保存";
            case "callback" -> "回调通知";
            default -> stageCode == null || stageCode.isBlank() ? "执行阶段" : stageCode;
        };
    }

    private String stageCallbackUrl(String callbackUrl) {
        if (callbackUrl.endsWith("/callback")) {
            return callbackUrl.substring(0, callbackUrl.length() - "/callback".length()) + "/stageCallback";
        }
        return callbackUrl.endsWith("/") ? callbackUrl + "stageCallback" : callbackUrl + "/stageCallback";
    }

    private String callbackEventId(AuditTask task, String eventType) {
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
        return "wf-audit-" + task.getTaskId() + "-" + retryCount + "-" + eventType;
    }

    private int progressPercent(AuditWorkflowNode node) {
        int order = node.getNodeOrder() == null ? 0 : node.getNodeOrder();
        return Math.max(5, Math.min(95, order));
    }

    private String stageProgressText(AuditWorkflowNode node, String stageStatus) {
        if ("RUNNING".equalsIgnoreCase(stageStatus)) {
            return "正在执行" + node.getNodeName();
        }
        if ("SUCCESS".equalsIgnoreCase(stageStatus)) {
            return node.getNodeName() + "执行完成";
        }
        return node.getNodeName() + "执行失败";
    }

    private String failureStageCode(AuditTask task) {
        return task.getCurrentNodeCode() == null || task.getCurrentNodeCode().isBlank()
                ? "workflow_failed"
                : task.getCurrentNodeCode();
    }

    private String failedStageCode(List<Map<String, Object>> stages, String fallback) {
        for (Map<String, Object> stage : stages) {
            if ("FAILED".equalsIgnoreCase(stringValue(stage.get("stage_status")))) {
                String stageCode = stringValue(stage.get("stage_code"));
                if (!stageCode.isBlank()) {
                    return stageCode;
                }
            }
        }
        return fallback;
    }

    private long durationMs(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return Duration.between(start, end).toMillis();
    }

    private String nowIso() {
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String formatTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(ZoneId.systemDefault()).toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String firstNotBlank(Object first, String fallback) {
        String value = stringValue(first);
        return value.isBlank() ? fallback : value;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Object value, int fallback) {
        if (value == null || String.valueOf(value).isBlank()) {
            return fallback;
        }
        return Integer.parseInt(String.valueOf(value));
    }
}
