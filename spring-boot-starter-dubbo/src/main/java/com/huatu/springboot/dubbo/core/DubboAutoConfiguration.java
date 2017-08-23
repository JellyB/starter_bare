package com.huatu.springboot.dubbo.core;

import com.alibaba.dubbo.config.*;
import com.huatu.springboot.dubbo.support.EnableDubboApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(DubboProperties.class)
@ConditionalOnBean(annotation = EnableDubboApplication.class)
@Import(DubboHealthConfiguration.class)
public class DubboAutoConfiguration {

    @Bean
    public ApplicationConfig requestApplicationConfig(DubboProperties dubboProperties) {
        return dubboProperties.getApplication() == null ? new ApplicationConfig() : dubboProperties.getApplication();
    }

    @Bean
    public RegistryConfig requestRegistryConfig(DubboProperties dubboProperties) {
        return dubboProperties.getRegistry() == null ? new RegistryConfig() : dubboProperties.getRegistry();
    }

    @Bean
    public ProtocolConfig requestProtocolConfig(DubboProperties dubboProperties) {
        return dubboProperties.getProtocol() == null ? new ProtocolConfig() : dubboProperties.getProtocol();
    }

    @Bean
    public ProviderConfig requestProviderConfig(DubboProperties dubboProperties) {
        return dubboProperties.getProvider() == null ? new ProviderConfig() : dubboProperties.getProvider();
    }

    @Bean
    public ConsumerConfig requestConsumerConfig(DubboProperties dubboProperties) {
        return dubboProperties.getConsumer() == null ? new ConsumerConfig() : dubboProperties.getConsumer();
    }

}
