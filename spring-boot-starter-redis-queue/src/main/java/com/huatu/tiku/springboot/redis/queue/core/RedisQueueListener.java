package com.huatu.tiku.springboot.redis.queue.core;

/**
 * @author hanchao
 * @date 2017/10/31 16:46
 */
public interface RedisQueueListener {
    default int getConsumerCount(){
        return 1;
    }

    void consume(String message);

    String queue();
}
