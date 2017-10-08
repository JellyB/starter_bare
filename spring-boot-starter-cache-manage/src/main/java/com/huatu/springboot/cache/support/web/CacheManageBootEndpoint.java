package com.huatu.springboot.cache.support.web;

import com.huatu.springboot.cache.support.CacheManageEndPoint;

/**
 * @author hanchao
 * @date 2017/10/7 16:35
 */
@CacheManageEndPoint
public class CacheManageBootEndpoint extends ServletWrappingEndpoint {

    public CacheManageBootEndpoint(){
        super(CacheManageServlet.class, "cacheManageServlet", "/cacheManage",
                true, true);
    }

}
