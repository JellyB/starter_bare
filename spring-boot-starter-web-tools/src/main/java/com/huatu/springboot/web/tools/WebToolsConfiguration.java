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
            super.addViewControllers(registry);
        }
    }


//    @Bean(name = "error")
//    public View errorView(){
//
//    }
}
