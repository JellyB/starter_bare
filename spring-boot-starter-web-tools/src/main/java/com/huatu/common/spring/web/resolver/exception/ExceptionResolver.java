package com.huatu.common.spring.web.resolver.exception;

import com.huatu.common.ErrorResult;

/**
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
}
