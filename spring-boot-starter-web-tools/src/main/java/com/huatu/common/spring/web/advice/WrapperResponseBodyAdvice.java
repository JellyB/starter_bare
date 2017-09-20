package com.huatu.common.spring.web.advice;

import com.huatu.common.Result;
import com.huatu.common.SuccessResponse;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 如果不是spring boot环境，应该会忽略掉conditional
 * Created by shaojieyue
 * Created time 2016-04-18 09:56
 */

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@ConditionalOnProperty(value = "htonline.wrapper.enabled", havingValue = "true", matchIfMissing = true)
public class WrapperResponseBodyAdvice implements ResponseBodyAdvice, InitializingBean {

    private Set<String> ignoreClasses = new HashSet();
    private PathMatcher pathMatcher = new AntPathMatcher();
    private List<String> ignoreUrls = new ArrayList();

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (isHtml(mediaType)) {
            return o;
        }
        //优先返回无需wrapper的，保证效率
        if(o != null && o instanceof Result){
            return o;
        }
        if (o != null && ignoreClasses.contains(o.getClass().getCanonicalName())) {
            return o;
        }
        if(serverHttpRequest instanceof ServletServerHttpRequest){
            String path = getRequestPath(((ServletServerHttpRequest) serverHttpRequest).getServletRequest());
            for (String ignoreUrl : ignoreUrls) {
                if(pathMatcher.match(ignoreUrl,path)){
                    return o;
                }
            }
        }
        //最后进行包装
        return new SuccessResponse(o);
    }


    /**
     * 返回是否是html
     *
     * @param mediaType
     * @return
     */
    public boolean isHtml(MediaType mediaType) {
        return mediaType.includes(MediaType.TEXT_HTML);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initIgnoreClasses(ignoreClasses);
        initIgnoreUrls(ignoreUrls);
    }

    private void initIgnoreUrls(List<String> ignoreUrls) {
        ignoreUrls.add("/_monitor/**");
    }


    public void initIgnoreClasses(Set<String> ignores) {
        ignores.add("org.springframework.hateoas.ResourceSupport");
    }

    private String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();

        if (request.getPathInfo() != null) {
            url += request.getPathInfo();
        }

        return url;
    }

    private String getRequestUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        if (request.getQueryString() != null) {
            url += ("?"+request.getQueryString());
        }
        return url;
    }

}
