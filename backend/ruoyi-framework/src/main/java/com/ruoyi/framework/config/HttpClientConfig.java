package com.ruoyi.framework.config;

import com.ruoyi.system.config.FastGptProperties;
import com.ruoyi.system.config.AuditWorkflowProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端配置
 *
 * @author ruoyi
 */
@Configuration
public class HttpClientConfig
{
    /**
     * FastGPT 专用 RestTemplate Bean
     *
     * @param builder RestTemplate 构建器
     * @return 配置好的 RestTemplate 实例
     */
    @Bean
    public RestTemplate fastGptRestTemplate(RestTemplateBuilder builder, FastGptProperties properties)
    {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        return builder
                .requestFactory(() -> requestFactory)
                .build();
    }

    @Bean
    public RestTemplate auditWorkflowRestTemplate(RestTemplateBuilder builder, AuditWorkflowProperties properties)
    {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeout());
        requestFactory.setReadTimeout(properties.getReadTimeout());
        return builder
                .requestFactory(() -> requestFactory)
                .build();
    }
}
