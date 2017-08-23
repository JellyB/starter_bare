package com.huatu.tiku.springboot.users.service;

import com.huatu.tiku.springboot.users.support.SessionRedisTemplate;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.user.common.UserErrors;
import com.huatu.ztk.user.common.UserRedisSessionKeys;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 用户session工具包
 * Created by shaojieyue
 * Created time 2016-04-23 21:57
 */
@Service
public class UserSessionService {

    @Resource(name = "sessionRedisTemplate")
    private RedisTemplate<String,String> sessionRedisTemplate;

    @Autowired
    private SessionRedisTemplate sessionRedisTemplate;

    /**
     * 医护人员token
     */
    public static final String USER_TOKEN_KEY = "utoken_%s";

    //新设备登录消息提示
    public static final String TIP_MESSAGE="您的账号于%s在其它客户端登录，请重新登录。\n如非本人操作，则密码可能已泄露，请及时修改密码。";

    /**
     * 查询用户手机号
     * @param token
     * @return
     */
    public final String getMobileNo(String token){
        return getSessionValue(token,UserRedisSessionKeys.mobile);
    }

    /**
     * 查询session key 对应的value
     * @param token token
     * @param key session 属性
     * @return
     */
    private final String getSessionValue(String token,String key){
        String value = null;
        if (isBlank(token) || isBlank(key)) {
            return value;
        }
        value = sessionRedisTemplate.hget(token, key);
        return value;
    }


    /**
     * 查询用户名称
     * @param token
     * @return
     */
    public final String getNick(String token){
        return getSessionValue(token,UserRedisSessionKeys.nick);
    }

    /**
     * 查询用户username
     * @param token
     * @return
     */
    public final String getUname(String token){
        return getSessionValue(token,UserRedisSessionKeys.uname);
    }

    /**
     * 获取用户id
     * @param token
     * @return
     */
    public long getUid(String token) {
        final String uidStr = getSessionValue(token, UserRedisSessionKeys.id);
        long userId = -1;
        if(isNotBlank(uidStr)){//id存在
            userId = Long.valueOf(uidStr);
        }
        return userId;
    }

    /**
     * 通过用户id查询其token
     * @param userId
     * @return
     */
    public String getTokenById(long userId) {
        String key = String.format(USER_TOKEN_KEY, userId);
        final String token = sessionRedisTemplate.opsForValue().get(key);
        return token;
    }

    /**
     * 获取用户id
     * @param token
     * @return
     */
    public int getSubject(String token) {
        final String subjectStr = getSessionValue(token, UserRedisSessionKeys.subject);
        int subject = -1;
        if(isNotBlank(subjectStr)){//id存在
            subject = Integer.valueOf(subjectStr);
        }
        return subject;
    }

    /**
     * 获取当前用户所属的知识点类目
     * @param token
     * @return
     */
    public int getCatgory(String token){
        final String sessionValue = getSessionValue(token, UserRedisSessionKeys.catgory);
        int pointCatgory = -1;
        if(isNotBlank(sessionValue)){//id存在
            pointCatgory = Integer.valueOf(sessionValue);
        }
        return pointCatgory;
    }

    /**
     * 获取用户抽题数量配置
     * @param token
     * @return
     */
    public int getQcount(String token){
        final String sessionValue = getSessionValue(token, UserRedisSessionKeys.qcount);
        int qcount = 10;
        if(isNotBlank(sessionValue)){//id存在
            qcount = Integer.valueOf(sessionValue);
        }

        return qcount;
    }

    /**
     * 查询区域id
     * @param token token
     * @return
     */
    public int getArea(String token ){
        final String areaStr = getSessionValue(token, UserRedisSessionKeys.area);
        int area = -1;
        if(isNotBlank(areaStr)){//id存在
            area = Integer.valueOf(areaStr);
        }
        return area;
    }

    /**
     * 查询email
     * @param token token
     * @return
     */
    public String getEmail(String token ){
        final String emailStr = getSessionValue(token, UserRedisSessionKeys.email);

        if(isBlank(emailStr)){//id存在
            return null;
        }
        return emailStr;
    }

    /**
     * 获取过期时间
     * @param token
     * @return
     */
    public long getExpireTime(String token) {
        final String expireTimeStr = getSessionValue(token, UserRedisSessionKeys.expireTime);
        long loginTime = -1;
        if(isNotBlank(expireTimeStr)){//id存在
            loginTime = Long.valueOf(expireTimeStr);
        }
        return loginTime;
    }

    /**
     * 判断用户登录状态是否过期
     * @param token 用户token
     * @return true：过期 false：没有过期
     */
    private final boolean isExpire(String token){
        if (isBlank(token)) {
            return true;
        }
        //过期时间小于当前时间，说明已经过期
        return getExpireTime(token)<System.currentTimeMillis();
    }

    /**
     * 断定session有效
     * @param token 用户的token
     * @throws BizException 当用户session无效时抛出异常
     */
    public void assertSession(String token) throws BizException {
        final String oldToken = getSessionValue(token, UserRedisSessionKeys.oldToken);
        if ("1".equals(oldToken)) {//有新设备登录，当前设备已经被踢掉
            final String newDiveceLoginTime = getSessionValue(token, UserRedisSessionKeys.newDiveceLoginTime);
            //7月1日15:20
            final String time = DateFormatUtils.format(Long.parseLong(newDiveceLoginTime), "MM月dd日HH:mm");
            final String tipMessage = String.format(TIP_MESSAGE, time);
            //设置过期一个月,让自动过期
            sessionRedisTemplate.expire(token,30, TimeUnit.DAYS);
            throw new BizException(UserErrors.LOGIN_ON_OTHER_DEVICE,tipMessage);
        }

        if (isExpire(token)) {//session过期
            //拋出普通的session過期異常
            throw new BizException(UserErrors.SESSION_EXPIRE);
        }
    }

}
