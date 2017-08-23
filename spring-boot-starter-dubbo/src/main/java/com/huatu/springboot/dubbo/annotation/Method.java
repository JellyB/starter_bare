package com.huatu.springboot.dubbo.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Dubbo方法配置
 * Created by roc on 2017/3/30.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Method {

    /**
     * @return 方法名
     */
    String name();

    /**
     * @return 负载均衡策略
     */
    String loadbalance() default "";

    /**
     * @return 远程服务调用重试次数
     */
    int retries() default -1;

    /**
     * @return 方法使用线程数限制
     */
    int executes() default 0;

    /**
     * @return 每服务消费者最大并发调用限制
     */
    int actives() default 0;

    /**
     * @return 是否过时
     */
    boolean deprecated() default false;

    /**
     * @return 是否需要开启stiky策略
     */
    boolean sticky() default false;

    /**
     * @return 是否需要返回
     */
    boolean isReturn() default true;

    /**
     * @return 方法调用超时时间(毫秒)
     */
    int timeout() default -1;

    boolean async() default false;
    boolean sent() default true;

    String cache() default "";

    boolean validation() default false;
}