package com.huatu.springboot.dubbo.core;

import com.huatu.springboot.dubbo.support.DubboHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hanchao
 * @date 2017/5/6 13:32
 */
@Configuration
@ConditionalOnClass(HealthEndpoint.class)
@AutoConfigureAfter(EndpointAutoConfiguration.class)
public class DubboHealthConfiguration {
    /**
     * management.health.dubbo.......
     * @return
     */
    @Bean
    @ConditionalOnEnabledHealthIndicator("dubbo")
    public DubboHealthIndicator dubboHealthIndicator(){
        return new DubboHealthIndicator();
    }
}
