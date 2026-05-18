package com.audit.workflow.vector;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowEmbeddingClient {

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int dimensions;
    private final int batchSize;
    private final int timeoutSeconds;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WorkflowEmbeddingClient(@Value("${audit.temp-vector.embedding.base-url:}") String baseUrl,
                                   @Value("${audit.temp-vector.embedding.api-key:}") String apiKey,
                                   @Value("${audit.temp-vector.embedding.model:text-embedding-v4}") String model,
                                   @Value("${audit.temp-vector.embedding.dimensions:1024}") int dimensions,
                                   @Value("${audit.temp-vector.embedding.batch-size:8}") int batchSize,
                                   @Value("${audit.temp-vector.embedding.timeout-seconds:60}") int timeoutSeconds,
                                   ObjectMapper objectMapper) {
        this.baseUrl = trimRight(baseUrl);
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model == null ? "" : model.trim();
        this.dimensions = dimensions;
        this.batchSize = Math.max(1, Math.min(10, batchSize));
        this.timeoutSeconds = Math.max(1, timeoutSeconds);
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(this.timeoutSeconds))
                .build();
    }

    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }
        if (baseUrl.isBlank()) {
            throw new IllegalStateException("临时向量 embedding base-url 未配置");
        }
        if (apiKey.isBlank()) {
            throw new IllegalStateException("临时向量 embedding api-key 未配置");
        }
        if (model.isBlank()) {
            throw new IllegalStateException("临时向量 embedding model 未配置");
        }

        List<float[]> embeddings = new ArrayList<>();
        for (int start = 0; start < texts.size(); start += batchSize) {
            List<String> batch = texts.subList(start, Math.min(start + batchSize, texts.size()));
            embeddings.addAll(embedBatch(batch));
        }
        if (embeddings.size() != texts.size()) {
            throw new IllegalStateException("Embedding 返回数量不匹配，期望 " + texts.size() + "，实际 " + embeddings.size());
        }
        return embeddings;
    }

    public String model() {
        return model;
    }

    public int dimensions() {
        return dimensions;
    }

    private List<float[]> embedBatch(List<String> texts) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("input", texts);
            if (dimensions > 0) {
                payload.put("dimensions", dimensions);
            }
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/embeddings"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Embedding HTTP " + response.statusCode() + ": " + abbreviate(response.body(), 500));
            }
            Map<?, ?> body = objectMapper.readValue(response.body(), Map.class);
            Object data = body.get("data");
            if (!(data instanceof List<?> dataList)) {
                throw new IllegalStateException("Embedding 响应 data 为空");
            }
            List<Map<?, ?>> items = new ArrayList<>();
            for (Object item : dataList) {
                if (item instanceof Map<?, ?> map) {
                    items.add(map);
                }
            }
            items.sort(Comparator.comparingInt(item -> intValue(item.get("index"))));
            List<float[]> vectors = new ArrayList<>();
            for (Map<?, ?> item : items) {
                vectors.add(toVector(item.get("embedding")));
            }
            return vectors;
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException stateException) {
                throw stateException;
            }
            throw new IllegalStateException("Embedding 调用失败: " + ex.getMessage(), ex);
        }
    }

    private float[] toVector(Object value) {
        if (!(value instanceof List<?> values)) {
            throw new IllegalStateException("Embedding 向量为空");
        }
        if (dimensions > 0 && values.size() != dimensions) {
            throw new IllegalStateException("Embedding 维度不匹配，期望 " + dimensions + "，实际 " + values.size());
        }
        float[] vector = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            vector[i] = Float.parseFloat(String.valueOf(values.get(i)));
        }
        return vector;
    }

    private int intValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return 0;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private String abbreviate(String value, int maxLength) {
        String text = value == null ? "" : value;
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private String trimRight(String value) {
        String text = value == null ? "" : value.trim();
        while (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
