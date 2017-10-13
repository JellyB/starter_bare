package com.huatu.tiku.springboot.basic.reward.event;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/10/13 13:59
 */
@Slf4j
public class RewardActionEventListener implements ApplicationContextAware,InitializingBean,ApplicationListener<RewardActionEvent> {
    private ApplicationContext applicationContext;
    private List<RewardActionEventHandler> rewardActionEventHandlers;


    /**
     * 事件处理
     * @param event
     */
    @Override
    public void onApplicationEvent(RewardActionEvent event) {
        for (RewardActionEventHandler rewardActionEventHandler : rewardActionEventHandlers) {
            if(rewardActionEventHandler.canHanle(event.getAction())){
                rewardActionEventHandler.handle(event);
                return;
            }
        }
        log.error("no handler found for event {}",event.getAction());
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, RewardActionEventHandler> beans = applicationContext.getBeansOfType(RewardActionEventHandler.class);
        rewardActionEventHandlers = Lists.newArrayList(beans.values());
        Collections.sort(rewardActionEventHandlers,Comparator.comparingInt(RewardActionEventHandler::getOrder));
    }

}
