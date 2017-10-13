package com.huatu.tiku.springboot.basic.reward.event;

import com.huatu.tiku.springboot.basic.reward.RewardAction;
import org.springframework.core.Ordered;

/**
 * @author hanchao
 * @date 2017/10/13 14:19
 */
public interface RewardActionEventHandler extends Ordered {
    /**
     * 是否可以处理
     * @return
     */
    boolean canHanle(RewardAction.ActionType actionType);

    /**
     * 处理
     */
    void handle(RewardActionEvent actionEvent);
}
