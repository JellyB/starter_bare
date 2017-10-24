package com.huatu.springboot.web.tools;

import com.huatu.common.spring.event.EventPublisher;
import com.huatu.springboot.web.tools.advice.AdviceExcluder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author hanchao
 * @date 2017/10/13 19:34
 */
@Configuration
public class WebToolsConfiguration {
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
            //common静态视图
            registry.addViewController("/common/error/200").setViewName("/static/error/200.html");
            registry.addViewController("/common/error/400").setViewName("/static/error/400.html");
            registry.addViewController("/common/error/401").setViewName("/static/error/401.html");
            registry.addViewController("/common/error/404").setViewName("/static/error/404.html");
            registry.addViewController("/common/error/500").setViewName("/static/error/500.html");
            super.addViewControllers(registry);
        }
    }
}
