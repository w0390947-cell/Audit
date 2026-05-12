package com.ruoyi.system.service.audit.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.system.config.FastGptProperties;
import com.ruoyi.system.domain.audit.AuditAiTask;
import com.ruoyi.system.domain.audit.FastGptAuditFinding;
import com.ruoyi.system.domain.audit.FastGptAuditResult;
import com.ruoyi.system.exception.FastGptAuditException;
import com.ruoyi.system.service.audit.IFastGptAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FastGPT 审核服务实现
 *
 * @author ruoyi
 */
@Service
public class FastGptAuditServiceImpl implements IFastGptAuditService
{
    private static final Logger log = LoggerFactory.getLogger(FastGptAuditServiceImpl.class);

    private static final Pattern PAGE_TEXT_PATTERN = Pattern.compile("(?:第\\s*)?(\\d+)\\s*页");

    private final FastGptProperties properties;
    private final RestTemplate fastGptRestTemplate;
    private final ObjectMapper objectMapper;

    public FastGptAuditServiceImpl(FastGptProperties properties,
                                   @Qualifier("fastGptRestTemplate") RestTemplate fastGptRestTemplate)
    {
        this.properties = properties;
        this.fastGptRestTemplate = fastGptRestTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public FastGptAuditResult analyze(AuditAiTask task)
    {
        // 1. 检查功能开关
        if (!properties.isEnabled())
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.DISABLED);
        }

        // 2. 检查配置
        String apiKey = getApiKey(task);
        String baseUrl = properties.getBaseUrl();
        if (isBlank(apiKey))
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.CONFIG_MISSING, "API Key未配置");
        }
        if (isBlank(baseUrl))
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.CONFIG_MISSING, "BaseURL未配置");
        }

        // 3. 检查任务参数
        if (task.getReportFileUrl() == null || task.getReportFileUrl().isEmpty())
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.REPORT_URL_EMPTY);
        }

        // 4. 生成 chatId
        String chatId = generateChatId(task.getAiTaskId());
        long startTime = System.currentTimeMillis();

        log.info("FastGPT audit start, aiTaskId={}, chatId={}, reportUrl={}",
                task.getAiTaskId(), chatId, maskUrl(task.getReportFileUrl()));

        try
        {
            String reportText = "";
            if (hasBasisFiles(task))
            {
                reportText = preprocessReportText(task, baseUrl);
            }

            // 5. 构造请求
            String requestBody = buildRequestBody(task, chatId, reportText);
            log.debug("FastGPT request prepared, aiTaskId={}, chatId={}, reportUrl={}, requestLength={}",
                    task.getAiTaskId(), chatId, maskUrl(task.getReportFileUrl()), requestBody.length());

            // 6. 调用 FastGPT
            String endpoint = baseUrl + "/v1/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = fastGptRestTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // 7. 解析响应
            FastGptAuditResult result = parseResponse(response, chatId);
            result.setChatId(chatId);
            result.setElapsedMs(System.currentTimeMillis() - startTime);

            log.info("FastGPT audit success, aiTaskId={}, chatId={}, findingCount={}, elapsedMs={}",
                    task.getAiTaskId(), chatId,
                    result.getFindings() != null ? result.getFindings().size() : 0,
                    result.getElapsedMs());

            return result;
        }
        catch (FastGptAuditException e)
        {
            throw e;
        }
        catch (HttpClientErrorException e)
        {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED)
            {
                log.error("FastGPT audit failed, aiTaskId={}, chatId={}, errorCode=HTTP_401", task.getAiTaskId(), chatId);
                throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_401, "API Key无效", e);
            }
            else if (e.getStatusCode() == HttpStatus.NOT_FOUND)
            {
                log.error("FastGPT audit failed, aiTaskId={}, chatId={}, errorCode=HTTP_404", task.getAiTaskId(), chatId);
                throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_404, "服务地址不存在", e);
            }
            else
            {
                log.error("FastGPT audit failed, aiTaskId={}, chatId={}, errorCode=HTTP_ERROR, status={}",
                        task.getAiTaskId(), chatId, e.getStatusCode());
                throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_ERROR,
                        "HTTP请求失败: " + e.getStatusCode(), e);
            }
        }
        catch (ResourceAccessException e)
        {
            log.error("FastGPT audit failed, aiTaskId={}, chatId={}, errorCode=HTTP_TIMEOUT", task.getAiTaskId(), chatId);
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_TIMEOUT, "请求超时", e);
        }
        catch (Exception e)
        {
            log.error("FastGPT audit failed, aiTaskId={}, chatId={}, unexpected error",
                    task.getAiTaskId(), chatId, e);
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_ERROR,
                    "未预期的错误: " + e.getMessage(), e);
        }
    }

    /**
     * 生成 chatId
     * 格式：audit-ai-{aiTaskId}-{yyyyMMddHHmmss}
     */
    private String generateChatId(Long aiTaskId)
    {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return String.format("audit-ai-%d-%s", aiTaskId, timestamp);
    }

    private String getApiKey(AuditAiTask task)
    {
        if (hasBasisFiles(task))
        {
            return properties.getBasisApiKey();
        }
        return properties.getApiKey();
    }

    /**
     * 构造 FastGPT 请求体
     */
    private String buildRequestBody(AuditAiTask task, String chatId, String reportText)
    {
        try
        {
            Map<String, Object> request = new HashMap<>();
            request.put("chatId", chatId);
            // 阶段 2 只支持非流式响应，避免 SSE 响应进入 JSON 解析链路。
            request.put("stream", false);
            request.put("detail", properties.isDetail());

            // variables
            Map<String, Object> variables = new HashMap<>();
            String reportFileUrl = toPublicFileUrl(task.getReportFileUrl());
            variables.put("taskNo", task.getTaskNo() != null ? task.getTaskNo() : "");
            variables.put("productName", task.getProductName() != null ? task.getProductName() : "");
            variables.put("reportFileName", task.getReportFileName() != null ? task.getReportFileName() : "");
            variables.put("reportFileUrl", reportFileUrl);
            variables.put("basisFileUrls", toPublicFileUrls(task.getBasisFileUrls()));
            variables.put("reportText", reportText != null ? reportText : "");
            variables.put("auditStandard", properties.getDefaultAuditStandard() != null ?
                    properties.getDefaultAuditStandard() : "");
            variables.put("callbackTraceId", chatId);
            request.put("variables", variables);

            // messages
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");

            List<Map<String, Object>> content = new ArrayList<>();

            // text content
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", buildAuditPrompt(reportText));
            content.add(textContent);

            // file_url content
            Map<String, Object> fileContent = new HashMap<>();
            fileContent.put("type", "file_url");
            fileContent.put("name", task.getReportFileName() != null ? task.getReportFileName() : "report.pdf");
            fileContent.put("url", reportFileUrl);
            content.add(fileContent);

            if (hasBasisFiles(task))
            {
                for (String basisFileUrl : splitFileUrls(task.getBasisFileUrls()))
                {
                    String publicBasisFileUrl = toPublicFileUrl(basisFileUrl);
                    Map<String, Object> basisFileContent = new HashMap<>();
                    basisFileContent.put("type", "file_url");
                    basisFileContent.put("name", extractFileName(publicBasisFileUrl, "basis-file"));
                    basisFileContent.put("url", publicBasisFileUrl);
                    content.add(basisFileContent);
                }
            }

            message.put("content", content);
            messages.add(message);
            request.put("messages", messages);

            return objectMapper.writeValueAsString(request);
        }
        catch (Exception e)
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_ERROR,
                    "构造请求体失败: " + e.getMessage(), e);
        }
    }

    private String preprocessReportText(AuditAiTask task, String baseUrl)
    {
        String preprocessApiKey = properties.getPreprocessApiKey();
        if (isBlank(preprocessApiKey))
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.CONFIG_MISSING, "Preprocess API Key未配置");
        }

        String chatId = generatePreprocessChatId(task.getAiTaskId());
        long startTime = System.currentTimeMillis();
        log.info("FastGPT preprocess start, aiTaskId={}, chatId={}, reportUrl={}",
                task.getAiTaskId(), chatId, maskUrl(task.getReportFileUrl()));

        String requestBody = buildPreprocessRequestBody(task, chatId);
        log.debug("FastGPT preprocess request prepared, aiTaskId={}, chatId={}, requestLength={}",
                task.getAiTaskId(), chatId, requestBody.length());

        String endpoint = baseUrl + "/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(preprocessApiKey);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = fastGptRestTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                entity,
                String.class
        );

        String reportText = parsePlainContentResponse(response, chatId);
        if (isBlank(reportText))
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.RESPONSE_EMPTY, "预处理工作流返回正文为空");
        }

        log.info("FastGPT preprocess success, aiTaskId={}, chatId={}, textLength={}, elapsedMs={}",
                task.getAiTaskId(), chatId, reportText.length(), System.currentTimeMillis() - startTime);
        return reportText;
    }

    private String generatePreprocessChatId(Long aiTaskId)
    {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return String.format("audit-preprocess-%d-%s", aiTaskId, timestamp);
    }

    private String buildPreprocessRequestBody(AuditAiTask task, String chatId)
    {
        try
        {
            String reportFileUrl = toPublicFileUrl(task.getReportFileUrl());
            Map<String, Object> request = new HashMap<>();
            request.put("chatId", chatId);
            request.put("stream", false);
            request.put("detail", false);

            Map<String, Object> variables = new HashMap<>();
            variables.put("reportFileName", task.getReportFileName() != null ? task.getReportFileName() : "");
            variables.put("reportFileUrl", reportFileUrl);
            variables.put("callbackTraceId", chatId);
            request.put("variables", variables);

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");

            List<Map<String, Object>> content = new ArrayList<>();
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "请读取该报告文件，返回报告完整正文。只输出正文内容，不要输出Markdown、JSON或解释性文字。");
            content.add(textContent);

            Map<String, Object> fileContent = new HashMap<>();
            fileContent.put("type", "file_url");
            fileContent.put("name", task.getReportFileName() != null ? task.getReportFileName() : "report.pdf");
            fileContent.put("url", reportFileUrl);
            content.add(fileContent);

            message.put("content", content);
            messages.add(message);
            request.put("messages", messages);

            return objectMapper.writeValueAsString(request);
        }
        catch (Exception e)
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_ERROR,
                    "构造预处理请求体失败: " + e.getMessage(), e);
        }
    }

    private String parsePlainContentResponse(ResponseEntity<String> response, String chatId)
    {
        if (!response.getStatusCode().is2xxSuccessful())
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_ERROR,
                    "预处理HTTP状态码异常: " + response.getStatusCode());
        }

        String responseBody = response.getBody();
        if (isBlank(responseBody))
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.RESPONSE_EMPTY, "预处理响应体为空");
        }

        JsonNode root;
        try
        {
            root = objectMapper.readTree(responseBody);
        }
        catch (Exception e)
        {
            log.warn("FastGPT preprocess protocol invalid, chatId={}, responsePreview={}",
                    chatId, truncate(responseBody, 200));
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.RESPONSE_NOT_JSON,
                    "预处理响应不是有效的JSON", e);
        }

        String content = extractMessageContent(root);
        if (isBlank(content))
        {
            content = extractTextFromResponseData(root);
        }

        if (isBlank(content))
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.RESPONSE_EMPTY, "预处理响应content为空");
        }

        return content.trim();
    }

    private String buildAuditPrompt(String reportText)
    {
        String basePrompt = "请按审核规则分析该报告，输出严格JSON，不要输出Markdown。";
        if (isBlank(reportText))
        {
            return basePrompt;
        }
        return basePrompt + "\n\n以下是第三个工作流从待审核报告中提取出的完整正文，请将其作为待审核报告正文使用：\n" + reportText;
    }

    private boolean hasBasisFiles(AuditAiTask task)
    {
        return task != null && !splitFileUrls(task.getBasisFileUrls()).isEmpty();
    }

    private List<String> splitFileUrls(String fileUrls)
    {
        if (isBlank(fileUrls))
        {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (String item : fileUrls.split(","))
        {
            if (!isBlank(item))
            {
                list.add(item.trim());
            }
        }
        return list;
    }

    private String extractFileName(String fileUrl, String fallback)
    {
        if (isBlank(fileUrl))
        {
            return fallback;
        }
        int index = fileUrl.lastIndexOf("/");
        return index > -1 ? fileUrl.substring(index + 1) : fileUrl;
    }

    private String toPublicFileUrls(String fileUrls)
    {
        List<String> urls = splitFileUrls(fileUrls);
        if (urls.isEmpty())
        {
            return "";
        }
        List<String> publicUrls = new ArrayList<>();
        for (String url : urls)
        {
            publicUrls.add(toPublicFileUrl(url));
        }
        return String.join(",", publicUrls);
    }

    private String toPublicFileUrl(String fileUrl)
    {
        if (isBlank(fileUrl) || fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))
        {
            return encodeUrl(fileUrl);
        }
        String baseUrl = properties.getPublicFileBaseUrl();
        if (isBlank(baseUrl))
        {
            return fileUrl;
        }
        String publicUrl;
        if (fileUrl.startsWith("/"))
        {
            publicUrl = baseUrl + fileUrl;
        }
        else
        {
            publicUrl = baseUrl + "/" + fileUrl;
        }
        return encodeUrl(publicUrl);
    }

    private String encodeUrl(String url)
    {
        if (isBlank(url))
        {
            return url;
        }
        try
        {
            return UriComponentsBuilder.fromUriString(url)
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();
        }
        catch (Exception e)
        {
            log.warn("Failed to encode FastGPT file url, url={}", maskUrl(url), e);
            return url;
        }
    }

    private boolean isBlank(String value)
    {
        return value == null || value.trim().isEmpty();
    }

    private String extractMessageContent(JsonNode root)
    {
        JsonNode choices = root.path("choices");
        if (choices.isArray() && !choices.isEmpty())
        {
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.path("message");
            return message.path("content").asText();
        }
        return "";
    }

    private String extractTextFromResponseData(JsonNode root)
    {
        JsonNode responseData = root.path("responseData");
        if (responseData.isMissingNode() || responseData.isNull())
        {
            return "";
        }
        return findFirstText(responseData);
    }

    private String findFirstText(JsonNode node)
    {
        if (node == null || node.isNull() || node.isMissingNode())
        {
            return "";
        }
        if (node.isTextual())
        {
            return node.asText();
        }
        if (node.isContainerNode())
        {
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext())
            {
                String text = findFirstText(elements.next());
                if (!isBlank(text))
                {
                    return text;
                }
            }
        }
        return "";
    }

    /**
     * 解析 FastGPT 响应
     */
    private FastGptAuditResult parseResponse(ResponseEntity<String> response, String chatId)
    {
        // 1. 检查 HTTP 状态码
        if (!response.getStatusCode().is2xxSuccessful())
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.HTTP_ERROR,
                    "HTTP状态码异常: " + response.getStatusCode());
        }

        // 2. 检查响应体
        String responseBody = response.getBody();
        if (responseBody == null || responseBody.isEmpty())
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.RESPONSE_EMPTY, "响应体为空");
        }

        // 3. 解析 JSON
        JsonNode root;
        try
        {
            root = objectMapper.readTree(responseBody);
        }
        catch (Exception e)
        {
            log.warn("FastGPT audit protocol invalid, chatId={}, responsePreview={}",
                    chatId, truncate(responseBody, 200));
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.RESPONSE_NOT_JSON,
                    "响应不是有效的JSON", e);
        }

        // 4. 提取 content，优先使用 choices[0].message.content。
        String content = extractMessageContent(root);

        if (content == null || content.isEmpty())
        {
            content = extractContentFromResponseData(root);
        }

        if (content == null || content.isEmpty())
        {
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.RESPONSE_EMPTY,
                    "响应content为空，且responseData中未找到可解析输出");
        }

        // 5. 清理可能的 Markdown 包裹
        content = cleanMarkdownWrapper(content);

        // 6. 解析业务 JSON
        return parseBusinessJson(content, chatId);
    }

    /**
     * 从 detail=true 返回的 responseData 中兜底提取工作流输出。
     * 不假设固定 moduleType 或节点名称，递归查找包含 findings 字段的 JSON 文本或对象。
     */
    private String extractContentFromResponseData(JsonNode root)
    {
        JsonNode responseData = root.path("responseData");
        if (responseData.isMissingNode() || responseData.isNull())
        {
            return "";
        }
        return findBusinessJsonCandidate(responseData);
    }

    /**
     * 递归查找可解析的业务 JSON。
     */
    private String findBusinessJsonCandidate(JsonNode node)
    {
        if (node == null || node.isNull() || node.isMissingNode())
        {
            return "";
        }

        if (node.isObject() && node.has("findings"))
        {
            return node.toString();
        }

        if (node.isTextual())
        {
            String text = cleanMarkdownWrapper(node.asText());
            if (isBusinessJson(text))
            {
                return text;
            }
            return "";
        }

        if (node.isContainerNode())
        {
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext())
            {
                String candidate = findBusinessJsonCandidate(elements.next());
                if (candidate != null && !candidate.isEmpty())
                {
                    return candidate;
                }
            }
        }

        return "";
    }

    /**
     * 判断文本是否是包含 findings 字段的业务 JSON。
     */
    private boolean isBusinessJson(String text)
    {
        if (text == null || text.isEmpty())
        {
            return false;
        }
        try
        {
            JsonNode json = objectMapper.readTree(text);
            return json.isObject() && json.has("findings");
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * 清理 Markdown 包裹
     */
    private String cleanMarkdownWrapper(String content)
    {
        if (content == null)
        {
            return "";
        }

        // 去除前后空白
        content = content.trim();

        // 去除 UTF-8 BOM
        if (content.startsWith("\uFEFF"))
        {
            content = content.substring(1);
        }

        // 如果以 ```json 或 ``` 开头，删除第一行
        if (content.startsWith("```"))
        {
            int firstNewline = content.indexOf('\n');
            if (firstNewline > 0)
            {
                content = content.substring(firstNewline + 1);
            }

            // 删除结尾的 ```
            if (content.endsWith("```"))
            {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();
        }

        // 兜底：寻找第一个 { 和最后一个 }
        int firstBrace = content.indexOf('{');
        int lastBrace = content.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace)
        {
            content = content.substring(firstBrace, lastBrace + 1);
        }

        return content;
    }

    /**
     * 解析业务 JSON
     */
    private FastGptAuditResult parseBusinessJson(String content, String chatId)
    {
        try
        {
            JsonNode businessJson = objectMapper.readTree(content);
            FastGptAuditResult result = new FastGptAuditResult();

            // 保存原始内容
            result.setRawContent(content);

            // 解析顶层字段
            boolean success = businessJson.path("success").asBoolean(true);
            result.setSuccess(success);

            // summary
            String summary = businessJson.path("summary").asText();
            result.setSummary(summary);

            // findings
            JsonNode findingsNode = businessJson.path("findings");
            if (!findingsNode.isArray())
            {
                log.warn("FastGPT audit protocol invalid, chatId={}, findings is not an array", chatId);
                throw new FastGptAuditException(FastGptAuditException.ErrorCode.PROTOCOL_INVALID,
                        "响应中findings字段不是数组");
            }

            List<FastGptAuditFinding> findings = new ArrayList<>();
            for (JsonNode findingNode : findingsNode)
            {
                FastGptAuditFinding finding = parseFinding(findingNode);
                if (finding != null)
                {
                    findings.add(finding);
                }
            }
            result.setFindings(findings);

            // totalIssues
            if (businessJson.has("totalIssues"))
            {
                result.setTotalIssues(businessJson.path("totalIssues").asInt());
            }
            else
            {
                result.setTotalIssues(findings.size());
            }

            // 如果 summary 为空，生成默认摘要
            if (summary == null || summary.isEmpty())
            {
                int issueCount = findings.size();
                if (issueCount == 0)
                {
                    result.setSummary("未发现问题");
                }
                else
                {
                    result.setSummary(String.format("本次报告发现%d个问题", issueCount));
                }
            }

            return result;
        }
        catch (FastGptAuditException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.warn("FastGPT audit protocol invalid, chatId={}, contentPreview={}",
                    chatId, truncate(content, 200));
            throw new FastGptAuditException(FastGptAuditException.ErrorCode.JSON_PARSE_ERROR,
                    "解析业务JSON失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析单条 finding
     */
    private FastGptAuditFinding parseFinding(JsonNode findingNode)
    {
        String type = normalizeText(findingNode.path("type").asText());
        String title = normalizeText(findingNode.path("title").asText());
        String content = normalizeText(findingNode.path("content").asText());
        String severity = normalizeText(findingNode.path("severity").asText());
        JsonNode locationNode = findingNode.path("location");
        String location = normalizeText(toTextOrJson(locationNode));
        String suggestion = normalizeText(findingNode.path("suggestion").asText());

        // content 如果缺失，使用原始 title；如果 title 也没有，丢弃该条。
        if (content == null || content.isEmpty())
        {
            content = title;
            if (content.isEmpty())
            {
                log.warn("Finding has no content, discarding");
                return null;
            }
        }

        // 字段清洗和默认值
        if (type == null || type.isEmpty())
        {
            type = "其他";
        }

        if (title == null || title.isEmpty())
        {
            title = "AI发现问题";
        }

        if (severity == null || severity.isEmpty())
        {
            severity = "medium";
        }

        if (location == null)
        {
            location = "";
        }

        if (suggestion == null)
        {
            suggestion = "";
        }

        FastGptAuditFinding finding = new FastGptAuditFinding(type, title, content);
        finding.setSeverity(severity);
        finding.setLocation(location);
        finding.setPageNo(resolvePageNo(findingNode));
        finding.setLocationJson(toJson(locationNode));
        finding.setSuggestion(suggestion);

        return finding;
    }

    /**
     * 规范化文本字段。
     */
    private String normalizeText(String text)
    {
        return text == null ? "" : text.trim();
    }

    private Integer resolvePageNo(JsonNode findingNode)
    {
        JsonNode locationNode = findingNode.path("location");
        Integer pageNo = firstPositiveInt(locationNode.path("page"), locationNode.path("pageNo"),
                locationNode.path("page_no"), findingNode.path("page"), findingNode.path("pageNo"),
                findingNode.path("page_no"));
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
        if (text == null || text.trim().isEmpty())
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
        if (text == null || text.trim().isEmpty())
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
        catch (Exception e)
        {
            return node.asText("");
        }
    }

    /**
     * 截断字符串（用于日志）
     */
    private String truncate(String str, int maxLength)
    {
        if (str == null)
        {
            return "";
        }
        if (str.length() <= maxLength)
        {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * 脱敏 URL（用于日志）
     */
    private String maskUrl(String url)
    {
        if (url == null || url.isEmpty())
        {
            return "";
        }
        // 只显示协议和域名部分
        int doubleSlash = url.indexOf("//");
        if (doubleSlash < 0)
        {
            return "***";
        }
        int firstSlash = url.indexOf('/', doubleSlash + 2);
        if (firstSlash < 0)
        {
            return url;
        }
        return url.substring(0, firstSlash) + "/***";
    }
}
