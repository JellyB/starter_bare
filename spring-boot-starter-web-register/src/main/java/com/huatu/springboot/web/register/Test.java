package com.huatu.springboot.web.register;

import com.google.common.collect.Lists;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import mousio.client.promises.ResponsePromise;
import mousio.client.retry.RetryNTimes;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.EtcdSecurityContext;
import mousio.etcd4j.promises.EtcdResponsePromise;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import mousio.etcd4j.transport.EtcdNettyClient;
import mousio.etcd4j.transport.EtcdNettyConfig;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author hanchao
 * @date 2017/9/21 13:27
 */
public class Test {
    public static void main(String[] args) throws IOException, EtcdAuthenticationException, TimeoutException, EtcdException, InterruptedException {
        EtcdNettyConfig nettyConfig = new EtcdNettyConfig();
        EventLoopGroup loopGroup = new NioEventLoopGroup(1);
        nettyConfig.setEventLoopGroup(loopGroup);
        EtcdNettyClient etcdNettyClient = new EtcdNettyClient(nettyConfig, EtcdSecurityContext.NONE, URI.create("http://192.168.100.19:2379/"));
        EtcdClient etcdClient = new EtcdClient(etcdNettyClient);
        etcdClient.setRetryHandler(new RetryNTimes(1000,3));
        final List<Throwable> list = Lists.newArrayList();
        try {
            EtcdResponsePromise<EtcdKeysResponse> send = etcdClient.put("/test/k2", "123").send();
            send.addListener(new ResponsePromise.IsSimplePromiseResponseHandler<EtcdKeysResponse>() {
                @Override
                public void onResponse(ResponsePromise<EtcdKeysResponse> response) {
                    if(!response.getNettyPromise().isSuccess()){
                        list.add(response.getException());
                    }
                }
            });
        } catch(Exception e){
            e.printStackTrace();
        }

    }
}
