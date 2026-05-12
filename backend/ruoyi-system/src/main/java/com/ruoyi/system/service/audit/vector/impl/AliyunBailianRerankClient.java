package com.ruoyi.system.service.audit.vector.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.VectorProperties;
import com.ruoyi.system.domain.audit.vector.RerankRequest;
import com.ruoyi.system.domain.audit.vector.RerankResponse;
import com.ruoyi.system.domain.audit.vector.RerankResult;
import com.ruoyi.system.service.audit.vector.RerankClient;
import java.util.ArrayList;
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
public class AliyunBailianRerankClient implements RerankClient
{
    private final VectorProperties properties;

    private final RestTemplate restTemplate;

    @Autowired
    public AliyunBailianRerankClient(VectorProperties properties, RestTemplateBuilder builder)
    {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getReranker().getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReranker().getReadTimeout());
        this.restTemplate = builder.requestFactory(() -> requestFactory).build();
    }

    AliyunBailianRerankClient(VectorProperties properties, RestTemplate restTemplate)
    {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public List<RerankResult> rerank(String query, List<String> documents, int topN)
    {
        if (documents == null || documents.isEmpty())
        {
            return new ArrayList<>();
        }
        VectorProperties.Reranker config = properties.getReranker();
        if (StringUtils.isBlank(config.getApiKey()))
        {
            throw new IllegalStateException("Reranker api-key 未配置");
        }
        if (StringUtils.isBlank(config.getBaseUrl()))
        {
            throw new IllegalStateException("Reranker base-url 未配置");
        }
        if (StringUtils.isBlank(config.getModel()))
        {
            throw new IllegalStateException("Reranker model 未配置");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        RerankRequest request = new RerankRequest(config.getModel(), query, documents,
                Math.max(1, Math.min(topN, documents.size())), Boolean.FALSE,
                StringUtils.isBlank(config.getInstruct()) ? null : config.getInstruct());
        ResponseEntity<RerankResponse> response = restTemplate.postForEntity(
                config.getBaseUrl() + "/reranks",
                new HttpEntity<>(request, headers),
                RerankResponse.class);
        RerankResponse body = response.getBody();
        if (body == null || body.getResults() == null)
        {
            throw new IllegalStateException("Reranker 响应为空");
        }

        List<RerankResult> results = new ArrayList<>();
        for (RerankResponse.Result item : body.getResults())
        {
            if (item == null || item.getIndex() == null || item.getRelevanceScore() == null)
            {
                continue;
            }
            RerankResult result = new RerankResult();
            result.setIndex(item.getIndex());
            result.setScore(item.getRelevanceScore());
            results.add(result);
        }
        return results;
    }
}
