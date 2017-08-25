package com.huatu.springboot.demo;

import com.huatu.tiku.springboot.users.support.TokenMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author hanchao
 * @date 2017/8/25 9:07
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter{
    @Autowired
    private TokenMethodArgumentResolver tokenMethodArgumentResolver;
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(tokenMethodArgumentResolver);
        super.addArgumentResolvers(argumentResolvers);
    }
}
