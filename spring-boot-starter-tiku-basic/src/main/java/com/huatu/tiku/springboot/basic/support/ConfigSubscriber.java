package com.huatu.tiku.springboot.basic.support;

import com.ctrip.framework.apollo.model.ConfigChange;

/**
 * @author hanchao
 * @date 2017/10/6 14:10
 */
public interface ConfigSubscriber {
    void update(ConfigChange configChange);
}
