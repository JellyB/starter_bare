package com.huatu.tiku.springboot.basic.support;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;

import java.util.Map;

/**
 * 监听配置中心的配置变化
 * @author hanchao
 * @date 2017/10/6 14:00
 */
public class BasicConfigListener {
    private Map<String,ConfigSubscriber> configSubscriberMap;

    public BasicConfigListener(Map<String,ConfigSubscriber> subscriberMap){
        this.configSubscriberMap = subscriberMap;
    }

    /**
     * 变化通知
     * @param changeEvent
     */
    @ApolloConfigChangeListener("tiku.basic")
    private void listener(ConfigChangeEvent changeEvent){
        configSubscriberMap.forEach((k,v) -> {
            if(changeEvent.isChanged(k)){
                v.update(changeEvent.getChange(k));
            }
        });
    }


}
