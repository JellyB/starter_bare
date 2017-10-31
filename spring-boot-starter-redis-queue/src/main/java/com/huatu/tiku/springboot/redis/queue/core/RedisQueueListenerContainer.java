package com.huatu.tiku.springboot.redis.queue.core;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * TODO 阻塞or轮询
 * @author hanchao
 * @date 2017/10/31 16:45
 */
public class RedisQueueListenerContainer implements InitializingBean{
    private JedisPool jedisPool;
    public RedisQueueListenerContainer(JedisPool jedisPool){
        this.jedisPool = jedisPool;
    }

    @Autowired(required = false)
    private List<RedisQueueListener> listeners;


    @Override
    public void afterPropertiesSet() throws Exception {
        if(CollectionUtils.isNotEmpty(listeners)){
            listeners = Lists.newCopyOnWriteArrayList(listeners);
        }
    }

    public void addListener(RedisQueueListener redisQueueListener){
        //if(coll)
    }
}
