package com.audit.workflow.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class OpenAiCompatibleModelGateway implements ModelGateway {

    private final String provider;
    private final String baseUrl;
    private final String apiKey;
    private final String defaultModel;
    private final int timeoutSeconds;
    private final boolean enableThinking;
    private final boolean responseFormatJson;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleModelGateway(@Value("${audit.model.provider:aliyun-bailian}") String provider,
                                        @Value("${audit.model.base-url:}") String baseUrl,
                                        @Value("${audit.model.api-key:}") String apiKey,
                                        @Value("${audit.model.default-chat-model:qwen-plus}") String defaultModel,
                                        @Value("${audit.model.timeout-seconds:120}") int timeoutSeconds,
                                        @Value("${audit.model.enable-thinking:false}") boolean enableThinking,
                                        @Value("${audit.model.response-format-json:false}") boolean responseFormatJson,
                                        ObjectMapper objectMapper) {
        this.provider = provider;
        this.baseUrl = trimRight(baseUrl);
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.timeoutSeconds = timeoutSeconds;
        this.enableThinking = enableThinking;
        this.responseFormatJson = responseFormatJson;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Override
    public ModelResponse chat(ModelRequest request) {
        String modelName = request.getModelName() == null || request.getModelName().isBlank()
                ? defaultModel
                : request.getModelName();
        long started = System.currentTimeMillis();
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            String content = """
                    {"summary":{"overall_result":"待人工复核","risk_level":"low","total_issues":0},"issues":[]}
                    """;
            return ModelResponse.success("LOCAL-MOCK-" + UUID.randomUUID(), provider, modelName, content,
                    estimateTokens(request.getSystemPrompt()) + estimateTokens(request.getUserPrompt()),
                    estimateTokens(content),
                    System.currentTimeMillis() - started);
        }

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", modelName);
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", value(request.getSystemPrompt())),
                    Map.of("role", "user", "content", value(request.getUserPrompt()))
            ));
            payload.put("temperature", 0.1);
            if (modelName.startsWith("qwen3.5") || modelName.startsWith("qwen3.6")) {
                payload.put("enable_thinking", enableThinking);
            }
            if (responseFormatJson) {
                payload.put("response_format", Map.of("type", "json_object"));
            }
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            long durationMs = System.currentTimeMillis() - started;
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return ModelResponse.failure(provider, modelName, "MODEL_UNAVAILABLE",
                        "model http status " + response.statusCode() + ": " + abbreviate(response.body(), 500), durationMs);
            }
            Map<?, ?> body = objectMapper.readValue(response.body(), Map.class);
            String requestId = value(body.get("id"));
            String content = extractContent(body);
            int inputTokens = 0;
            int outputTokens = 0;
            Object usage = body.get("usage");
            if (usage instanceof Map<?, ?> usageMap) {
                inputTokens = intValue(usageMap.get("prompt_tokens"));
                outputTokens = intValue(usageMap.get("completion_tokens"));
            }
            return ModelResponse.success(requestId, provider, modelName, content, inputTokens, outputTokens, durationMs);
        } catch (java.net.http.HttpTimeoutException ex) {
            return ModelResponse.failure(provider, modelName, "MODEL_TIMEOUT", ex.getMessage(), System.currentTimeMillis() - started);
        } catch (Exception ex) {
            String message = ex.getClass().getSimpleName() + ": " + value(ex.getMessage());
            return ModelResponse.failure(provider, modelName, "MODEL_UNAVAILABLE", message, System.currentTimeMillis() - started);
        }
    }

    private String extractContent(Map<?, ?> body) {
        Object choices = body.get("choices");
        if (choices instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> choice) {
            Object message = choice.get("message");
            if (message instanceof Map<?, ?> messageMap) {
                return value(messageMap.get("content"));
            }
        }
        return "";
    }

    private int estimateTokens(String text) {
        return text == null ? 0 : Math.max(1, text.length() / 2);
    }

    private int intValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String abbreviate(String value, int maxLength) {
        String text = value(value);
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private String trimRight(String value) {
        if (value == null) {
            return "";
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
