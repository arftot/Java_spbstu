package com.alina.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableScheduling
@Profile({"scheduling", "default"})
public class AsyncSchedulerConfig {
    private static final Logger logger = LoggerFactory.getLogger(AsyncSchedulerConfig.class);
    
    private final AsyncProperties asyncProperties;

    public AsyncSchedulerConfig(AsyncProperties asyncProperties) {
        this.asyncProperties = asyncProperties;
    }

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        logger.info("Configuring async executor with corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                   asyncProperties.getCorePoolSize(), asyncProperties.getMaxPoolSize(), asyncProperties.getQueueCapacity());
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncProperties.getQueueCapacity());
        executor.setThreadNamePrefix("AsyncTask-");
        executor.initialize();
        return executor;
    }
}
