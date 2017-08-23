package com.huatu.springboot.dubbo.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author xiaofei.wxf(teaey)
 * @since 0.0.0
 */
@Slf4j
public class DubboHolderListener implements ApplicationListener {
    private static Thread holdThread;
    private static Boolean running = Boolean.FALSE;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            AnnotationBean annotationBean = ((ContextRefreshedEvent) event).getApplicationContext().getBean(AnnotationBean.class);
            if(annotationBean.isEnabled()){
                running = Boolean.TRUE;
            }
            if (running && holdThread == null) {
                holdThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("dubbo started already...");;
                        while (running && !Thread.currentThread().isInterrupted()) {
                            try {
                                log.debug("dubbo running...");
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                            }
                        }
                    }
                }, "Dubbo-Holder");
                holdThread.setDaemon(false);
                holdThread.start();
            }
        }
        if (event instanceof ContextClosedEvent) {
            running = Boolean.FALSE;
            if (null != holdThread) {
                holdThread.interrupt();
                holdThread = null;
            }
        }
    }
}
