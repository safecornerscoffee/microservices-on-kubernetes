package com.safecornerscoffee.microservices.core.review.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;

@Configuration
public class JDBCConfiguration
{

    private final static Logger LOG = LoggerFactory.getLogger(JDBCConfiguration.class);

    @Value("${spring.datasource.maximum-pool-size:10}")
    private Integer connectionPoolSize;

    @Bean
    public Scheduler jdbcScheduler() {
        LOG.info("creates a jdbcScheduler with connectionPoolSize = {}", connectionPoolSize);
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize));
    }
}
