package com.huatu.tiku.springboot.users.support;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

/**
 * @author hanchao
 * @date 2017/8/23 17:15
 */
@Slf4j
public class SessionRedisTemplate {
    private Pool<Jedis> pool;
    public SessionRedisTemplate(Pool<Jedis> pool){
        this.pool = pool;
    }


    public <T> T execute(JedisCallback<T> callback){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return callback.doInJedis(jedis);
        } catch(Exception e){
            log.error("execute jedis command error... ",e);
            throw new JedisException(e);
        }finally {
            jedis.close();
        }
    }


    public String hget(String key,String field){
        return execute(jedis -> jedis.hget(key,field));
    }

    public void hset(String key,String field,String value){
        execute(jedis -> jedis.hset(key,field,value));
    }

    public void set(String key,String value){
        execute(jedis -> jedis.set(key,value));
    }
}
