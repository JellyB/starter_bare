package com.huatu.springboot.druid;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * @author hanchao
 * @date 2017/1/14 22:49
 */
@ConfigurationProperties(prefix = "spring.datasource.druid")
@Data
public class DruidProperties {
    private Integer initialSize;
    private Integer minIdle;
    private Integer maxActive;
    private Integer maxWait;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer minEvictableIdleTimeMillis;

    private boolean testWhileIdle = true;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;
    private boolean poolPreparedStatements = true;
    private Integer psCacheSize;
    private Integer maxPoolPreparedStatementPerConnectionSize;
    private String filters;
    private String validationQuery;
    private Properties connectionProperties;
    private MonitorProperties monitor = new MonitorProperties();


    @Data
    public static class MonitorProperties {
        private String druidStatView;
        private String druidWebStatFilter;
        private String allow;
        private String deny;
        private String loginUsername;
        private String loginPassword;
        private String exclusions;
        private String resetEnable;
    }
}
