package com.huatu.springboot.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @author hanchao
 * @date 2017/1/14 23:03
 */
@Configuration
@EnableConfigurationProperties(DruidProperties.class)
@ConditionalOnClass(DruidDataSource.class)
public class DruidAutoconfiguration {
    @Autowired
    private DruidProperties druidProperties;
    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Bean
    @ConditionalOnProperty(name = "spring.datasource.type", havingValue = "com.alibaba.druid.pool.DruidDataSource")
    public DataSource dataSource() throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(dataSourceProperties.getUrl());
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        if(druidProperties.getInitialSize()!=null){
            dataSource.setInitialSize(druidProperties.getInitialSize());
        }
        if(druidProperties.getMinIdle()!=null){
            dataSource.setMinIdle(druidProperties.getMinIdle());
        }
        if(druidProperties.getMaxActive()!=null){
            dataSource.setMaxActive(druidProperties.getMaxActive());
        }
        if(druidProperties.getMaxWait()!=null){
            dataSource.setMaxWait(druidProperties.getMaxWait());
        }
        if(druidProperties.getTimeBetweenEvictionRunsMillis()!=null){
            dataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        }
        if(druidProperties.getMinEvictableIdleTimeMillis()!=null){
            dataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        }
        if(druidProperties.getValidationQuery() != null){
            dataSource.setValidationQuery(druidProperties.getValidationQuery());
        }
        dataSource.setPoolPreparedStatements(druidProperties.isPoolPreparedStatements());
        if(druidProperties.isPoolPreparedStatements() && druidProperties.getMaxPoolPreparedStatementPerConnectionSize()!=null){
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        dataSource.setTestWhileIdle(druidProperties.isTestWhileIdle());
        dataSource.setTestOnBorrow(druidProperties.isTestOnBorrow());
        dataSource.setTestOnReturn(druidProperties.isTestOnReturn());
        if(druidProperties.getMaxPoolPreparedStatementPerConnectionSize()!=null){
            dataSource.setMaxPoolPreparedStatementPerConnectionSize(druidProperties.getMaxPoolPreparedStatementPerConnectionSize());
        }
        if(druidProperties.getFilters()!=null){
            dataSource.setFilters(druidProperties.getFilters());
        }
        if(druidProperties.getConnectionProperties()!=null){
            dataSource.setConnectProperties(druidProperties.getConnectionProperties());
        }
        return dataSource;
    }

    @Configuration
    @ConditionalOnProperty(prefix = "spring.druid.monitor",havingValue = "enabled",value = "enabled",matchIfMissing = true)
    protected class DruidMonitorAutoConfiguration {
        @Bean
        public ServletRegistrationBean druidStatViewServlet(){
            //org.springframework.boot.context.embedded.ServletRegistrationBean提供类的进行注册.
            ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new StatViewServlet(),druidProperties.getMonitor().getDruidStatView());

            //添加初始化参数：initParams

            //白名单：
            servletRegistrationBean.addInitParameter("allow",druidProperties.getMonitor().getAllow());
            //IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to view this page.
            servletRegistrationBean.addInitParameter("deny",druidProperties.getMonitor().getDeny());
            //登录查看信息的账号密码.
            servletRegistrationBean.addInitParameter("loginUsername",druidProperties.getMonitor().getLoginUsername());
            servletRegistrationBean.addInitParameter("loginPassword",druidProperties.getMonitor().getLoginPassword());
            //是否能够重置数据.
            servletRegistrationBean.addInitParameter("resetEnable",druidProperties.getMonitor().getResetEnable());
            return servletRegistrationBean;
        }

        @Bean
        public FilterRegistrationBean druidStatFilter(){

            FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());

            //添加过滤规则.
            filterRegistrationBean.addUrlPatterns(druidProperties.getMonitor().getDruidWebStatFilter());

            //添加不需要忽略的格式信息.
            filterRegistrationBean.addInitParameter("exclusions",druidProperties.getMonitor().getExclusions());
            return filterRegistrationBean;
        }


    }
}

/**
 * 用法示例:
    spring:
        druid:
            initialSize: 10
            minIdle: 10
            maxActive: 60
            maxWait: 60000
            timeBetweenEvictionRunsMillis: 60000
            minEvictableIdleTimeMillis : 300000
            poolPreparedStatements: true
            maxPoolPreparedStatementPerConnectionSize: 20
            validationQuery: "select 'x'"
            filters: stat,slf4j
            monitor:
                enabled: enabled # 配置此属性Monitor才生效
                druid-stat-view: /druid/*
                druid-web-stat-filter: /*
                allow: 219.230.50.107,127.0.0.1
                deny: 219.230.50.108
                login-username: admin
                login-password: 123456
                exclusions: '*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*'
                reset-enable: false
*/