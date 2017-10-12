package com.huatu.tiku.springboot.basic.reward;

/**
 * @author hanchao
 * @date 2017/10/12 15:48
 */
public class RewardAction {


    public enum ActionType {
        REGSITER,//注册
        ATTENDANCE,//签到
        MATCH_ENROLL,//模考报名
        MATCH_ENTER,//参加
        MATCH_PRIZE,//获得前五名
        ARENA_WIN,//竞技场每日首胜
        TRAIN_DAILY,//每日特训
        TRAIN_SPECIAL,//专项练习
        TRAIN_INTELLIGENCE,//智能练习
        TRAIN_MISTAKE,//错题重练
        ANSWER_CORRECTION,//试题纠错
        WATCH_FREE,//观看免费课
        WATCH_PAY,//观看付费课
        EVALUATE_AFTER,//观看完成后评价
        EVALUATE,//评价课程
        SHARE,//分享
        CHARGE,//充值

    }
}
