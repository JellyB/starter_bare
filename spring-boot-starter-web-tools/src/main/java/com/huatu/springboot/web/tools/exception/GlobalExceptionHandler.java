package com.huatu.springboot.web.tools.exception;

import com.huatu.common.CommonErrors;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.ArgsValidException;
import com.huatu.common.exception.BizException;
import com.huatu.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.List;

/**
 * 好处是这里不捕捉所有异常，子项目依然可以在throwable上层定义自己的处理逻辑
 * @author hanchao
 * @date 2017/8/31 20:52
 */
@ConditionalOnProperty(value = "htonline.ex-handler.enabled",havingValue = "true",matchIfMissing = true)
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    //用于子项目扩展
    private List<ExceptionResolver> exceptionResolvers = new ArrayList();
    private ErrorResultHandler errorHandler = new SimpleErrorResultHandler();

    /**
     * 未授权异常
     * @param request
     * @param handlerMethod
     * @param exception
     */
    @ExceptionHandler(value = UnauthorizedException.class)
    public ModelAndView unauthorizedHandler(HttpServletRequest request, HandlerMethod handlerMethod, Exception exception) {
        if(log.isDebugEnabled()){
            log.debug("catch exception : ",exception);
        }
        ErrorResult errorResult = buildByResolvers(exception, ((UnauthorizedException)exception).getErrorResult());
        return errorHandler.handle(request,handlerMethod,errorResult, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 其他业务异常
     * @param request
     * @param handlerMethod
     * @param exception
     */
    @ExceptionHandler(value = BizException.class)
    public ModelAndView bizErrorHandler(HttpServletRequest request, HandlerMethod handlerMethod, Exception exception) {
        if(log.isDebugEnabled()){
            log.debug("catch exception : ",exception);
        }
        ErrorResult errorResult = buildByResolvers(exception, ((BizException)exception).getErrorResult());
        return errorHandler.handle(request,handlerMethod,errorResult, null);
    }

    /**
     * 参数错误
     * @param request
     * @param handlerMethod
     * @param exception
     */
    @ExceptionHandler(value = {ServletException.class,BindException.class,ArgsValidException.class, ValidationException.class, javax.validation.ValidationException.class, MethodArgumentNotValidException.class,HttpMessageConversionException.class})
    public ModelAndView invalidArgumentsHandler(HttpServletRequest request, HandlerMethod handlerMethod, Exception exception) {
        if(log.isDebugEnabled()){
            log.debug("catch exception : ",exception);
        }
        ErrorResult errorResult = buildByResolvers(exception, CommonErrors.INVALID_ARGUMENTS);
        return errorHandler.handle(request,handlerMethod,errorResult, HttpStatus.BAD_REQUEST);
    }

    /**
     * notsupport 默认只能处理到controler-method里面的
     * notfoundex 设置dispatcher servlet的thrownotfound以后才可以使用
     * @param request
     * @param exception
     * @return
     */
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class, NoHandlerFoundException.class})
    public ModelAndView notFoundHandler(HttpServletRequest request,Exception exception){
        if(log.isDebugEnabled()){
            log.debug("catch exception : ",exception);
        }
        ErrorResult errorResult = buildByResolvers(exception, CommonErrors.RESOURCE_NOT_FOUND);
        return errorHandler.handle(request,null,errorResult, HttpStatus.NOT_FOUND);
    }



    /**
     *
     * @param request
     * @param exception
     * @return
     */
    @ExceptionHandler(Throwable.class)
    public ModelAndView globalErrorHandler(HttpServletRequest request, HandlerMethod handlerMethod, Exception exception){
        //不认识的错误，打印完整的异常信息
        log.error("catch exception : ",exception);
        ErrorResult errorResult = buildByResolvers(exception,CommonErrors.SERVICE_INTERNAL_ERROR);
        return errorHandler.handle(request,handlerMethod,errorResult, HttpStatus.NOT_FOUND);
    }



    /**
     * 获取处理结果
     * @param ex
     * @return
     */
    private ErrorResult buildByResolvers(Exception ex,ErrorResult optional){
        ErrorResult errorResult = null;
        if(!CollectionUtils.isEmpty(exceptionResolvers)){
            for (ExceptionResolver resolver : exceptionResolvers) {
                if(resolver.canResolve(ex)){
                    errorResult = resolver.resolve(ex);
                    break;
                }
            }
        }
        if(errorResult == null){
            errorResult = optional;
        }
        return errorResult;
    }

    public List<ExceptionResolver> getExceptionResolvers() {
        return exceptionResolvers;
    }

    public ErrorResultHandler getErrorHandler() {
        return errorHandler;
    }

    public void setExceptionResolvers(List<ExceptionResolver> exceptionResolvers) {
        this.exceptionResolvers = exceptionResolvers;
    }

    public void setErrorHandler(ErrorResultHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
