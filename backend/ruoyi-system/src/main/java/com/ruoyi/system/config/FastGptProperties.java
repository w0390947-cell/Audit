package com.ruoyi.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * FastGPT 配置属性类
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "fastgpt")
public class FastGptProperties
{
    /**
     * FastGPT 总开关
     */
    private boolean enabled;

    /**
     * FastGPT OpenAPI 根地址
     */
    private String baseUrl;

    /**
     * FastGPT 应用特定 API Key
     */
    private String apiKey;

    /**
     * 带依据文件场景使用的 FastGPT 应用特定 API Key。
     */
    private String basisApiKey;

    /**
     * 主报告正文预处理工作流使用的 FastGPT 应用特定 API Key。
     */
    private String preprocessApiKey;

    /**
     * FastGPT 服务可访问的后端文件公开地址，如 http://192.168.1.182:6039。
     */
    private String publicFileBaseUrl;

    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;

    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 300000;

    /**
     * 是否使用流式响应
     */
    private boolean stream = false;

    /**
     * 是否返回节点详情
     */
    private boolean detail = true;

    /**
     * 默认审核标准
     */
    private String defaultAuditStandard;

    /**
     * 最大并发任务数
     */
    private int maxRunningTasks = 3;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getBaseUrl()
    {
        return normalizeBaseUrl(baseUrl);
    }

    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }

    public String getBasisApiKey()
    {
        return basisApiKey;
    }

    public void setBasisApiKey(String basisApiKey)
    {
        this.basisApiKey = basisApiKey;
    }

    public String getPreprocessApiKey()
    {
        return preprocessApiKey;
    }

    public void setPreprocessApiKey(String preprocessApiKey)
    {
        this.preprocessApiKey = preprocessApiKey;
    }

    public String getPublicFileBaseUrl()
    {
        return normalizeBaseUrl(publicFileBaseUrl);
    }

    public void setPublicFileBaseUrl(String publicFileBaseUrl)
    {
        this.publicFileBaseUrl = publicFileBaseUrl;
    }

    public int getConnectTimeout()
    {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout)
    {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout()
    {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout)
    {
        this.readTimeout = readTimeout;
    }

    public boolean isStream()
    {
        return stream;
    }

    public void setStream(boolean stream)
    {
        this.stream = stream;
    }

    public boolean isDetail()
    {
        return detail;
    }

    public void setDetail(boolean detail)
    {
        this.detail = detail;
    }

    public String getDefaultAuditStandard()
    {
        return defaultAuditStandard;
    }

    public void setDefaultAuditStandard(String defaultAuditStandard)
    {
        this.defaultAuditStandard = defaultAuditStandard;
    }

    public int getMaxRunningTasks()
    {
        return maxRunningTasks;
    }

    public void setMaxRunningTasks(int maxRunningTasks)
    {
        this.maxRunningTasks = maxRunningTasks;
    }

    /**
     * 规范化 Base URL，去除末尾的斜杠
     *
     * @param url 原始 URL
     * @return 规范化后的 URL
     */
    private String normalizeBaseUrl(String url)
    {
        if (url == null || url.isEmpty())
        {
            return url;
        }
        while (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * 获取 API Key 的脱敏字符串（用于日志）
     *
     * @return 脱敏后的 API Key，例如 ****abcd
     */
    public String getMaskedApiKey()
    {
        if (apiKey == null || apiKey.isEmpty())
        {
            return "";
        }
        if (apiKey.length() <= 4)
        {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }

    @Override
    public String toString()
    {
        return "FastGptProperties{" +
                "enabled=" + enabled +
                ", baseUrl='" + baseUrl + '\'' +
                ", apiKey='[PROTECTED]'" +
                ", basisApiKey='[PROTECTED]'" +
                ", preprocessApiKey='[PROTECTED]'" +
                ", publicFileBaseUrl='" + publicFileBaseUrl + '\'' +
                ", connectTimeout=" + connectTimeout +
                ", readTimeout=" + readTimeout +
                ", stream=" + stream +
                ", detail=" + detail +
                ", defaultAuditStandard='" + defaultAuditStandard + '\'' +
                ", maxRunningTasks=" + maxRunningTasks +
                '}';
    }
}
