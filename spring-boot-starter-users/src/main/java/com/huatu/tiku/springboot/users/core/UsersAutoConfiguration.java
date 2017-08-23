package com.huatu.tiku.springboot.users.core;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.huatu.tiku.springboot.users.support.EnableUserSessions;
import com.huatu.tiku.springboot.users.support.SessionRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import redis.clients.jedis.JedisSentinelPool;

/**
 * 使用需要引入公共namespace,同时添加importusersessions注解
 * @author hanchao
 * @date 2017/8/23 10:22
 */
@Configuration
@ConditionalOnBean(annotation = EnableUserSessions.class)
@EnableApolloConfig("tiku.user-sessions")
@Import(SessionRedisConfiguration.class)
public class UsersAutoConfiguration {
    /**
     * 简单实现的template,仅用于操作user session，区分开spring redis，
     * 减少对RedisAutoConfiguration的侵入，保证使用该模块的项目可以正常接入spring auto config
     * @param jediSentinelPool
     * @return
     */
    @Bean
    public SessionRedisTemplate sessionRedisTemplate(@Autowired @Qualifier("sessionJediSentinelPool") JedisSentinelPool jediSentinelPool){
        return new SessionRedisTemplate(jediSentinelPool);
    }
}
