package com.ruoyi.framework.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceBuilder;

/**
 * 审核文件库向量库 PostgreSQL 数据源配置。
 */
@Configuration
@ConditionalOnProperty(prefix = "vector", name = "enabled", havingValue = "true")
public class VectorDataSourceConfig
{
    @Bean(name = "vectorDataSource")
    @ConfigurationProperties(prefix = "vector.datasource")
    public DataSource vectorDataSource()
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        dataSource.setValidationQuery("SELECT 1");
        return dataSource;
    }

    @Bean(name = "vectorJdbcTemplate")
    public JdbcTemplate vectorJdbcTemplate(@Qualifier("vectorDataSource") DataSource vectorDataSource)
    {
        return new JdbcTemplate(vectorDataSource);
    }
}
