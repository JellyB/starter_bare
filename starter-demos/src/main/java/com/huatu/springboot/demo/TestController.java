package com.huatu.springboot.demo;

import com.huatu.tiku.springboot.users.bean.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author hanchao
 * @date 2017/8/25 8:59
 */
@Controller
@RequestMapping("/")
public class TestController {
    @RequestMapping("/")
    @ResponseBody
    public Object test(@Token(check = true) UserSession userSession){
        return userSession;
    }
}
