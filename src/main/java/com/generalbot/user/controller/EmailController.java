package com.generalbot.user.controller;

import com.generalbot.common.api.Result;
import com.generalbot.user.service.EmailService;
import com.generalbot.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;

    public EmailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    /**
     * 发送验证码邮件
     * @param email 收件邮箱
     */
    @PostMapping
    public Result<String> sendVerificationCode(String email) {
        return emailService.sendVerificationCode(email);
    }

    /**
     * 验证验证码
     * @param email 收件邮箱
     * @param verificationCode 验证码
     */
    @GetMapping("/verifyCode")
    public Result<Boolean> verifyCode(String email, String verificationCode) {
        return Result.success(emailService.verifyCode(email, verificationCode));
    }

    /**
     * 判断邮箱是否存在
     */
    @GetMapping("/exists")
    public Result<Boolean> exists(String email) {
        return Result.success(userService.isExistByEmail(email));
    }
}
