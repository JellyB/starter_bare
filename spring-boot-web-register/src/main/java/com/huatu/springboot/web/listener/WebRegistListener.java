package com.huatu.springboot.web.listener;

import com.huatu.springboot.web.register.WebRegister;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @author hanchao
 * @date 2017/9/18 14:44
 */
@Slf4j
public class WebRegistListener implements ApplicationListener {
    private WebRegister webRegister;

    public WebRegistListener(WebRegister webRegister) {
        this.webRegister = webRegister;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(webRegister == null){
            log.warn("null register find...");
            return;
        }
        //org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent 容器启动完成
        log.info("catch spring event,{}",event.getClass());
        if(event instanceof ApplicationReadyEvent){
            log.info("app start,regist...");
            webRegister.regist();
        }else if(event instanceof ContextClosedEvent){
            log.info("app stop,stop...");
            webRegister.unregister();
        }
    }


}
