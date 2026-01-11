package com.example.demo.service;

import com.example.demo.pojo.Result;

/**
 * 邮箱服务接口
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     * @param email 收件邮箱
     * @return 发送结果
     */
    Result<String> sendVerificationCode(String email);

    /**
     * 验证验证码
     * @param email 收件邮箱
     * @param verificationCode 验证码
     * @return 验证结果
     */
    boolean verifyCode(String email, String verificationCode);

    /**
     * 发送邮件
     * @param email 收件邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    Boolean sendEmail(String email, String subject, String content, Boolean isHtml);
}
