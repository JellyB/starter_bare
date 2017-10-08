package com.huatu.springboot.cache.core;

import com.huatu.common.spring.cache.CachedInfosBuilder;
import com.huatu.springboot.cache.spel.SpelExecutor;
import com.huatu.springboot.cache.support.web.CacheManageBootEndpoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author hanchao
 * @date 2017/10/7 14:35
 */
@Configuration
@ConditionalOnProperty(name = "htonline.cache-manage.enabled",matchIfMissing = true)
public class CacheManageAutoConfiguration {
    @Value("${htonline.cache-manage.basePackage:}")
    private String basePackage;

    @Bean
    public CachedInfosBuilder cachedInfosBuilder(){
        return new CachedInfosBuilder(basePackage);
    }

    @Bean
    public SpelExecutor spelExecutor(){
        return new SpelExecutor();
    }

    @Configuration
    @ConditionalOnWebApplication
    protected static class WebEnvConfiguration {
        @Bean
        public CacheManageBootEndpoint cacheManageBootEndpoint(){
            return new CacheManageBootEndpoint();
        }
    }

}
