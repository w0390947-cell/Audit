package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.service.audit.vector.OcrClient;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@ConditionalOnProperty(prefix = "vector.ocr", name = "enabled", havingValue = "true")
public class HttpOcrClient implements OcrClient
{
    private final VectorProperties properties;

    private final RestTemplate restTemplate;

    @Autowired
    public HttpOcrClient(VectorProperties properties, RestTemplateBuilder builder)
    {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getOcr().getConnectTimeout());
        requestFactory.setReadTimeout(properties.getOcr().getReadTimeout());
        this.restTemplate = builder.requestFactory(() -> requestFactory).build();
    }

    HttpOcrClient(VectorProperties properties, RestTemplate restTemplate)
    {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public String recognize(BufferedImage image, int pageNo)
    {
        VectorProperties.Ocr config = properties.getOcr();
        if (StringUtils.isBlank(config.getEndpoint()))
        {
            throw new IllegalStateException("OCR endpoint 未配置");
        }
        String imageBase64 = encodePng(image);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("image_base64", imageBase64);
        payload.put("image", imageBase64);
        payload.put("page_no", pageNo);
        payload.put("provider", config.getProvider());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = restTemplate.postForEntity(config.getEndpoint(),
                new HttpEntity<>(payload, headers), Map.class);
        Map body = response.getBody();
        if (body == null)
        {
            throw new IllegalStateException("OCR 响应为空");
        }
        return extractText(body);
    }

    private String encodePng(BufferedImage image)
    {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            ImageIO.write(image, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        }
        catch (Exception e)
        {
            throw new IllegalStateException("PDF 页面图片编码失败：" + e.getMessage(), e);
        }
    }

    private String extractText(Object value)
    {
        List<String> texts = new ArrayList<>();
        collectText(value, texts);
        return String.join("\n", texts);
    }

    private void collectText(Object value, List<String> texts)
    {
        if (value instanceof Map<?, ?> map)
        {
            appendString(map.get("text"), texts);
            appendString(map.get("content"), texts);
            appendString(map.get("ocr_text"), texts);
            Object data = map.get("data");
            Object result = map.get("result");
            Object results = map.get("results");
            if (data != value)
            {
                collectText(data, texts);
            }
            if (result != value)
            {
                collectText(result, texts);
            }
            if (results != value)
            {
                collectText(results, texts);
            }
            return;
        }
        if (value instanceof Iterable<?> iterable)
        {
            for (Object item : iterable)
            {
                collectText(item, texts);
            }
            return;
        }
        appendString(value, texts);
    }

    private void appendString(Object value, List<String> texts)
    {
        if (value instanceof String text && StringUtils.isNotBlank(text))
        {
            texts.add(text);
        }
    }
}
