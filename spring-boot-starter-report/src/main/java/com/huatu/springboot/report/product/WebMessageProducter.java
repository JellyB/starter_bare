package com.huatu.springboot.report.product;

import com.huatu.common.utils.date.TimestampUtil;
import com.huatu.springboot.report.annotation.WebReport;
import com.huatu.springboot.report.support.MessageReportExecutor;
import com.huatu.springboot.report.support.RabbitReporter;
import com.huatu.springboot.report.util.HostCacheUtil;
import com.huatu.tiku.common.bean.report.WebReportMessage;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.service.UserSessionService;
import com.huatu.tiku.springboot.users.support.Token;
import com.huatu.tiku.springboot.users.support.UserSessionHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author hanchao
 * @date 2018/1/12 9:47
 */
@Slf4j
public class WebMessageProducter implements HandlerInterceptor {
    @Autowired
    private UserSessionService userSessionService;
    @Autowired
    private RabbitReporter rabbitReporter;
    @Autowired
    private MessageReportExecutor messageReportExecutor;
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        WebReport annotation = getAnnotation(handler);
        if(annotation != null){
            String token = request.getHeader(Token.DEFAULT_NAME);
            UserSession userSession = userSessionService.getUserSession(token);
            UserSessionHolder.set(userSession);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // do nothing

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserSessionHolder.clear();
        WebReport annotation = getAnnotation(handler);
        if(annotation != null){
            ServletServerHttpRequest nativeRequest = new ServletServerHttpRequest(request);
            ServletServerHttpResponse nativeResponse = new ServletServerHttpResponse(response);
            String body = IOUtils.toString(nativeRequest.getBody());
            String stackstrace = "";
            Object exception = request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
            if(exception != null && exception instanceof Exception){
                stackstrace = ExceptionUtils.getStackTrace((Exception)exception);
            }
            WebReportMessage webReportMessage = WebReportMessage.builder()
                    .name(annotation.value())
                    .url(request.getRequestURI())
                    .urlParameters(request.getQueryString())
                    .method(request.getMethod())
                    .body(body)
                    .status(response.getStatus())
                    .requestHeaders(nativeRequest.getHeaders())
                    .responseHeaders(nativeResponse.getHeaders())
                    .userSession(UserSessionHolder.get())
                    .stacktrace(stackstrace)
                    .build();

            webReportMessage.setTimestamp(TimestampUtil.currentTimeStamp());
            webReportMessage.setApplication(applicationContext.getEnvironment().getProperty("spring.application.name",""));
            webReportMessage.setHost(HostCacheUtil.getHost());

            messageReportExecutor.submit(this.new ReportTask(webReportMessage,annotation.extraHandler()));

        }

    }


    protected class ReportTask implements Runnable {
        private WebReportMessage message;
        private Class<?> handlerClass;

        public ReportTask(WebReportMessage message, Class<?> handler) {
            this.message = message;
            this.handlerClass = handler;
        }

        @Override
        public void run() {
            if(handlerClass != null && ExtraDataHandler.class.isAssignableFrom(handlerClass)){
                if(applicationContext.getBean(handlerClass) == null ){
                    log.error("cant find extra data handler for class {}",handlerClass);
                }else{
                    ExtraDataHandler handler = (ExtraDataHandler)applicationContext.getBean(handlerClass);
                    Object extra = handler.extra(message);
                    message.setExtraData(extra);
                }
            }
            rabbitReporter.report(message);
        }
    }


    public WebReport getAnnotation(Object handler){
        if(handler.getClass().isAssignableFrom(HandlerMethod.class)){
            WebReport annotation = ((HandlerMethod)handler).getMethodAnnotation(WebReport.class);
            return annotation;
        }
        return null;
    }
}
