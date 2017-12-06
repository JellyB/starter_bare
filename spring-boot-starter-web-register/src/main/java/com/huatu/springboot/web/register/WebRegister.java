package com.huatu.springboot.web.register;

/**
 * @author hanchao
 * @date 2017/9/18 13:11
 */
public interface WebRegister {

    boolean regist();

    boolean unregister();

    default boolean pause(){
        return unregister();
    }

    default boolean resume(){
        return regist();
    }
}
