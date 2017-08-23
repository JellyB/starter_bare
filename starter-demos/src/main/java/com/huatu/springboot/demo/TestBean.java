package com.huatu.springboot.demo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author hanchao
 * @date 2017/8/22 20:40
 */
@Data
@ConfigurationProperties(prefix = "test")
public class TestBean {
    private String a;
    private String b;
    private String c;
}
