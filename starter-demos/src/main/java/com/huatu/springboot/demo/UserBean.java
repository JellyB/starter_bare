package com.huatu.springboot.demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author hanchao
 * @date 2017/8/22 19:41
 */
@Data
@ConfigurationProperties(prefix = "user")
public class UserBean {
    private String name;
    private String nick;
}
