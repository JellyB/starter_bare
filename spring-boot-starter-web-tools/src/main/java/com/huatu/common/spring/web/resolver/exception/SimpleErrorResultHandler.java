package com.huatu.common.spring.web.resolver.exception;

import com.alibaba.fastjson.JSON;
import com.huatu.common.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.springframework.web.servlet.view.xml.MappingJackson2XmlView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 所有方法可被重写，重新注入到GlobalExceptionhandler
 * @author hanchao
 * @date 2017/4/9 18:43
 */
@Slf4j
public class SimpleErrorResultHandler implements ErrorResultHandler {
    private static final MediaType DEFAULT_MEDIATYPE = MediaType.APPLICATION_JSON;


    @Override
    public ModelAndView handle(HttpServletRequest request,HandlerMethod handlerMethod, Object errorResult, HttpStatus status) {
        MediaType type = getProduceType(request,handlerMethod);
        ModelAndView modelAndView = produce(type,errorResult,status);
        if(modelAndView.getStatus() == null){
            modelAndView.setStatus(status == null ? HttpStatus.OK : status);
        }
        return modelAndView;
    }

    /**
     * 1,json 2,html
     * @param handlerMethod
     * @param request
     * @return
     */
    public MediaType getProduceType(HttpServletRequest request,HandlerMethod handlerMethod){
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if(accept != null){
            return contentType2MediaType(accept);
        }
        if(handlerMethod == null){
            return DEFAULT_MEDIATYPE;
        }
        final RequestMapping requestMapping = handlerMethod.getMethodAnnotation(RequestMapping.class);
        if (requestMapping != null &&  requestMapping.produces()!= null && requestMapping.produces().length>0) {//处理RequestMapping 含有指定输出类型
            return contentType2MediaType(requestMapping.produces()[0]);
        }
        //其次检查controller的RequestMapping
        final RequestMapping classRequestMapping = handlerMethod.getMethod().getDeclaringClass().getAnnotation(RequestMapping.class);
        if (classRequestMapping != null &&  classRequestMapping.produces()!= null && classRequestMapping.produces().length>0) {//处理RequestMapping 含有指定输出类型
            return contentType2MediaType(classRequestMapping.produces()[0]);
        }
        final ResponseBody responseBody = handlerMethod.getMethodAnnotation(ResponseBody.class);
        if (responseBody != null) {//存在注解ResponseBody,则认为返回json
            return MediaType.APPLICATION_JSON;
        }
        final RestController restController = handlerMethod.getMethod().getDeclaringClass().getAnnotation(RestController.class);
        if(restController != null){
            return MediaType.APPLICATION_JSON;
        }
        return DEFAULT_MEDIATYPE;
    }

    protected MediaType contentType2MediaType(String contentType){
        if(contentType.startsWith(MediaType.TEXT_HTML_VALUE)){
            return MediaType.TEXT_HTML;
        }
        if(contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)){
            return MediaType.APPLICATION_JSON;
        }
        if(contentType.startsWith(MediaType.APPLICATION_XML_VALUE)){
            return MediaType.APPLICATION_XML;
        }
        return DEFAULT_MEDIATYPE;
    }


    public ModelAndView produce(MediaType type,Object errorResult,HttpStatus status){
        if(type == MediaType.TEXT_HTML){
            return produceHtml(errorResult,status);
        }else if(type == MediaType.APPLICATION_JSON){
            return produceJson(errorResult,status);
        }else if(type == MediaType.APPLICATION_XML){
            return produceXml(errorResult,status);
        }
        throw new IllegalArgumentException("unknown media type to produce...");
    }

    private ModelAndView produceJson(Object errorResult,HttpStatus status) {
        final MappingJackson2JsonView jackson2JsonView = new MappingJackson2JsonView();
        jackson2JsonView.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        ModelAndView modelAndView = new ModelAndView(jackson2JsonView);
        /*BeanMap map = new BeanMap(errorResult);
        modelAndView.addAllObjects(map);*/
        modelAndView.addAllObjects((Map<String, Object>) JSON.toJSON(errorResult));
        return modelAndView;
    }

    private ModelAndView produceXml(Object errorResult,HttpStatus status) {
        final MappingJackson2XmlView jackson2XmlView = new MappingJackson2XmlView();
        jackson2XmlView.setContentType(MediaType.APPLICATION_XML_VALUE+";charset=UTF-8");
        ModelAndView modelAndView = new ModelAndView(jackson2XmlView);
        modelAndView.addAllObjects((Map<String, Object>) JSON.toJSON(errorResult));
        return modelAndView;
    }

    private ModelAndView produceHtml(Object errorResult,HttpStatus status) {
        //应该对于状态码返回不同的页面,临时写死

        if(errorResult instanceof ErrorResult){
            return new ModelAndView("redirect:http://ns.huatu.com/pc/error/5xx?code="+((ErrorResult) errorResult).getCode()+"&message="+((ErrorResult) errorResult).getMessage(), HttpStatus.FOUND);
        }
        return new ModelAndView("redirect:http://ns.huatu.com/pc/error/5xx",HttpStatus.FOUND);
    }

}
