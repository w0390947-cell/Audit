package com.ruoyi.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 向量库连接验证。仅在 vector.enabled=true 时执行。
 */
@Component
@ConditionalOnProperty(prefix = "vector", name = "enabled", havingValue = "true")
public class VectorConnectionVerifier implements ApplicationRunner
{
    private static final Logger log = LoggerFactory.getLogger(VectorConnectionVerifier.class);

    private final JdbcTemplate vectorJdbcTemplate;

    public VectorConnectionVerifier(@Qualifier("vectorJdbcTemplate") JdbcTemplate vectorJdbcTemplate)
    {
        this.vectorJdbcTemplate = vectorJdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args)
    {
        Integer connected = vectorJdbcTemplate.queryForObject("SELECT 1", Integer.class);
        String vectorExt = vectorJdbcTemplate.query(
                "SELECT extname FROM pg_extension WHERE extname = 'vector'",
                rs -> rs.next() ? rs.getString("extname") : null);
        if (connected == null || connected != 1 || vectorExt == null)
        {
            throw new IllegalStateException("PostgreSQL vector datasource is available, but pgvector extension is not enabled.");
        }
        log.info("PostgreSQL vector datasource verified, pgvector extension enabled.");
    }
}
