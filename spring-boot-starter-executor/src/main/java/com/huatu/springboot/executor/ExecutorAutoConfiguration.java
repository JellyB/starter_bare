package com.huatu.springboot.executor;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author hanchao
 * @date 2017/9/12 20:46
 */
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
@ConditionalOnProperty(value = "htonline.executor.enabled",havingValue = "true",matchIfMissing = false) //必填
@Configuration
@EnableApolloConfig("htonline.executor")
@EnableConfigurationProperties(ExecutorProperties.class)
public class ExecutorAutoConfiguration {
    private final ExecutorProperties executorProperties;
    public ExecutorAutoConfiguration(ExecutorProperties executorProperties){
        this.executorProperties = executorProperties;
    }

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(executorProperties.getCorePoolSize());
        threadPoolTaskExecutor.setMaxPoolSize(executorProperties.getMaxPoolSize());
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(executorProperties.isAllowCoreThreadTimeOut());
        threadPoolTaskExecutor.setKeepAliveSeconds(executorProperties.getKeepAliveSeconds());
        threadPoolTaskExecutor.setQueueCapacity(executorProperties.getQueueCapacity());
        return threadPoolTaskExecutor;
    }
}
