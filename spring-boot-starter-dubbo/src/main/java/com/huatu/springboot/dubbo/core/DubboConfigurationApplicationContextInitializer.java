package com.huatu.springboot.dubbo.core;

import com.huatu.springboot.dubbo.support.AnnotationBean;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class DubboConfigurationApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        AnnotationBean scanner = BeanUtils.instantiate(AnnotationBean.class);
        scanner.setApplicationContext(applicationContext);
        applicationContext.addBeanFactoryPostProcessor(scanner);
        applicationContext.getBeanFactory().addBeanPostProcessor(scanner);
        applicationContext.getBeanFactory().registerSingleton("annotationBean", scanner);
    }


}
