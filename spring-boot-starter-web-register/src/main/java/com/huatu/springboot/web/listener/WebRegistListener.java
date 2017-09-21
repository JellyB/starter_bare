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
        if(!(event instanceof ApplicationReadyEvent) && !(event instanceof ContextClosedEvent)){
            return;
        }
        if(webRegister == null){
            log.error("null register find...");
            return;
        }
        //org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent 容器启动完成
        log.info("catch spring event,{}",event.getClass());
        if(event instanceof ApplicationReadyEvent){
            log.info("app start,regist...");
            webRegister.regist();
        }else if(event instanceof ContextClosedEvent){
            //目前遇到的问题是这里报错，会阻塞关闭进程，只能通过强杀
            log.info("app stop,stop...");
            try {
                if(!webRegister.unregister()){
                    log.error("unable to unregister,the {} retrun false ! ",webRegister.getClass());
                }
            } catch(Exception e){
                log.error("unregister cause an exception...",e);
            }
        }
    }


}
