package com.huatu.tiku.springboot.users.bean;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.io.IOException;

/**
 * 用到这个类基本是有问题，usersession不建议default
 * @author hanchao
 * @date 2017/8/25 19:08
 */
@Deprecated
public class UserSessionEditor extends PropertyEditorSupport {
    private ObjectMapper objectMapper;
    public UserSessionEditor(){
        this.objectMapper = new ObjectMapper();
    }
    /**
     * Parse the Date from the given text, using the specified DateFormat.
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.isEmpty(text)) {
            // Treat empty String as null value.
            setValue(null);
        } else {
            try {
                setValue( objectMapper.readValue(text, UserSession.class));
            } catch (IOException e) {
                throw new IllegalArgumentException("还有这种操作？");
            }
        }
    }

    /**
     * Format the Date as String, using the specified DateFormat.
     */
    @Override
    public String getAsText() {
        try {
            return objectMapper.writeValueAsString(getValue());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("还有这种操作？");
        }
    }
}
