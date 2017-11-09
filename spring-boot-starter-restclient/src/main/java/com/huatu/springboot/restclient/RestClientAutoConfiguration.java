package com.huatu.springboot.restclient;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.google.common.collect.Lists;
import com.huatu.springboot.restclient.support.RestClientConfig;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * @author hanchao
 * @date 2017/8/29 11:45
 */
@Configuration
@EnableApolloConfig("htonline.rest-clients")
@EnableConfigurationProperties(RestClientConfig.class)
public class RestClientAutoConfiguration {


    @Configuration
    @ConditionalOnClass(OkHttpClient.class)
    protected static class OkHttpClientConfiguration {
        @Autowired
        private RestClientConfig config;

        @Bean
        @ConditionalOnMissingBean(OkHttpClient.class)
        @ConditionalOnProperty(value = "htonline.okhttp.enabled",havingValue = "true",matchIfMissing = true)
        public OkHttpClient okHttpClient(){
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            return builder.connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(config.getWriteTimeout(),TimeUnit.MILLISECONDS)
                    .readTimeout(config.getReadTimeout(),TimeUnit.MILLISECONDS)
                    .connectionPool(new ConnectionPool(config.getMaxTotal(),config.getKeepAliveTime(), TimeUnit.MILLISECONDS))
                    .followRedirects(config.isRedirectable())//不跟踪重定向
                    .build();
        }

    }


    @Configuration
    @ConditionalOnClass(HttpClient.class)
    @ConditionalOnMissingClass("okhttp3.OkHttpClient")
    @ConditionalOnProperty(value = "htonline.httpclient.enabled",havingValue = "true",matchIfMissing = true)
    protected static class ApacheHttpclientConfiguration {
        @Autowired
        private RestClientConfig config;
        /**
         * 无配置情况下，默认生成httpclient的客户端
         * @return
         * @throws Exception
         */
        @Bean
        @ConditionalOnMissingBean(HttpClient.class)
        @ConditionalOnProperty(value = "htonline.httpclient.enabled",havingValue = "true",matchIfMissing = true)
        public HttpClient httpClient()  throws Exception {
            ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();

            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            LayeredConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", plainsf).register("https", sslsf).build();
            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
            // 将最大连接数增加到200
            cm.setMaxTotal(config.getMaxTotal());
            // 将每个路由基础的连接增加到20
            cm.setDefaultMaxPerRoute(config.getMaxPerRoute());

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(config.getConnectRequestTimeout())
                    .setConnectTimeout(config.getConnectTimeout())
                    .setSocketTimeout(config.getSocketTimeout())
                    .setRedirectsEnabled(config.isRedirectable())
                    .build();

            //请求重试处理
            HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
                public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                    if (executionCount >= 3) {// 如果已经重试了5次，就放弃
                        return false;
                    }
                    if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                        return true;
                    }
                    if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                        return false;
                    }
                    if (exception instanceof InterruptedIOException) {// 超时
                        return false;
                    }
                    if (exception instanceof UnknownHostException) {// 目标服务器不可达
                        return false;
                    }
                    if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                        return false;
                    }
                    if (exception instanceof SSLException) {// ssl握手异常
                        return false;
                    }

                    HttpClientContext clientContext = HttpClientContext.adapt(context);
                    HttpRequest request = clientContext.getRequest();
                    // 如果请求是幂等的，就再次尝试
                    if (!(request instanceof HttpEntityEnclosingRequest)) {
                        return true;
                    }
                    return false;
                }
            };

            HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).build();
            return httpClient;
        }
    }


    @Configuration
    @ConditionalOnClass(RestTemplate.class)
    protected static class RestTemplateConfiguration {
        @Bean
        @ConditionalOnBean(OkHttpClient.class)
        public ClientHttpRequestFactory okHttp3ClientHttpRequestFactory(@Autowired OkHttpClient okHttpClient){
            return new OkHttp3ClientHttpRequestFactory(okHttpClient);
        }


        @Bean
        @ConditionalOnBean(OkHttpClient.class)
        public ClientHttpRequestFactory httpClientHttpRequestFactory(@Autowired HttpClient httpClient){
            return new HttpComponentsClientHttpRequestFactory(httpClient);
        }

        @Bean
        public RestTemplate restTemplate(@Autowired ClientHttpRequestFactory clientHttpRequestFactory){
            RestTemplate template = new RestTemplate(clientHttpRequestFactory);
            MappingJackson2HttpMessageConverter compatibleWechat = new MappingJackson2HttpMessageConverter();
            compatibleWechat.setSupportedMediaTypes(Lists.newArrayList(MediaType.TEXT_PLAIN));
            template.getMessageConverters().add(compatibleWechat);
            return template;
        }
    }



}
