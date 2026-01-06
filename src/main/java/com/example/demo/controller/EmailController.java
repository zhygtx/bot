package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/email")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * 发送验证码邮件
     * @param email 收件邮箱
     */
    @RequestMapping("/sendVerificationCode")
    public Result<String> sendVerificationCode(String email) {
        if (emailService.sendVerificationCode(email)){
            return Result.success("发送成功");
        }else{
            return Result.error("发送失败");
        }
    }

    /**
     * 验证验证码
     * @param email 收件邮箱
     * @param verificationCode 验证码
     */
    @RequestMapping("/verifyCode")
    public Result<Boolean> verifyCode(String email, String verificationCode) {
        return Result.success(emailService.verifyCode(email, verificationCode));
    }
}
