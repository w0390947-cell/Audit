package com.audit.workflow.service;

import com.audit.workflow.common.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HttpOcrClient implements OcrClient {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final String endpoint;
    private final String provider;
    private final Duration timeout;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public HttpOcrClient(@Value("${audit.ocr.endpoint:http://127.0.0.1:8866/ocr}") String endpoint,
                         @Value("${audit.ocr.provider:paddle}") String provider,
                         @Value("${audit.ocr.timeout-seconds:600}") int timeoutSeconds,
                         ObjectMapper objectMapper) {
        this.endpoint = endpoint == null ? "" : endpoint.trim();
        this.provider = provider == null ? "" : provider.trim();
        this.timeout = Duration.ofSeconds(Math.max(1, timeoutSeconds));
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
    }

    @Override
    public String recognize(BufferedImage image, int pageNo) {
        if (endpoint.isBlank()) {
            throw new BusinessException("OCR_NOT_CONFIGURED", "OCR endpoint 未配置");
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            String imageBase64 = encodePng(image);
            payload.put("image_base64", imageBase64);
            payload.put("image", imageBase64);
            payload.put("page_no", pageNo);
            payload.put("provider", provider);

            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException("OCR_FAILED", "OCR failed, http status " + response.statusCode());
            }
            Map<String, Object> body = objectMapper.readValue(response.body(), MAP_TYPE);
            String text = extractText(body);
            if (text.isBlank()) {
                return "";
            }
            return text;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("OCR_FAILED", "OCR failed: " + ex.getMessage());
        }
    }

    private String encodePng(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception ex) {
            throw new BusinessException("OCR_IMAGE_ENCODE_FAILED", "OCR image encode failed: " + ex.getMessage());
        }
    }

    private String extractText(Map<String, Object> body) {
        List<String> texts = new ArrayList<>();
        collectText(body, texts);
        return String.join("\n", texts).trim();
    }

    private void collectText(Object value, List<String> texts) {
        if (value instanceof Map<?, ?> map) {
            appendString(map.get("text"), texts);
            appendString(map.get("content"), texts);
            appendString(map.get("ocr_text"), texts);
            collectText(map.get("data"), texts);
            collectText(map.get("result"), texts);
            collectText(map.get("results"), texts);
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                collectText(item, texts);
            }
        }
    }

    private void appendString(Object value, List<String> texts) {
        if (value instanceof String text && !text.isBlank()) {
            texts.add(text);
        }
    }
}
