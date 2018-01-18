package com.huatu.springboot.report.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huatu.tiku.common.bean.report.ReportMessage;
import com.huatu.tiku.common.consts.RabbitConsts;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hanchao
 * @date 2018/1/11 15:27
 */
public class RabbitReporter implements MessageReporter {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void report(ReportMessage message) {
        //直接使用json发送，避免不同序列化导致的问题
        rabbitTemplate.convertAndSend(RabbitConsts.QUEUE_REPORT, JSON.toJSONString(message, SerializerFeature.WriteClassName));
    }

}
