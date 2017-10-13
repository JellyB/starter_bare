package com.huatu.tiku.springboot.basic.reward.event;

import com.huatu.tiku.springboot.basic.reward.RewardAction;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * 完成任务事件
 * @author hanchao
 * @date 2017/10/13 13:26
 */
@Data
public class RewardActionEvent extends ApplicationEvent {
    private RewardAction.ActionType action;//动作名称
    private String bizId;
    private int gold;//赠送金币数量,0为不固定
    private int experience;//经验值,0为不固定
    private int uid;
    private String uname;

    public RewardActionEvent(Object source) {
        super(source);
    }

    public RewardAction.ActionType getAction() {
        return action;
    }

    public RewardActionEvent setAction(RewardAction.ActionType action) {
        this.action = action;
        return this;
    }

    public String getBizId() {
        return bizId;
    }

    public RewardActionEvent setBizId(String bizId) {
        this.bizId = bizId;
        return this;
    }

    public int getGold() {
        return gold;
    }

    public RewardActionEvent setGold(int gold) {
        this.gold = gold;
        return this;
    }

    public int getExperience() {
        return experience;
    }

    public RewardActionEvent setExperience(int experience) {
        this.experience = experience;
        return this;
    }

    public int getUid() {
        return uid;
    }

    public RewardActionEvent setUid(int uid) {
        this.uid = uid;
        return this;
    }

    public String getUname() {
        return uname;
    }

    public RewardActionEvent setUname(String uname) {
        this.uname = uname;
        return this;
    }

}
