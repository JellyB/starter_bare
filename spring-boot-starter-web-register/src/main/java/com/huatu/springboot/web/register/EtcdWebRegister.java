package com.huatu.springboot.web.register;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import mousio.client.retry.RetryNTimes;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.EtcdSecurityContext;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.transport.EtcdNettyClient;
import mousio.etcd4j.transport.EtcdNettyConfig;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 * @author hanchao
 * @date 2017/9/18 13:31
 */
@Slf4j
public class EtcdWebRegister implements WebRegister {
    public static final String REGISTER_DATA= "{\"host\":\"%s\",\"weight\":%s,\"port\":%s}";

    private String connectString;
    private String host;
    private int port;
    private String serverName;
    private String prefix;


    private EtcdClient etcdClient;
    private String etcdServerHome;//服务注册dir
    private String etcdServerNode;//服务注册路径


    public EtcdWebRegister(String connectString,String host,int port,String serverName,String prefix){
        this.host = host;
        this.port = port;
        this.serverName = serverName;
        this.prefix = prefix;
        this.connectString = connectString;

        this.etcdServerHome = prefix + serverName;
        this.etcdServerNode = etcdServerHome+"/" + host + ":" + port;

        this.etcdClient = buildEtcdClient(this.connectString);
    }


    private EtcdClient buildEtcdClient(String connectString){
        final URI[] etcdServers = Stream.of(connectString.split(",")).map(etcdServer -> URI.create(etcdServer)).toArray(URI[]::new);
        EtcdNettyConfig nettyConfig = new EtcdNettyConfig();
        EventLoopGroup loopGroup = new NioEventLoopGroup(1);
        nettyConfig.setEventLoopGroup(loopGroup);
        EtcdNettyClient etcdNettyClient = new EtcdNettyClient(nettyConfig, EtcdSecurityContext.NONE, etcdServers);
        EtcdClient etcdClient = new EtcdClient(etcdNettyClient);
        etcdClient.setRetryHandler(new RetryNTimes(1000,3));
        return etcdClient;
    }

    @Override
    public boolean regist() {
        log.info("start register server http(s)://{}:{} to {} success.",host,port,etcdServerNode);
        Throwable throwable = null;
        try {
            etcdClient.put(etcdServerNode, String.format(REGISTER_DATA,host,5,port))
                    .timeout(3, TimeUnit.SECONDS)
                    .send().get();
            log.info("register to {} success.",etcdServerNode);
            return true;
        } catch (IOException e) {
            throwable = e;
        } catch (EtcdException e) {
            throwable = e;
        } catch (EtcdAuthenticationException e) {
            log.error("register fail.",e);
            return false;
        } catch (TimeoutException e) {
            throwable = e;
        }

        //注册失败
        if (throwable != null) {
            log.error("register fail.",throwable);
        }
        return false;
    }

    @Override
    public boolean unregister() {
        log.info("unregister the server from etcd. node={}",etcdServerNode);
        try {
            etcdClient.delete(etcdServerNode).send().get();
            log.info("unregister the server from etcd success. node={}",etcdServerNode);
            return true;
        } catch (IOException e) {
            log.warn("unregister fail.",e);
        } catch (EtcdException e) {
            log.warn("unregister fail.",e);
        } catch (EtcdAuthenticationException e) {
            log.warn("unregister fail.",e);
        } catch (TimeoutException e) {
            log.warn("unregister fail.",e);
        }
        try {
            etcdClient.close();
        } catch (IOException e) {
            log.error("close etcdclient error...",e);
        }
        return false;
    }


    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }
}
