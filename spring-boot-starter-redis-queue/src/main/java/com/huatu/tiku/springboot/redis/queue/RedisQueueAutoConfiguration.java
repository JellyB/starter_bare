package com.huatu.tiku.springboot.redis.queue;

import com.huatu.tiku.springboot.redis.queue.support.QueueRedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 减少对auto redis的侵入，所以不使用spring data提供的tempalte
 * @author hanchao
 * @date 2017/8/23 10:54
 */
@Configuration
@EnableConfigurationProperties(QueueRedisProperties.class)
public class RedisQueueAutoConfiguration {

    private final QueueRedisProperties properties;

    public RedisQueueAutoConfiguration(QueueRedisProperties properties){
        this.properties = properties;
    }


    @Bean
    public JedisPool jediSentinelPool(){
        JedisPoolConfig poolConfig = this.properties.getPool() != null ? jedisPoolConfig() : new JedisPoolConfig();
        JedisPool jedisPool = new JedisPool(poolConfig,this.properties.getHost(),this.properties.getPort(),this.properties.getTimeout(),this.properties.getPassword());
        return jedisPool;
    }

    private JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        QueueRedisProperties.Pool props = this.properties.getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis(props.getMaxWait());
        config.setTestOnBorrow(props.isTestOnBorrow());
        return config;
    }
}
