package com.huatu.tiku.springboot.redis.queue.core;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.huatu.tiku.springboot.redis.queue.support.QueueJedisTemplate;
import redis.clients.jedis.JedisPool;

/**
 * @author hanchao
 * @date 2017/10/31 16:46
 */
public class RedisQueueTempalte {
    private JedisPool jedisPool;
    private QueueJedisTemplate jedisTemplate;

    public RedisQueueTempalte(JedisPool jedisPool){
        this.jedisPool = jedisPool;
        this.jedisTemplate = new QueueJedisTemplate(jedisPool);
    }

    public void convertAndSend(String queue,Object... objects){
        String[] messages = Lists.newArrayList(objects).stream().map(o -> JSON.toJSONString(o)).toArray(String[]::new);
        this.jedisTemplate.lpush(queue,messages);
    }
}