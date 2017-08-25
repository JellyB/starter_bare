package com.huatu.tiku.springboot.users.bean;


import com.huatu.common.ErrorResult;

/**
 *
 * 用户错误码
 * Created by shaojieyue
 * Created time 2016-04-24 13:51
 */
public class UserErrors {
    /**
     * 用户名密码错误
     */
    public static final ErrorResult LOGIN_FAIL = ErrorResult.create(1110001,"用户名或密码错误");

    /**
     * 用户会话过期
     */
    public static final ErrorResult SESSION_EXPIRE = ErrorResult.create(1110002,"用户会话过期");

    /**
     * 用户不存在
     */
    public static final ErrorResult USER_NOT_EXIST = ErrorResult.create(1110003,"用户不存在");

    /**
     *其他设备登录
     */
    public static final ErrorResult LOGIN_ON_OTHER_DEVICE = ErrorResult.create(1110004,"用户在其他设备登录");

    /**
     * 账号未注册
     */
    public static final ErrorResult USER_NOT_REGISTER = ErrorResult.create(1110005,"账号未注册");

    /**
     * 非法的手机号
     */
    public static final ErrorResult ILLEGAL_MOBILE = ErrorResult.create(1112101,"非法的手机号");

    /**
     * 验证码错误
     */
    public static final ErrorResult CAPTCHA_ERROR = ErrorResult.create(1112102,"验证码错误");

    /**
     * 用户已经存在
     */
    public static final ErrorResult USER_EXISTS = ErrorResult.create(1112103,"用户已经存在");

    /**
     * 旧密码输入错误
     */
    public static final ErrorResult OLD_PASSWORD_ERROR = ErrorResult.create(1112104,"旧密码输入错误");

    /**
     * 昵称长度错误
     */
    public static final ErrorResult NICKNAME_PATTERN_ERROR = ErrorResult.create(1112105,"昵称格式错误");

    /**
     * 昵称包含敏感词
     */
    public static final ErrorResult NICKNAME_SENSITIVE = ErrorResult.create(1112106, "昵称包含敏感词");

    /**
     * 反馈内容过长
     */
    public static final ErrorResult FEEDBACK_CONTENT_TOO_LONG = ErrorResult.create(1114001,"反馈内容过长");

    /**
     * 反馈内容为空
     */
    public static final ErrorResult FEEDBACK_CONTENT_IS_NULL = ErrorResult.create(1114001,"反馈内容为空");

    /**
     * 密码长度错误
     */
    public static final ErrorResult PASSWORD_LENGTH_ERROR = ErrorResult.create(1112107, "密码长度错误");

    /**
     * 验证码发送过于频繁
     */
    public static final ErrorResult CAPTCHA_SEND_TOO_FREQUENT = ErrorResult.create(1112109, "验证码发送过于频繁");

    /**
     * 验证码过期
     */
    public static final ErrorResult CAPTCHA_EXPIRE = ErrorResult.create(1112108, "验证码过期");

    /**
     * 头像格式不支持
     */
    public static final ErrorResult IMG_TYPE_NOT_SUPPORT = ErrorResult.create(1115100, "头像格式不支持");

    /**
     * 上传头像尺寸过大
     */
    public static final ErrorResult AVATAR_FILE_TOO_LARGE = ErrorResult.create(1115101, "上传头像尺寸过大");

    /**
     * 上传失败
     */
    public static final ErrorResult UPLOAD_AVATAR_FAIL = ErrorResult.create(1115102, "上传失败");
}
