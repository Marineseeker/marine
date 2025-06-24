package com.marine.manage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Spring事务配置类
 * 展示如何配置事务管理器和其他事务相关设置
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * 配置事务管理器
     * 这里使用默认的DataSourceTransactionManager，适用于单数据源
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        
        // 设置默认超时时间（秒）
        transactionManager.setDefaultTimeout(30);
        
        // 设置是否允许嵌套事务
        transactionManager.setNestedTransactionAllowed(true);
        
        // 设置是否在回滚时抛出异常
        transactionManager.setRollbackOnCommitFailure(true);
        
        return transactionManager;
    }
} 