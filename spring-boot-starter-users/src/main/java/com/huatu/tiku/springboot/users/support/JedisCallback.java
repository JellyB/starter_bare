package com.huatu.tiku.springboot.users.support;

import redis.clients.jedis.Jedis;

/**
 * @author hanchao
 * @date 2017/8/23 17:26
 */
public interface JedisCallback<T> {
    T doInJedis(Jedis jedis);
}
