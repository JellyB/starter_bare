package com.huatu.tiku.springboot.redis.queue.core;

import com.alibaba.fastjson.JSON;

/**
 * @author hanchao
 * @date 2017/10/31 16:58
 */
public abstract class AbstractRedisQueueListener<T> implements RedisQueueListener {
    @Override
    public void consume(String message) {
        T t = JSON.parseObject(message,contentType());
        consumeContent(t);
    }

    public abstract void consumeContent(T t);

    public abstract Class<T> contentType();
}
