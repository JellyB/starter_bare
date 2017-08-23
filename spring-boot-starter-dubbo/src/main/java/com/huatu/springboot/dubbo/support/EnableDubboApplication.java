package com.huatu.springboot.dubbo.support;

import java.lang.annotation.*;

/**
 * @author hanchao
 * @date 2017/4/23 17:15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EnableDubboApplication {
    /**
     * 扫描包
     * @return
     */
    String value() default "";

    /**
     * 装配到factory中，可以使用autowire,意义不大
     * @return
     */
    @Deprecated
    boolean buildFactory() default false;
}
