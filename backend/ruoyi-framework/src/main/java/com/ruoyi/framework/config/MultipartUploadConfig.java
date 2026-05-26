package com.ruoyi.framework.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

/**
 * 文件上传配置
 */
@Configuration
public class MultipartUploadConfig
{
    /**
     * 放开 Spring Boot multipart 上传大小限制。
     */
    @Bean
    public MultipartConfigElement multipartConfigElement()
    {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(-1));
        factory.setMaxRequestSize(DataSize.ofBytes(-1));
        return factory.createMultipartConfig();
    }
}
