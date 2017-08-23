package com.huatu.tiku.springboot.users.support;

import java.lang.annotation.*;

/**
 * @author hanchao
 * @date 2017/4/23 17:15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EnableUserSessions {

}
