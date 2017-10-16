package com.huatu.tiku.springboot.basic.reward;

import com.huatu.tiku.common.bean.reward.RewardResult;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEvent;
import com.huatu.tiku.springboot.basic.reward.event.RewardActionEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 需要获取返回结果的处理类
 * @author hanchao
 * @date 2017/10/16 13:39
 */
@Slf4j
public class RewardActionExecutor implements InitializingBean {
    @Autowired
    private List<RewardActionEventHandler> rewardActionEventHandlers;


    public RewardResult submit(RewardActionEvent event){
        for (RewardActionEventHandler rewardActionEventHandler : rewardActionEventHandlers) {
            if(rewardActionEventHandler.canHandle(event.getAction())){
                return rewardActionEventHandler.handle(event);
            }
        }
        log.error("no handler found for event {}",event.getAction());
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Collections.sort(rewardActionEventHandlers, Comparator.comparingInt(RewardActionEventHandler::getOrder));
    }
}
