package com.huatu.common.spring.web.resolver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hanchao
 * @date 2017/4/9 18:41
 */
public interface ErrorResultHandler {
    ModelAndView handle( HttpServletRequest request, HandlerMethod handlerMethod, Object errorResult, HttpStatus status);
}
