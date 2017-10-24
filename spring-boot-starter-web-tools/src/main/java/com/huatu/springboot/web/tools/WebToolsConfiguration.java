package com.huatu.springboot.web.tools;

import com.huatu.common.spring.event.EventPublisher;
import com.huatu.springboot.web.tools.advice.AdviceExcluder;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;

/**
 * @author hanchao
 * @date 2017/10/13 19:34
 */
@Configuration
public class WebToolsConfiguration {
    public static String ERROR_TEMPALTE = "";
    static {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource errorTemplate = resourcePatternResolver.getResource("classpath:static/error/template.html");
        try {
            ERROR_TEMPALTE = IOUtils.toString(errorTemplate.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Bean
    public EventPublisher eventPublisher(@Autowired(required = false) @Qualifier("coreThreadPool") ThreadPoolTaskExecutor threadPoolTaskExecutor){
        if(threadPoolTaskExecutor != null){
            return new EventPublisher(threadPoolTaskExecutor);
        }
        return new EventPublisher();
    }

    @Bean
    @ConditionalOnMissingBean(AdviceExcluder.class)
    public AdviceExcluder adviceExcluder(){
        return new AdviceExcluder();
    }

    @Configuration
    static class GlobalWebMvcConfig extends WebMvcConfigurerAdapter {
        @Override
        public void addViewControllers(ViewControllerRegistry registry) {
            super.addViewControllers(registry);
        }
    }

    //替换WhitelabelErrorView，同时方便在异常处理直接使用，而不必走请求转发的流程
//    @Bean(name="error")
//    public View errorView(){
//        return new SpelView
//    }




//    @Bean(name = "error")
//    public View errorView(){
//
//    }
}
