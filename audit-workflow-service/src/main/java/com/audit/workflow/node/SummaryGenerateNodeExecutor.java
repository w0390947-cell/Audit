package com.audit.workflow.node;

import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.model.ModelGateway;
import com.audit.workflow.model.ModelRequest;
import com.audit.workflow.model.ModelResponse;
import com.audit.workflow.repository.AuditModelCallLogRepository;
import com.audit.workflow.support.JsonSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SummaryGenerateNodeExecutor implements WorkflowNodeExecutor {

    private final ModelGateway modelGateway;
    private final AuditModelCallLogRepository modelCallLogRepository;
    private final JsonSupport jsonSupport;
    private final String defaultModel;
    private final int maxReportChars;
    private final int maxFindingsChars;
    private final int maxSummaryChars;

    public SummaryGenerateNodeExecutor(ModelGateway modelGateway,
                                       AuditModelCallLogRepository modelCallLogRepository,
                                       JsonSupport jsonSupport,
                                       @Value("${audit.model.default-chat-model:qwen-plus}") String defaultModel,
                                       @Value("${audit.summary.max-report-chars:12000}") int maxReportChars,
                                       @Value("${audit.summary.max-findings-chars:12000}") int maxFindingsChars,
                                       @Value("${audit.summary.max-summary-chars:500}") int maxSummaryChars) {
        this.modelGateway = modelGateway;
        this.modelCallLogRepository = modelCallLogRepository;
        this.jsonSupport = jsonSupport;
        this.defaultModel = defaultModel;
        this.maxReportChars = maxReportChars;
        this.maxFindingsChars = maxFindingsChars;
        this.maxSummaryChars = maxSummaryChars;
    }

    @Override
    public String nodeType() {
        return NodeType.SUMMARY_GENERATE;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        Object rawResult = context.getVariables().get("validated_result");
        if (!(rawResult instanceof Map<?, ?>)) {
            return NodeExecutionResult.failure("SUMMARY_INPUT_MISSING", "validated result not found");
        }

        Map<String, Object> result = mapValue(rawResult);
        String fallbackSummary = defaultSummary(result);
        try {
            ModelRequest request = new ModelRequest();
            request.setTaskId(context.getTask().getTaskId());
            request.setTaskNo(context.getTask().getTaskNo());
            request.setWorkflowCode(context.getTask().getWorkflowCode());
            request.setModelName(defaultModel);
            request.setSystemPrompt("你是专业报告审核总结助手，只能基于输入的报告内容和已审查出的问题生成总结。");
            request.setUserPrompt(buildPrompt(context, result, fallbackSummary));

            ModelResponse response = modelGateway.chat(request);
            modelCallLogRepository.insertLog(context.getTask(), request, response);
            if (!response.isSuccess()) {
                return fallback(result, context, fallbackSummary,
                        firstNotBlank(response.getErrorMsg(), response.getErrorCode(), "summary model call failed"));
            }

            String generatedSummary = parseSummary(response.getContent());
            if (generatedSummary.isBlank()) {
                return fallback(result, context, fallbackSummary, "summary is blank");
            }

            generatedSummary = normalizeSummary(generatedSummary);
            applySummary(result, context, generatedSummary, fallbackSummary, response);

            Map<String, Object> output = new LinkedHashMap<>();
            output.put("summary_status", "LLM_GENERATED");
            output.put("summary_source", "llm");
            output.put("summary_length", generatedSummary.length());
            output.put("fallback_summary", fallbackSummary);
            output.put("model", firstNotBlank(response.getModelName(), defaultModel));
            output.put("request_id", response.getRequestId());
            output.put("input_tokens", response.getInputTokens());
            output.put("output_tokens", response.getOutputTokens());
            output.put("duration_ms", response.getDurationMs());
            return NodeExecutionResult.success(output);
        } catch (Exception ex) {
            return fallback(result, context, fallbackSummary, ex.getMessage());
        }
    }

    private String buildPrompt(WorkflowTaskContext context, Map<String, Object> result, String fallbackSummary) {
        String reportText = reportText(context);
        String findingsJson = findingsJson(result);
        int totalIssues = intValue(first(result, "totalIssues", "total_issues", "issue_count"));
        String riskLevel = riskLevel(result);

        return """
                请根据【待审查报告正文摘录】和【已确认问题清单】生成一段中文 AI 审查总结。

                要求：
                1. 只能基于输入材料总结，不得新增、编造或扩大问题。
                2. 可以偏业务化概述，不强制逐项罗列问题数量。
                3. 如果未发现问题，不得表述为“完全合格”或“无任何风险”，应表述为“未发现关键问题，建议结合人工审核流程复核”。
                4. 总结控制在 150 到 300 字左右，语言正式、客观，适合写入 PDF 报告“AI总结”部分。
                5. 不要输出 Markdown，不要输出代码块，不要输出解释性文字。
                6. 只返回严格 JSON 对象，格式为 {"summary":"..."}。

                【规则摘要兜底值】
                %s

                【问题统计】
                totalIssues=%d
                riskLevel=%s

                【待审查报告正文摘录】
                %s

                【已确认问题清单】
                %s
                """.formatted(fallbackSummary, totalIssues, riskLevel, reportText, findingsJson);
    }

    private String reportText(WorkflowTaskContext context) {
        Object parsed = context.getVariables().get("parsed_document");
        if (parsed instanceof ParsedDocument document) {
            return abbreviateMiddle(document.getFullText(), Math.max(1000, maxReportChars));
        }
        return "";
    }

    private String findingsJson(Map<String, Object> result) {
        List<Map<String, Object>> findings = new ArrayList<>();
        for (Map<String, Object> item : listOfMaps(result.get("findings"))) {
            Map<String, Object> finding = new LinkedHashMap<>();
            putIfPresent(finding, "type", item.get("type"));
            putIfPresent(finding, "title", item.get("title"));
            putIfPresent(finding, "content", item.get("content"));
            putIfPresent(finding, "severity", item.get("severity"));
            putIfPresent(finding, "quote", item.get("quote"));
            putIfPresent(finding, "location_text", item.get("location_text"));
            putIfPresent(finding, "location", item.get("location"));
            putIfPresent(finding, "suggestion", item.get("suggestion"));
            findings.add(finding);
        }
        String json = jsonSupport.toJson(findings);
        return abbreviateEnd(json, Math.max(1000, maxFindingsChars));
    }

    private String parseSummary(String content) {
        String json = stripJson(content);
        try {
            Map<String, Object> response = jsonSupport.toMap(json);
            return stringValue(first(response, "summary", "ai_summary", "content", "text"));
        } catch (Exception ex) {
            return "";
        }
    }

    private void applySummary(Map<String, Object> result, WorkflowTaskContext context, String summary,
                              String fallbackSummary, ModelResponse response) {
        result.put("summary", summary);
        Map<String, Object> meta = mapValue(result.get("summary_meta"));
        meta.put("source", "llm");
        meta.put("fallback_summary", fallbackSummary);
        meta.put("model", firstNotBlank(response.getModelName(), defaultModel));
        meta.put("request_id", response.getRequestId());
        meta.remove("error");
        result.put("summary_meta", meta);
        context.putVariable("validated_result", result);
        updateTaskSummary(context, summary, "llm", fallbackSummary, null);
    }

    private NodeExecutionResult fallback(Map<String, Object> result, WorkflowTaskContext context,
                                         String fallbackSummary, String error) {
        result.put("summary", fallbackSummary);
        Map<String, Object> meta = mapValue(result.get("summary_meta"));
        meta.put("source", "rule_fallback");
        meta.put("fallback_summary", fallbackSummary);
        meta.put("error", abbreviateEnd(firstNotBlank(error, "summary generation failed"), 500));
        result.put("summary_meta", meta);
        context.putVariable("validated_result", result);
        updateTaskSummary(context, fallbackSummary, "rule_fallback", fallbackSummary, error);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("summary_status", "RULE_FALLBACK");
        output.put("summary_source", "rule_fallback");
        output.put("fallback_summary", fallbackSummary);
        output.put("error", abbreviateEnd(firstNotBlank(error, "summary generation failed"), 500));
        return NodeExecutionResult.success(output);
    }

    private void updateTaskSummary(WorkflowTaskContext context, String summary, String source,
                                   String fallbackSummary, String error) {
        Object rawSummary = context.getVariables().get("task_summary");
        if (!(rawSummary instanceof Map<?, ?>)) {
            return;
        }
        Map<String, Object> taskSummary = mapValue(rawSummary);
        taskSummary.put("summary", summary);
        taskSummary.put("summary_source", source);
        taskSummary.put("fallback_summary", fallbackSummary);
        if (error != null && !error.isBlank()) {
            taskSummary.put("summary_generate_error", abbreviateEnd(error, 500));
        } else {
            taskSummary.remove("summary_generate_error");
        }
        context.putVariable("task_summary", taskSummary);
    }

    private String defaultSummary(Map<String, Object> result) {
        String summary = stringValue(result.get("summary")).trim();
        if (!summary.isBlank()) {
            return summary;
        }
        int totalIssues = intValue(first(result, "totalIssues", "total_issues", "issue_count"));
        return totalIssues == 0 ? "未发现关键问题" : "本次审核发现" + totalIssues + "个有效问题";
    }

    private String riskLevel(Map<String, Object> result) {
        String risk = "low";
        for (Map<String, Object> finding : listOfMaps(result.get("findings"))) {
            risk = higherRisk(risk, stringValue(finding.get("severity")));
        }
        return risk;
    }

    private String higherRisk(String left, String right) {
        return riskRank(right) > riskRank(left) ? right : left;
    }

    private int riskRank(String risk) {
        return switch (risk == null ? "" : risk.toLowerCase()) {
            case "high" -> 3;
            case "medium" -> 2;
            default -> 1;
        };
    }

    private String normalizeSummary(String value) {
        String text = stringValue(value).replaceAll("\\s+", " ").trim();
        return abbreviateEnd(text, Math.max(100, maxSummaryChars));
    }

    private void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null && !stringValue(value).isBlank()) {
            target.put(key, value);
        }
    }

    private Object first(Map<String, Object> source, String... keys) {
        if (source == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (value != null && !stringValue(value).isBlank()) {
                return value;
            }
        }
        return null;
    }

    private List<Map<String, Object>> listOfMaps(Object value) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> map = mapValue(item);
                if (!map.isEmpty()) {
                    result.add(map);
                }
            }
        }
        return result;
    }

    private Map<String, Object> mapValue(Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }
        return result;
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(stringValue(value));
        } catch (Exception ex) {
            return 0;
        }
    }

    private String firstNotBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
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

    private String abbreviateMiddle(String value, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        int headLength = Math.max(maxLength * 2 / 3, 1);
        int tailLength = Math.max(maxLength - headLength, 0);
        return text.substring(0, headLength)
                + "\n...\n【中间内容已截断】\n...\n"
                + text.substring(text.length() - tailLength);
    }

    private String abbreviateEnd(String value, int maxLength) {
        String text = value == null ? "" : value.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength));
    }
}
