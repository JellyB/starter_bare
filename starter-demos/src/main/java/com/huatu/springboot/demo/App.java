package com.huatu.springboot.demo;

import com.huatu.tiku.springboot.users.support.EnableUserSessions;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author hanchao
 * @date 2017/8/22 14:03
 */
@EnableAutoConfiguration
@ComponentScan("com.huatu.springboot.demo")
@EnableConfigurationProperties({UserBean.class,TestBean.class})
@Configuration
@EnableUserSessions
public class App {
    public static void main(String[] args){
        new SpringApplicationBuilder().web(true).build().run(App.class,args);
    }
}
