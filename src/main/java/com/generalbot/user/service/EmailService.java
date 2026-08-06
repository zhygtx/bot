package com.generalbot.user.service;

import com.generalbot.common.api.Result;

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
}
