package com.huatu.springboot.web.tools.exception;

import com.huatu.common.CommonResult;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.ArgsValidException;
import com.huatu.common.exception.BizException;
import com.huatu.common.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.ValidationException;
import java.util.List;

/**
 * spring boot默认异常处理是容器的errorpage,映射到error.path(basicerrorcontroller)，然后通过accept,status来读取不同的模板，没有模板读html文件,如果没有文件走默认的spelview /error,即Whitelabel
 * 所以无需自定义errorpage，直接复用errorcontroller提供的策略即可
 *
 * 不应该作为cloud service的异常捕捉
 * 好处是这里不捕捉所有异常，子项目依然可以在throwable上层定义自己的处理逻辑
 * @author hanchao
 * @date 2017/8/31 20:52
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler implements InitializingBean {

    //用于子项目扩展
    @Autowired(required = false)
    private List<ExceptionResolver> exceptionResolvers;
    @Autowired
    private ErrorResultHandler errorHandler;
    @Autowired
    private ExceptionCounter exceptionCounter;

    private boolean needResolve = false;

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
        return resolveAndHandle(request,handlerMethod,exception,((UnauthorizedException)exception).getErrorResult(),HttpStatus.UNAUTHORIZED);
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
        return resolveAndHandle(request,handlerMethod,exception,((BizException)exception).getErrorResult(),null);
    }

    /**
     * 参数错误
     * @param request
     * @param handlerMethod
     * @param exception
     */
    @ExceptionHandler(value = {ServletException.class,BindException.class,ArgsValidException.class,
            ValidationException.class, javax.validation.ValidationException.class,
            MethodArgumentNotValidException.class,HttpMessageConversionException.class,
            MethodArgumentTypeMismatchException.class})
    public ModelAndView invalidArgumentsHandler(HttpServletRequest request, HandlerMethod handlerMethod, Exception exception) {
        if(log.isDebugEnabled()){
            log.debug("catch exception : ",exception);
        }
        return resolveAndHandle(request,handlerMethod,exception, CommonResult.INVALID_ARGUMENTS,HttpStatus.BAD_REQUEST);
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
        return resolveAndHandle(request,null,exception,CommonResult.RESOURCE_NOT_FOUND,HttpStatus.NOT_FOUND);
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
        return resolveAndHandle(request,handlerMethod,exception,CommonResult.SERVICE_INTERNAL_ERROR,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ModelAndView resolveAndHandle(HttpServletRequest request, HandlerMethod handlerMethod,Exception ex,ErrorResult optionalError,HttpStatus optionalStatus){
        //其实在DefaultErrorAttributes已经有存储该异常，源码不熟悉，自己额外存储一次
        request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE,ex);

        ErrorResult errorResult = null;
        HttpStatus httpStatus = null;
        if(needResolve){
            for (ExceptionResolver resolver : exceptionResolvers) {
                if(resolver.canResolve(ex,optionalStatus)){
                    try {
                        errorResult = resolver.resolve(ex,request);
                        httpStatus = resolver.status(ex);
                    } catch(Exception e){
                        log.error("handler throw an exception!!!",e);
                    }
                    break;
                }
            }
        }
        if(errorResult == null){
            errorResult = optionalError;
        }
        if(httpStatus == null){
            httpStatus = optionalStatus;
        }

        // 5xx异常需要记录
        if(httpStatus != null && httpStatus.is5xxServerError()){
            exceptionCounter.add(ex);
        }
        return errorHandler.handle(request,handlerMethod,errorResult,httpStatus);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if(CollectionUtils.isNotEmpty(exceptionResolvers)){
            AnnotationAwareOrderComparator.sort(exceptionResolvers);
            needResolve = true;
        }
    }

}
