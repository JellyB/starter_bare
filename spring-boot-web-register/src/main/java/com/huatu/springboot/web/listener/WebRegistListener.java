package com.huatu.springboot.web.listener;

import com.huatu.springboot.web.register.WebRegister;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;

/**
 * @author hanchao
 * @date 2017/9/18 14:44
 */
public class WebRegistListener implements ApplicationListener {
    private WebRegister webRegister;

    public WebRegistListener(WebRegister webRegister) {
        this.webRegister = webRegister;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(webRegister == null){
            return;
        }
        //org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent 容器启动完成
        if(event instanceof ApplicationReadyEvent){
            webRegister.regist();
        }else if(event instanceof ContextStoppedEvent){
            webRegister.unregister();
        }
    }


}
