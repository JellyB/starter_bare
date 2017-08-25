package com.huatu.tiku.springboot.users.support;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author hanchao
 * @date 2017/8/24 19:26
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Token {

    @AliasFor("name")
    String value() default "token";

    @AliasFor("value")
    String name() default "token";

    /**
     * required和spring集成后不代表头信息不存在，而是说最后的值存不存在，所以是uid，或者user bean
     * @return
     */
    boolean required() default true;

    /**
     * 验证session信息,会调用assert方法,可能会抛出异常
     * @return
     */
    boolean check() default true;

    String defaultValue() default "";
}
