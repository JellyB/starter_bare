package com.huatu.springboot.web.tools.exception;

import com.huatu.common.ErrorResult;
import org.springframework.http.HttpStatus;

/**
 * 将ex转换为errorresult
 * 不要直接处理基类异常，会导致所有的默认子类异常处理失效
 * @author hanchao
 * @date 2017/9/19 16:41
 */
public interface ExceptionResolver {
    /**
     * 处理结果
     * @param ex
     * @return
     */
    ErrorResult resolve(Exception ex);

    /**
     * 是否可以处理
     * @param ex
     * @return
     */
    boolean canResolve(Exception ex);

    default HttpStatus status(Exception ex){
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
