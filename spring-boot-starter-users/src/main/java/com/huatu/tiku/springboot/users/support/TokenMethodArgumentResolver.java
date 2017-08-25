package com.huatu.tiku.springboot.users.support;

import com.huatu.tiku.springboot.users.bean.UserSession;
import com.huatu.tiku.springboot.users.service.UserSessionService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;

/**
 * @author hanchao
 * @date 2017/8/24 19:45
 */
public class TokenMethodArgumentResolver extends RequestHeaderMethodArgumentResolver{
    private UserSessionService userSessionService;
    /**
     * @param beanFactory a bean factory to use for resolving  ${...}
     *                    placeholder and #{...} SpEL expressions in default values;
     *                    or {@code null} if default values are not expected to have expressions
     */
    public TokenMethodArgumentResolver(ConfigurableBeanFactory beanFactory,UserSessionService userSessionService) {
        super(beanFactory);
        this.userSessionService = userSessionService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        //参数类型目前大概是int Integer这样的
        return (parameter.hasParameterAnnotation(Token.class) && isLegalType(parameter.getParameterType()));
    }

    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
        Token annotation = parameter.getParameterAnnotation(Token.class);
        return new TokenNamedValueInfo(annotation);
    }

    /**
     * 参数装配
     * @param name
     * @param parameter
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        String token = (String) super.resolveName(name, parameter, request);
        Class<?> clazz = parameter.getParameterType();
        boolean needSimple = false;
        Token config = parameter.getParameterAnnotation(Token.class);

        if(clazz == int.class || Integer.class.isAssignableFrom(clazz)){
            //需要返回int类型
            needSimple = true;
        }
        if(!config.check() && needSimple){
            return userSessionService.getUid(token);
        }

        UserSession userSession = userSessionService.getUserSession(token);
        if(userSession == null){
            return needSimple ? -1:null;
        }
        if(config.check()){
            userSessionService.assertSession(userSession);
        }
        return needSimple ? userSession.getId():userSession;
    }

    /**
     * 是否合法的参数类型
     * @param clazz
     * @return
     */
    private boolean isLegalType(Class<?> clazz){
        if(clazz == int.class || Integer.class.isAssignableFrom(clazz)){
            return true;
        }
        if(UserSession.class.isAssignableFrom(clazz)){
            return true;
        }
        return false;
    }



    private static class TokenNamedValueInfo extends NamedValueInfo {

        private TokenNamedValueInfo(Token annotation) {
            super(annotation.name(), annotation.required(), annotation.defaultValue());
        }
    }
}
