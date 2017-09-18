package com.huatu.springboot.web.support;

import com.huatu.springboot.web.core.RegisterAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * connectionString需要放在配置中心，弃用
 * @author hanchao
 * @date 2017/9/18 13:38
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import({RegisterAutoConfiguration.class,RegisterConfigSupport.class})
@Deprecated
public @interface EnableWebRegister {
    /**
     * 注册连接字符串
     * @return
     */
    @AliasFor("connectString")
    String value() default "";

    @AliasFor("value")
    String connectString() default "";

    String[] preferedNetworks() default {};

    int port() default -1;

}
