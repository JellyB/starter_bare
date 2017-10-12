package com.huatu.tiku.springboot.users.bean;

import lombok.Builder;
import lombok.Data;

/**
 * @author hanchao
 * @date 2017/8/24 20:01
 */
@Data
@Builder
public class UserSession {
    private int ssoId;
    private int id;
    private String token;
    private String mobile;
    private String email;
    private String nick;
    private String uname;
    private int subject;
    private int area;
    private long expireTime;
    private String oldToken;
    private String newDiveceLoginTime;
    private int category;
    private int qcount;
}
