package com.example.demo.controller;

import com.example.demo.pojo.Result;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public EmailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    /**
     * 发送验证码邮件
     * @param email 收件邮箱
     */
    @RequestMapping("/sendVerificationCode")
    public Result<String> sendVerificationCode(String email) {
        emailService.sendVerificationCode(email);
        return Result.success();
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

    /**
     * 判断邮箱是否存在
     */
    @RequestMapping("/exists")
    public Result<Boolean> exists(String email) {
        return Result.success(userService.isExistByEmail(email));
    }
}
