package com.huatu.springboot.report.support;

import com.huatu.tiku.common.bean.report.ReportMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hanchao
 * @date 2018/1/11 15:27
 */
public class RabbitReporter implements MessageReporter {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;

    @Override
    public void report(ReportMessage message) {
        //直接使用json发送，避免不同序列化导致的问题
        rabbitTemplate.send(RabbitConsts.QUEUE_REPORT,jackson2JsonMessageConverter.toMessage(message,new MessageProperties()));
    }

}
