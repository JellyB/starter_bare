package com.huatu.tiku.springboot.basic.reward.event;

import com.huatu.common.utils.date.DateUtil;
import com.huatu.tiku.common.bean.RewardMessage;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import com.huatu.tiku.springboot.basic.reward.RewardActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Calendar;
import java.util.Date;

/**
 * ONCE类型的任务直接在业务中拦截，此处不做处理
 * @author hanchao
 * @date 2017/10/13 16:07
 */
@Slf4j
public abstract class AbstractRewardActionHandler implements RewardActionEventHandler {
    //直接强制使用该key，屏蔽各应用的序列化前缀,防止跨应用任务的情况
    public static final String REWARD_BIZ_KEY = "reward.action$%s$%s";
    @Autowired
    private RewardActionService rewardActionService;

    private StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();


    @Override
    public void handle(RewardActionEvent actionEvent) {
        if(actionEvent.getAction() == null){
            log.error("action cant be null,{}...",actionEvent);
        }
        if(filter(actionEvent)){
            RewardMessage rewardMessage = RewardMessage.builder()
                    .action(actionEvent.getAction().name())
                    .bizId(actionEvent.getBizId())
                    .gold(actionEvent.getGold())
                    .experience(actionEvent.getExperience())
                    .uid(actionEvent.getUid())
                    .uname(actionEvent.getUname())
                    .timestamp(actionEvent.getTimestamp())
                    .build();

        }
    }

    protected String getActionKey(RewardAction.ActionType action){
        return action.name();
    }

    public boolean filter(RewardActionEvent actionEvent){
        RewardAction rewardAction = rewardActionService.get(actionEvent.getAction().name());
        if(rewardAction.getStrategy() != RewardAction.Strategy.NONE){
            String cacheKey = String.format(REWARD_BIZ_KEY,getActionKey(rewardAction.getAction()),actionEvent.getUid());
            byte [] key = stringRedisSerializer.serialize(cacheKey);
            long mills = getExpireTime(rewardAction);
            //0不做限制
            if(mills != 0){
                Long reqTime = getRedisTemplate().<Long>execute((RedisCallback) (conn)-> {
                    Long incr = conn.incr(key);
                    conn.expire(key,mills);
                    return incr;
                });
                return analyzeTimes(rewardAction,reqTime);
            }
        }
        return true;
    }

    public long getExpireTime(RewardAction rewardAction){
        switch (rewardAction.getStrategy()){
            case DAILY:
                return DateUtil.getEndOfCurrentDay();
            case WEEKELY:
                return DateUtil.getEnd(Calendar.DAY_OF_WEEK,new Date()).getTimeInMillis();
            case MONTHLY:
                return DateUtil.getEnd(Calendar.DAY_OF_MONTH,new Date()).getTimeInMillis();
            case YEARLY:
                return DateUtil.getEnd(Calendar.YEAR,new Date()).getTimeInMillis();
            default:
                break;
        }
        return 0L;
    }

    /**
     *
     * @param rewardAction
     * @param time 次数
     * @return
     */
    public boolean analyzeTimes(RewardAction rewardAction,long time){
        if(time > rewardAction.getTimesLimit()){
            return false;
        }
        return true;
    }

    public void dealMessage(RewardMessage rewardMessage){

    }


    @Override
    public int getOrder() {
        return 0;
    }

    //所有应用要提供统一的redis数据源保证一致
    protected abstract RedisTemplate getRedisTemplate();

}
