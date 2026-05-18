package com.audit.workflow.node;

import com.audit.workflow.common.BusinessException;
import com.audit.workflow.domain.AuditWorkflowNode;
import com.audit.workflow.domain.NodeExecutionResult;
import com.audit.workflow.domain.ParsedDocument;
import com.audit.workflow.domain.RetrievalReference;
import com.audit.workflow.domain.WorkflowTaskContext;
import com.audit.workflow.enums.NodeType;
import com.audit.workflow.repository.AuditRetrievalRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ResultValidateNodeExecutor implements WorkflowNodeExecutor {

    private static final Set<String> FINDING_TYPES = Set.of("内容缺失", "格式错误", "数据异常", "逻辑错误", "标准不符", "其他");

    private final AuditRetrievalRepository retrievalRepository;

    public ResultValidateNodeExecutor(AuditRetrievalRepository retrievalRepository) {
        this.retrievalRepository = retrievalRepository;
    }

    @Override
    public String nodeType() {
        return NodeType.RESULT_VALIDATE;
    }

    @Override
    public NodeExecutionResult execute(WorkflowTaskContext context, AuditWorkflowNode node) {
        return validateBusinessReportFindings(context);
    }

    private NodeExecutionResult validateBusinessReportFindings(WorkflowTaskContext context) {
        Object rawResult = context.getVariables().get("model_result");
        if (!(rawResult instanceof Map<?, ?>)) {
            throw new BusinessException("RESULT_SCHEMA_INVALID", "model result not found");
        }
        Map<String, Object> result = mapValue(rawResult);
        result.put("success", true);

        Map<Long, List<RetrievalReference>> referencesByChunk = referencesByChunk(context);
        String productName = productName(context);
        List<Map<String, Object>> findings = listOfMaps(result.get("findings"));
        int candidateCount = findings.size();
        List<Map<String, Object>> normalizedFindings = new ArrayList<>();
        List<Map<String, Object>> validationWarnings = new ArrayList<>();
        for (int i = 0; i < findings.size(); i++) {
            Map<String, Object> finding = normalizeFinding(findings.get(i), i, referencesByChunk, productName, validationWarnings);
            if (finding != null) {
                normalizedFindings.add(finding);
            }
        }
        result.put("findings", normalizedFindings);
        result.put("totalIssues", normalizedFindings.size());
        Map<String, Object> diagnostics = validationDiagnostics(candidateCount, normalizedFindings.size(), validationWarnings);
        result.put("diagnostics", diagnostics);
        if (!validationWarnings.isEmpty()) {
            result.put("validation_warnings", validationWarnings);
        }
        addPageLocationWarningIfNeeded(context, result, normalizedFindings);
        result.put("summary", validatedSummary(normalizedFindings.size(), validationWarnings));
        Object retrievalUsedSummary = context.getVariables().get("retrieval_used_summary");
        if (retrievalUsedSummary instanceof Map<?, ?>) {
            result.put("retrieval_used_summary", mapValue(retrievalUsedSummary));
        }

        Map<String, Object> taskSummary = new LinkedHashMap<>();
        taskSummary.put("success", true);
        taskSummary.put("summary", result.get("summary"));
        taskSummary.put("totalIssues", normalizedFindings.size());
        taskSummary.put("risk_level", normalizedFindings.stream()
                .map(finding -> String.valueOf(finding.get("severity")))
                .reduce("low", this::higherRisk));

        context.putVariable("validated_result", result);
        context.putVariable("task_summary", taskSummary);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("validate_status", "SUCCESS");
        output.put("output_format", "business_report_findings");
        output.put("totalIssues", normalizedFindings.size());
        output.put("candidate_count", candidateCount);
        output.put("valid_issue_count", normalizedFindings.size());
        output.put("filtered_count", validationWarnings.size());
        output.put("filtered_findings", validationWarnings);
        output.put("filter_reasons", diagnostics.get("filter_reasons"));
        return NodeExecutionResult.success(output);
    }

    private Map<String, Object> normalizeFinding(Map<String, Object> rawFinding,
                                                int index,
                                                Map<Long, List<RetrievalReference>> referencesByChunk,
                                                String productName,
                                                List<Map<String, Object>> validationWarnings) {
        Map<String, Object> finding = new LinkedHashMap<>(rawFinding);
        String type = stringValue(finding.get("type"));
        finding.put("type", FINDING_TYPES.contains(type) ? type : "其他");
        finding.put("severity", normalizeRisk(finding.get("severity")));

        fillFindingText(finding);
        normalizeFindingLocation(finding);
        requireFindingText(finding, "title", index);
        requireFindingText(finding, "content", index);
        requireFindingText(finding, "location", index);
        requireFindingText(finding, "suggestion", index);
        if (isIncompleteFindingContent(finding)) {
            addValidationWarning(validationWarnings, "finding_content_incomplete", index, finding);
            return null;
        }
        Map<String, Object> location = mapValue(finding.get("location"));
        if (stringValue(location.get("quote")).isBlank()) {
            addValidationWarning(validationWarnings, "finding_quote_missing", index, finding);
            return null;
        }
        Long sourceChunkId = longValue(firstNotBlank(finding.get("source_chunk_id"), location.get("source_chunk_id")));
        Integer sourceChunkNo = positiveInt(firstNotBlank(finding.get("source_chunk_no"), location.get("source_chunk_no")));
        List<Map<String, Object>> basis = normalizeFindingBasis(
                finding.get("basis"), referencesByChunk.getOrDefault(sourceChunkId, List.of()));
        if (basis.isEmpty() && !referencesByChunk.isEmpty()) {
            addValidationWarning(validationWarnings, "finding_basis_missing", index, finding);
            return null;
        }
        if (isProductMismatch(productName, finding, basis)) {
            addValidationWarning(validationWarnings, "finding_basis_product_mismatch", index, finding);
            return null;
        }
        if (!basis.isEmpty()) {
            finding.put("basis", basis);
        }
        finalizeFindingDisplayFields(finding, sourceChunkId, sourceChunkNo);
        return finding;
    }

    private void fillFindingText(Map<String, Object> finding) {
        if (stringValue(finding.get("title")).isBlank()) {
            finding.put("title", abbreviate(firstNotBlank(finding.get("content"), finding.get("problem")), "AI审核发现问题", 30));
        }
        if (stringValue(finding.get("content")).isBlank()) {
            finding.put("content", firstNotBlank(finding.get("problem"), finding.get("description"), finding.get("title")));
        }
        if (stringValue(finding.get("location")).isBlank()) {
            finding.put("location", "未明确位置");
        }
        if (stringValue(finding.get("suggestion")).isBlank()) {
            finding.put("suggestion", "请依据知识库审核依据修正相关内容。");
        }
    }

    private void normalizeFindingLocation(Map<String, Object> finding) {
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
        if (page != null) {
            location.put("page", page);
            location.put("pageNo", page);
            location.put("page_no", page);
            finding.put("page", page);
            finding.put("pageNo", page);
            finding.put("page_no", page);
        }
        String section = firstNotBlank(location.get("section"), finding.get("section"), locationText);
        if (!section.isBlank()) {
            location.put("section", section);
        }
        String quote = firstNotBlank(location.get("quote"), finding.get("quote"));
        if (!quote.isBlank()) {
            location.put("quote", quote);
        }
        if (location.isEmpty()) {
            location.put("section", "未明确位置");
        }
        finding.put("location", location);
    }

    private void finalizeFindingDisplayFields(Map<String, Object> finding, Long sourceChunkId, Integer sourceChunkNo) {
        Map<String, Object> location = mapValue(finding.get("location"));
        String quote = firstNotBlank(finding.get("quote"), location.get("quote"));
        if (!quote.isBlank()) {
            finding.put("quote", quote);
            location.put("quote", quote);
        }

        Integer page = positiveInt(firstNotBlank(location.get("page"), location.get("pageNo"), location.get("page_no"),
                finding.get("page"), finding.get("pageNo"), finding.get("page_no")));
        if (page != null) {
            location.put("page", page);
            location.put("pageNo", page);
            location.put("page_no", page);
            finding.put("page", page);
            finding.put("pageNo", page);
            finding.put("page_no", page);
        }

        String section = firstNotBlank(location.get("section"), finding.get("location_text"), finding.get("section"));
        if (!section.isBlank()) {
            location.put("section", section);
        }
        finding.put("location_text", buildLocationText(page, section));

        Map<String, Object> debug = mapValue(finding.get("debug"));
        if (sourceChunkId != null) {
            debug.put("source_chunk_id", sourceChunkId);
        }
        if (sourceChunkNo != null) {
            debug.put("source_chunk_no", sourceChunkNo);
        }
        if (!debug.isEmpty()) {
            finding.put("debug", debug);
        }

        location.remove("source_chunk_id");
        location.remove("source_chunk_no");
        finding.remove("source_chunk_id");
        finding.remove("source_chunk_no");
        finding.put("location", location);
    }

    private String buildLocationText(Integer page, String section) {
        String cleanSection = stripTechnicalLocationText(section);
        if (page != null && !cleanSection.isBlank()) {
            return "第" + page + "页，" + abbreviate(cleanSection, cleanSection, 50);
        }
        if (page != null) {
            return "第" + page + "页";
        }
        if (!cleanSection.isBlank()) {
            return abbreviate(cleanSection, cleanSection, 50);
        }
        return "未明确位置";
    }

    private String stripTechnicalLocationText(String value) {
        String text = value == null ? "" : value.trim();
        if (text.startsWith("{") || text.contains("source_chunk_id") || text.contains("source_chunk_no")) {
            return "";
        }
        return text;
    }

    private Map<Long, List<RetrievalReference>> referencesByChunk(WorkflowTaskContext context) {
        Map<Long, List<RetrievalReference>> referencesByChunk = new LinkedHashMap<>();
        for (RetrievalReference reference : retrievalRepository.findReferencesByTaskId(context.getTask().getTaskId())) {
            if (reference.getSourceChunkId() == null) {
                continue;
            }
            referencesByChunk.computeIfAbsent(reference.getSourceChunkId(), ignored -> new ArrayList<>()).add(reference);
        }
        return referencesByChunk;
    }

    private List<Map<String, Object>> normalizeFindingBasis(Object rawBasis, List<RetrievalReference> references) {
        List<Map<String, Object>> basis = new ArrayList<>();
        Map<String, RetrievalReference> referenceByKbChunkId = referencesByKbChunkId(references);
        if (rawBasis instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> basisItem = normalizeFindingBasisItem(mapValue(item), referenceByKbChunkId);
                if (!basisItem.isEmpty()) {
                    basis.add(basisItem);
                }
            }
            return basis;
        }
        Map<String, Object> item = normalizeFindingBasisItem(mapValue(rawBasis), referenceByKbChunkId);
        if (!item.isEmpty()) {
            basis.add(item);
        }
        return basis;
    }

    private Map<String, Object> normalizeFindingBasisItem(Map<String, Object> rawItem,
                                                         Map<String, RetrievalReference> referenceByKbChunkId) {
        if (rawItem.isEmpty()) {
            return rawItem;
        }
        Map<String, Object> item = new LinkedHashMap<>(rawItem);
        String kbChunkId = firstNotBlank(first(item, "kb_chunk_id", "kbChunkId", "chunk_id", "chunkId"));
        RetrievalReference reference = kbChunkId.isBlank() ? null : referenceByKbChunkId.get(kbChunkId);
        if (!kbChunkId.isBlank()) {
            item.put("kb_chunk_id", kbChunkId);
        }
        String fileName = firstNotBlank(first(item, "file_name", "fileName", "source", "basis_file_name", "basisFileName"));
        if (fileName.isBlank() && reference != null) {
            fileName = stringValue(reference.getFileName());
        }
        if (!fileName.isBlank()) {
            item.put("file_name", fileName);
        }
        String versionNo = firstNotBlank(first(item, "version_no", "versionNo", "version"));
        if (versionNo.isBlank() && reference != null) {
            versionNo = stringValue(reference.getVersionNo());
        }
        if (!versionNo.isBlank()) {
            item.put("version_no", versionNo);
        }
        Integer page = positiveInt(firstNotBlank(first(item, "page", "pageNo", "page_no")));
        if (page == null && reference != null && reference.getPageNo() != null && reference.getPageNo() > 0) {
            page = reference.getPageNo();
        }
        if (page != null) {
            item.put("page", page);
        }
        String section = firstNotBlank(first(item, "section", "section_title", "sectionTitle"));
        if (section.isBlank() && reference != null) {
            section = stringValue(reference.getSectionTitle());
        }
        if (!section.isBlank()) {
            item.put("section", section);
        }
        String ruleCode = firstNotBlank(first(item, "rule_code", "ruleCode"));
        if (ruleCode.isBlank() && reference != null) {
            ruleCode = stringValue(reference.getRuleCode());
        }
        if (!ruleCode.isBlank()) {
            item.put("rule_code", ruleCode);
        }
        String quote = firstNotBlank(first(item, "quote", "quote_text", "basis_quote", "basisQuote", "content"));
        if (quote.isBlank() && reference != null) {
            quote = reference.getChunkTextSnapshot();
        }
        if (!quote.isBlank()) {
            item.put("quote", quote);
        }
        String conflictDescription = firstNotBlank(first(item, "conflict_description", "conflictDescription",
                "conflict_note", "conflictNote", "basis_conflict", "basisConflict"));
        if (!conflictDescription.isBlank()) {
            item.put("conflict_description", conflictDescription);
        }
        return item;
    }

    private Map<String, RetrievalReference> referencesByKbChunkId(List<RetrievalReference> references) {
        Map<String, RetrievalReference> result = new LinkedHashMap<>();
        for (RetrievalReference reference : references) {
            if (reference.getKbChunkId() != null && !reference.getKbChunkId().isBlank()) {
                result.putIfAbsent(reference.getKbChunkId(), reference);
            }
        }
        return result;
    }

    private boolean isIncompleteFindingContent(Map<String, Object> finding) {
        String content = stringValue(finding.get("content")).trim();
        if (content.length() < 8) {
            return true;
        }
        return content.endsWith("为")
                || content.endsWith("标注为")
                || content.endsWith("描述为")
                || content.endsWith("显示为")
                || content.endsWith("规定为")
                || content.endsWith("包括")
                || content.endsWith("如下")
                || content.endsWith("：")
                || content.endsWith(":");
    }

    private boolean isProductMismatch(String productName, Map<String, Object> finding, List<Map<String, Object>> basis) {
        String product = stringValue(productName);
        if (product.isBlank()) {
            return false;
        }
        String findingText = (stringValue(finding.get("title")) + "\n"
                + stringValue(finding.get("content")) + "\n"
                + stringValue(finding.get("suggestion"))).toLowerCase();
        String basisText = basisText(basis).toLowerCase();
        boolean phoneProduct = product.contains("手机");
        boolean intrinsicSafetyProduct = product.contains("本安") && !product.contains("隔爆");
        if (phoneProduct && (basisText.contains("控制箱") || basisText.contains("kxj127") || basisText.contains("plc控制箱"))) {
            return true;
        }
        return intrinsicSafetyProduct
                && (findingText.contains("缺少隔爆") || findingText.contains("未包含隔爆") || findingText.contains("\"db\"") || findingText.contains(" db "))
                && (basisText.contains("隔爆") || basisText.contains("\"db\"") || basisText.contains(" db "));
    }

    private String basisText(List<Map<String, Object>> basis) {
        StringBuilder builder = new StringBuilder();
        for (Map<String, Object> item : basis) {
            builder.append(firstNotBlank(item.get("file_name"), item.get("source"))).append('\n');
            builder.append(firstNotBlank(item.get("section"), item.get("section_title"))).append('\n');
            builder.append(firstNotBlank(item.get("quote"), item.get("content"))).append('\n');
        }
        return builder.toString();
    }

    private String productName(WorkflowTaskContext context) {
        Map<String, Object> metadata = mapValue(context.getInput().get("metadata"));
        return firstNotBlank(
                context.getInput().get("product_name"),
                context.getInput().get("productName"),
                metadata.get("product_name"),
                metadata.get("productName"));
    }

    private void addValidationWarning(List<Map<String, Object>> warnings,
                                      String reason,
                                      int index,
                                      Map<String, Object> finding) {
        Map<String, Object> warning = new LinkedHashMap<>();
        warning.put("type", "finding_filtered");
        warning.put("reason", reason);
        warning.put("message", filterReasonMessage(reason));
        warning.put("finding_index", index + 1);
        warning.put("title", finding.get("title"));
        warning.put("content", finding.get("content"));
        warning.put("severity", finding.get("severity"));
        warning.put("location", finding.get("location"));
        warning.put("basis", finding.get("basis"));
        warning.put("source_chunk_id", finding.get("source_chunk_id"));
        warnings.add(warning);
    }

    private Map<String, Object> validationDiagnostics(int candidateCount,
                                                      int validIssueCount,
                                                      List<Map<String, Object>> filteredFindings) {
        Map<String, Object> diagnostics = new LinkedHashMap<>();
        diagnostics.put("candidate_count", candidateCount);
        diagnostics.put("valid_issue_count", validIssueCount);
        diagnostics.put("filtered_count", filteredFindings.size());
        diagnostics.put("filter_reasons", filterReasons(filteredFindings));
        if (!filteredFindings.isEmpty()) {
            diagnostics.put("filtered_findings", filteredFindings);
        }
        return diagnostics;
    }

    private List<Map<String, Object>> filterReasons(List<Map<String, Object>> filteredFindings) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Map<String, Object> finding : filteredFindings) {
            String reason = firstNotBlank(finding.get("reason"), "unknown");
            counts.put(reason, counts.getOrDefault(reason, 0) + 1);
        }
        List<Map<String, Object>> reasons = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            Map<String, Object> reason = new LinkedHashMap<>();
            reason.put("reason", entry.getKey());
            reason.put("message", filterReasonMessage(entry.getKey()));
            reason.put("count", entry.getValue());
            reasons.add(reason);
        }
        return reasons;
    }

    private String filteredSummary(int candidateCount, List<Map<String, Object>> filteredFindings) {
        if (candidateCount > 0 && !filteredFindings.isEmpty()) {
            return "未发现关键问题；AI审核生成" + candidateCount + "个候选问题，经结果校验过滤" + filteredFindings.size() + "个。";
        }
        return "未发现关键问题";
    }

    private String validatedSummary(int validCount, List<Map<String, Object>> filteredFindings) {
        int filteredCount = filteredFindings.size();
        if (validCount == 0) {
            return filteredSummary(filteredCount, filteredFindings);
        }
        if (filteredCount == 0) {
            return "本次审核发现" + validCount + "个有效问题";
        }
        return "本次审核发现" + validCount + "个有效问题，另有" + filteredCount + "个候选问题经结果校验过滤。";
    }

    private String filterReasonMessage(String reason) {
        return switch (reason) {
            case "finding_content_incomplete" -> "候选问题描述不完整，无法形成可保存的有效问题";
            case "finding_quote_missing" -> "候选问题缺少报告原文 quote，无法定位问题原文";
            case "finding_basis_missing" -> "候选问题缺少有效审核依据";
            case "finding_basis_product_mismatch" -> "依据产品类型或保护型式与当前产品不匹配";
            default -> "候选问题未通过结果校验";
        };
    }

    private void addPageLocationWarningIfNeeded(WorkflowTaskContext context,
                                                Map<String, Object> result,
                                                List<Map<String, Object>> findings) {
        if (findings.isEmpty() || findings.stream().anyMatch(this::hasPageLocation)) {
            return;
        }
        Object parsed = context.getVariables().get("parsed_document");
        if (!(parsed instanceof ParsedDocument document)) {
            return;
        }
        Object supported = document.getMetadata().get("page_location_support");
        if (!Boolean.FALSE.equals(supported)) {
            return;
        }
        List<Map<String, Object>> warnings = listOfMaps(result.get("warnings"));
        Map<String, Object> warning = new LinkedHashMap<>();
        warning.put("type", "page_location_unavailable");
        warning.put("message", "当前文件解析链路无法取得与预览 PDF 一致的页码，终态 finding 不返回 location.page。");
        warning.put("page_location_source", document.getMetadata().get("page_location_source"));
        warning.put("file_type", document.getFileType());
        warnings.add(warning);
        result.put("warnings", warnings);
    }

    private boolean hasPageLocation(Map<String, Object> finding) {
        Map<String, Object> location = mapValue(finding.get("location"));
        return positiveInt(firstNotBlank(
                location.get("page"),
                location.get("pageNo"),
                location.get("page_no"),
                finding.get("page"),
                finding.get("pageNo"),
                finding.get("page_no"))) != null;
    }

    private String normalizeRisk(Object risk) {
        String value = risk == null ? "medium" : String.valueOf(risk).toLowerCase();
        if ("high".equals(value) || "medium".equals(value) || "low".equals(value)) {
            return value;
        }
        return "medium";
    }

    private String higherRisk(String left, String right) {
        return riskRank(right) > riskRank(left) ? right : left;
    }

    private int riskRank(String risk) {
        return switch (risk == null ? "" : risk) {
            case "high" -> 3;
            case "medium" -> 2;
            default -> 1;
        };
    }

    private void requireFindingText(Map<String, Object> finding, String key, int index) {
        if (stringValue(finding.get(key)).isBlank()) {
            throw new BusinessException("RESULT_SCHEMA_INVALID", "findings[" + index + "]." + key + " is required");
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

    private String abbreviate(String value, String fallback, int maxLength) {
        String text = value == null || value.isBlank() ? fallback : value.trim();
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private Object first(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }
        return null;
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

    private Map<String, Object> mapValue(Object value) {
        Map<String, Object> map = new HashMap<>();
        if (value instanceof Map<?, ?> rawMap) {
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() != null) {
                    map.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
        }
        return map;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Long longValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return Long.valueOf(String.valueOf(value));
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
}
