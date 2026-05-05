package com.ruoyi.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 审核文件库向量化配置属性。
 */
@Component
@ConfigurationProperties(prefix = "vector")
public class VectorProperties
{
    private boolean enabled = false;

    private Datasource datasource = new Datasource();

    private Chunk chunk = new Chunk();

    private ModelConfig embedding = new ModelConfig();

    private ModelConfig chat = new ModelConfig();

    private Task task = new Task();

    private Lifecycle lifecycle = new Lifecycle();

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public Datasource getDatasource()
    {
        return datasource;
    }

    public void setDatasource(Datasource datasource)
    {
        this.datasource = datasource;
    }

    public Chunk getChunk()
    {
        return chunk;
    }

    public void setChunk(Chunk chunk)
    {
        this.chunk = chunk;
    }

    public ModelConfig getEmbedding()
    {
        return embedding;
    }

    public void setEmbedding(ModelConfig embedding)
    {
        this.embedding = embedding;
    }

    public ModelConfig getChat()
    {
        return chat;
    }

    public void setChat(ModelConfig chat)
    {
        this.chat = chat;
    }

    public Task getTask()
    {
        return task;
    }

    public void setTask(Task task)
    {
        this.task = task;
    }

    public Lifecycle getLifecycle()
    {
        return lifecycle;
    }

    public void setLifecycle(Lifecycle lifecycle)
    {
        this.lifecycle = lifecycle;
    }

    public static class Datasource
    {
        private String url;

        private String username;

        private String password;

        private String driverClassName;

        public String getUrl()
        {
            return url;
        }

        public void setUrl(String url)
        {
            this.url = url;
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        public String getDriverClassName()
        {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName)
        {
            this.driverClassName = driverClassName;
        }
    }

    public static class Chunk
    {
        private int maxChars = 1200;

        private int overlapChars = 150;

        private int minChars = 80;

        public int getMaxChars()
        {
            return maxChars;
        }

        public void setMaxChars(int maxChars)
        {
            this.maxChars = maxChars;
        }

        public int getOverlapChars()
        {
            return overlapChars;
        }

        public void setOverlapChars(int overlapChars)
        {
            this.overlapChars = overlapChars;
        }

        public int getMinChars()
        {
            return minChars;
        }

        public void setMinChars(int minChars)
        {
            this.minChars = minChars;
        }
    }

    public static class ModelConfig
    {
        private String provider;

        private String apiMode;

        private String baseUrl;

        private String apiKey;

        private String model;

        private Integer dimensions;

        private int batchSize = 16;

        private int connectTimeout = 10000;

        private int readTimeout = 60000;

        public String getProvider()
        {
            return provider;
        }

        public void setProvider(String provider)
        {
            this.provider = provider;
        }

        public String getApiMode()
        {
            return apiMode;
        }

        public void setApiMode(String apiMode)
        {
            this.apiMode = apiMode;
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

        public String getModel()
        {
            return model;
        }

        public void setModel(String model)
        {
            this.model = model;
        }

        public Integer getDimensions()
        {
            return dimensions;
        }

        public void setDimensions(Integer dimensions)
        {
            this.dimensions = dimensions;
        }

        public int getBatchSize()
        {
            return batchSize;
        }

        public void setBatchSize(int batchSize)
        {
            this.batchSize = batchSize;
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
    }

    public static class Task
    {
        private boolean enabled = false;

        private long fixedDelay = 10000L;

        private int batchSize = 3;

        private int maxRunning = 2;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }

        public long getFixedDelay()
        {
            return fixedDelay;
        }

        public void setFixedDelay(long fixedDelay)
        {
            this.fixedDelay = fixedDelay;
        }

        public int getBatchSize()
        {
            return batchSize;
        }

        public void setBatchSize(int batchSize)
        {
            this.batchSize = batchSize;
        }

        public int getMaxRunning()
        {
            return maxRunning;
        }

        public void setMaxRunning(int maxRunning)
        {
            this.maxRunning = maxRunning;
        }
    }

    public static class Lifecycle
    {
        private boolean enabled = true;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;
        }
    }

    private static String normalizeBaseUrl(String value)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/"))
        {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
