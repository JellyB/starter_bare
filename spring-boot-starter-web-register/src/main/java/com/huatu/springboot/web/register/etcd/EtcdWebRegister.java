package com.huatu.springboot.web.register.etcd;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.net.HttpHeaders;
import com.huatu.common.utils.encode.CharsetConsts;
import com.huatu.springboot.web.register.WebRegister;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author hanchao
 * @date 2017/9/18 13:31
 */
@Slf4j
public class EtcdWebRegister implements WebRegister {
    public static final String PATH_PRE = "/v2/keys/";
    public static final String REGISTER_DATA= "{\"host\":\"%s\",\"weight\":%s,\"port\":%s}";

    private String connectString;
    private String host;
    private int port;
    private String serverName;
    private String prefix;


    private String etcdServerHome;//服务注册dir
    private String etcdServerNode;//服务注册路径

    private List<String> etcdServers;

    private static Thread maintainThread;
    private static AtomicBoolean _threadLock = new AtomicBoolean(false);


    public EtcdWebRegister(String connectString, String host, int port, String serverName, String prefix){
        this.host = host;
        this.port = port;
        this.serverName = serverName;
        this.prefix = prefix;
        this.connectString = connectString;

        this.etcdServerHome = prefix + serverName;
        this.etcdServerNode = etcdServerHome+"/" + host + ":" + port;

        this.etcdServers = Splitter.on(",").splitToList(connectString);
    }


    private boolean doRegist(){
        boolean success = false;
        for (String etcdServer : etcdServers) {
            URL url;
            HttpURLConnection connection = null;
            try {
                url = new URL(etcdServer+PATH_PRE+etcdServerHome);
                connection = buildEtcdConnection(url,HttpMethod.PUT);

                Node.Request request = Node.Request.builder()
                        .value(String.format(REGISTER_DATA,host,5,port))
                        .ttl(120)
                        .build();

                OutputStream out = connection.getOutputStream();
                out.write(buildBody(request).getBytes(CharsetConsts.DEFAULT_CHARSET));
                out.flush();
                out.close();
                if (connection.getResponseCode() == 200 || connection.getResponseCode() == 201){
                    success = true;
                }
                InputStream in = connection.getInputStream();
                String response = IOUtils.toString(in);
                in.close();
                log.info("request node: {} , result: {} , response: {} ",etcdServer,success,response);
                if(success){
                    break;
                }
            } catch(Exception e){
                log.error("request node : {} failed...",etcdServer);
            } finally {
                if(connection != null){
                    try {
                        connection.disconnect();
                    } catch(Exception e){
                    }
                }
            }
        }
        return success;
    }



    @Override
    public boolean regist() {
        log.info("start register server http(s)://{}:{} to {} .",host,port,etcdServerNode);
        if(_threadLock.compareAndSet(false,true)){
            maintainThread = new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(60); // 一分钟续约一次
                        doRegist();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        return doRegist();
    }

    @Override
    public boolean unregister() {
        log.info("start unregister the server from etcd. node={}",etcdServerNode);
        boolean success = false;
        for (String etcdServer : etcdServers) {
            URL url;
            HttpURLConnection connection = null;
            try {
                url = new URL(etcdServer+PATH_PRE+etcdServerHome);
                connection = buildEtcdConnection(url,HttpMethod.DELETE);
                if (connection.getResponseCode() == 200 || connection.getResponseCode() == 201){
                    success = true;
                }
                InputStream in = connection.getInputStream();
                String response = IOUtils.toString(in);
                in.close();
                log.info("request node: {} , result: {} , response: {} ",etcdServer,success,response);
                if(success){
                    break;
                }
            } catch(Exception e){
                log.error("request node : {} failed...",etcdServer);
            } finally {
                if(connection != null){
                    try {
                        connection.disconnect();
                    } catch(Exception e){
                    }
                }
            }
        }
        return success;
    }


    /**
     * 转换为map消息体
     * @param request
     * @return
     */
    private String buildBody(Node.Request request){
        Map<String,Object> map = (Map) JSON.toJSON(request);
        return Joiner.on("&").withKeyValueSeparator("&").join(map);
    }

    private HttpURLConnection buildEtcdConnection(URL url,HttpMethod method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod(method.toString());
        switch (method){
            case PUT:
            case POST:
                connection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
                break;
            default:
                break;
        }

        connection.setConnectTimeout(1000);
        connection.setReadTimeout(5000);
        return connection;
    }
}
