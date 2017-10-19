package com.huatu.springboot.web.tools.advice;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.AbstractJsonpResponseBodyAdvice;

/**
 * @author hanchao
 * @date 2017/10/19 10:42
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(value = "htonline.jsonp.enabled", havingValue = "true", matchIfMissing = true)
@ControllerAdvice
public class JsonpSupportAdvice extends AbstractJsonpResponseBodyAdvice {
    public JsonpSupportAdvice(){
        super("callback");
    }
}
