package com.huatu.common.spring.web.converter;

import org.omg.CORBA.Object;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * @author hanchao
 * @date 2016/12/2 0:14
 */
public class FormMessageConverter extends AbstractHttpMessageConverter<Map<String,Object>> {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private Charset charset;
    public FormMessageConverter(){
        super(MediaType.APPLICATION_FORM_URLENCODED);
        this.charset = DEFAULT_CHARSET;
    }

    public FormMessageConverter(Charset charset) {
        super(MediaType.APPLICATION_FORM_URLENCODED);
        this.charset = charset;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        if(Map.class.isAssignableFrom(clazz)){
            return true;
        }
        return false;
    }

    /**
     * unsupport
     * @param clazz
     * @param inputMessage
     * @return
     * @throws IOException
     * @throws HttpMessageNotReadableException
     */
    @Override
    protected Map<String, Object> readInternal(Class<? extends Map<String, Object>> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return null;
    }

    @Override
    protected void writeInternal(Map<String, Object> stringObjectMap, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        StringBuffer str = new StringBuffer();
        for(Iterator<Map.Entry<String, Object>> it = stringObjectMap.entrySet().iterator(); it.hasNext();){
            Map.Entry<String, Object> entry = it.next();
            if(entry.getValue() != null){
                str.append(entry.getKey()+"="+ URLEncoder.encode(String.valueOf(entry.getValue()),charset.name()));
            }
            if(it.hasNext()){
                str.append("&");
            }
        }
        StreamUtils.copy(str.toString(), charset, outputMessage.getBody());
    }

}
