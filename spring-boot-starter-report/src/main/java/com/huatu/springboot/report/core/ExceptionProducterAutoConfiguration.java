package com.huatu.springboot.report.core;

import com.huatu.springboot.report.product.ExceptionProducter;
import com.huatu.springboot.web.tools.exception.GlobalExceptionHandler;
import com.huatu.springboot.web.tools.indicator.ExceptionHealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author hanchao
 * @date 2018/1/16 16:23
 */
@Configuration
@AutoConfigureAfter(GlobalExceptionHandler.class)
@ConditionalOnBean(ExceptionHealthIndicator.class)
public class ExceptionProducterAutoConfiguration {
    @Bean
    public ExceptionProducter exceptionProducter(){
        return new ExceptionProducter();
    }
}
