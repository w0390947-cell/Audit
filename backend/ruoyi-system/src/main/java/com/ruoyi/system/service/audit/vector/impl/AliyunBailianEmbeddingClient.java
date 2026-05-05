package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.vector.EmbeddingRequest;
import com.ruoyi.system.domain.audit.vector.EmbeddingResponse;
import com.ruoyi.system.service.audit.vector.EmbeddingClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
@ConditionalOnProperty(prefix = "vector", name = "enabled", havingValue = "true")
public class AliyunBailianEmbeddingClient implements EmbeddingClient
{
    private final VectorProperties properties;

    private final RestTemplate restTemplate;

    @Autowired
    public AliyunBailianEmbeddingClient(VectorProperties properties, RestTemplateBuilder builder)
    {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getEmbedding().getConnectTimeout());
        requestFactory.setReadTimeout(properties.getEmbedding().getReadTimeout());
        this.restTemplate = builder.requestFactory(() -> requestFactory).build();
    }

    AliyunBailianEmbeddingClient(VectorProperties properties, RestTemplate restTemplate)
    {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<float[]> embed(List<String> texts)
    {
        if (texts == null || texts.isEmpty())
        {
            return new ArrayList<>();
        }
        VectorProperties.ModelConfig config = properties.getEmbedding();
        if (StringUtils.isBlank(config.getApiKey()))
        {
            throw new IllegalStateException("DASHSCOPE_API_KEY 未配置");
        }
        if (StringUtils.isBlank(config.getBaseUrl()))
        {
            throw new IllegalStateException("Embedding base-url 未配置");
        }
        if (StringUtils.isBlank(config.getModel()))
        {
            throw new IllegalStateException("Embedding model 未配置");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        EmbeddingRequest request = new EmbeddingRequest(config.getModel(), texts, expectedDimensions(config));
        ResponseEntity<EmbeddingResponse> response = restTemplate.postForEntity(
                config.getBaseUrl() + "/embeddings",
                new HttpEntity<>(request, headers),
                EmbeddingResponse.class);
        EmbeddingResponse body = response.getBody();
        if (body == null || body.getData() == null)
        {
            throw new IllegalStateException("Embedding 响应为空");
        }
        if (body.getData().size() != texts.size())
        {
            throw new IllegalStateException("Embedding 返回数量不匹配");
        }

        body.getData().sort(Comparator.comparing(item -> item.getIndex() == null ? 0 : item.getIndex()));
        List<float[]> vectors = new ArrayList<>();
        for (EmbeddingResponse.Data item : body.getData())
        {
            vectors.add(toVector(item.getEmbedding(), expectedDimensions(config)));
        }
        return vectors;
    }

    private float[] toVector(List<Double> values, int expectedDimensions)
    {
        if (values == null || values.size() != expectedDimensions)
        {
            int actual = values == null ? 0 : values.size();
            throw new IllegalStateException("Embedding 维度不匹配，期望 " + expectedDimensions + "，实际 " + actual);
        }
        float[] vector = new float[values.size()];
        for (int i = 0; i < values.size(); i++)
        {
            vector[i] = values.get(i).floatValue();
        }
        return vector;
    }

    private int expectedDimensions(VectorProperties.ModelConfig config)
    {
        return config.getDimensions() == null ? 1024 : config.getDimensions();
    }
}
