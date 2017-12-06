package com.huatu.springboot.web.register.listener;

import com.huatu.springboot.web.register.WebRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hanchao
 * @date 2017/12/6 17:33
 */
@RestController
@RequestMapping("/admin/webRegister")
public class RegisterController {
    @Autowired
    private WebRegister webRegister;
    @PostMapping
    public Object control(@RequestParam("_action")String action){
        boolean result = false;
        //不允许注销，因为注销后将无法直接恢复
        switch (action){
            case "regist":
                result = webRegister.regist();
                break;
            case "pause":
                result = webRegister.pause();
                break;
            case "resume":
                result = webRegister.resume();
                break;
            default:
                break;
        }
        return result;
    }
}
