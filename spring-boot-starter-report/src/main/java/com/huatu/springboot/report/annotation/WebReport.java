package com.huatu.springboot.report.annotation;

import java.lang.annotation.*;

/**
 * @author hanchao
 * @date 2018/1/15 10:39
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface WebReport {
    String value() default "";
    Class<?> extraHandler() default Object.class;
    boolean holdResult() default false;
}
