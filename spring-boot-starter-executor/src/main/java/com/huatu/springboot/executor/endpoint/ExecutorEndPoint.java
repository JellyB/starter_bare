package com.huatu.springboot.executor.endpoint;

import com.huatu.springboot.executor.support.NamedThreadPoolTaskExecutor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author hanchao
 * @date 2017/9/30 19:54
 */
@ConfigurationProperties(prefix = "endpoints.executor")
public class ExecutorEndPoint extends AbstractEndpoint<Map> implements ApplicationContextAware,InitializingBean {
    private ApplicationContext applicationContext;
    private Map<String,Executor> executors;

    public ExecutorEndPoint() {
        super("executor");
    }

    @Override
    public Map invoke() {
        Map<String,Object> result = new HashMap<>();
        result.put("size",executors == null ? 0 : executors.size());
        if(executors != null){
            executors.forEach((name,executor) -> {
                Map<String,String> info = new HashMap<>();
                if(executor instanceof NamedThreadPoolTaskExecutor){
                    info.put("name",((NamedThreadPoolTaskExecutor) executor).getName());
                }else{
                    info.put("name",name);
                } //TODO 由spring bean命名或者传入
                info.put("status",executor.toString());// TODO 细化拆分
                result.put(name,info);
            });
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        executors = applicationContext.getBeansOfType(Executor.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
