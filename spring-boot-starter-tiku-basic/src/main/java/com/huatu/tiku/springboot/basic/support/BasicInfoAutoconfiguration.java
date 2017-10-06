package com.huatu.tiku.springboot.basic.support;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.google.common.collect.ImmutableMap;
import com.huatu.tiku.springboot.basic.subject.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author hanchao
 * @date 2017/10/6 10:33
 */
@Configuration
@EnableApolloConfig("tiku.basic")
@EnableConfigurationProperties(BasicProperties.class)
public class BasicInfoAutoconfiguration {
    private BasicProperties basicProperties;
    public BasicInfoAutoconfiguration(BasicProperties basicProperties){
        this.basicProperties = basicProperties;
    }


    @Bean
    public SubjectService subjectService() throws IOException {
        return new SubjectService(basicProperties.getSubjects());
    }


    @Bean
    public BasicConfigListener basicConfigListener(@Autowired SubjectService subjectService){
        ImmutableMap<String, ConfigSubscriber> configSubscriberMap = ImmutableMap.of("tiku.basic.subjects", subjectService);
        BasicConfigListener basicConfigListener = new BasicConfigListener(configSubscriberMap);
        return basicConfigListener;
    }
}