package com.example.demo.service.impl;

import com.example.demo.pojo.entity.Result;
import com.example.demo.service.EmailService;
import com.example.demo.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailUtil emailUtil;

    public EmailServiceImpl(RedisTemplate<String, Object> redisTemplate, EmailUtil emailUtil) {
        this.redisTemplate = redisTemplate;
        // 解决 Windows 系统下 mailcap 文件找不到的问题
        System.setProperty("mail.mime.contenthandler.autoinit", "false");
        this.emailUtil = emailUtil;
    }

    /**
     * 发送验证码邮件
     * @param email 收件邮箱
     */
    @Override
    public Result<String> sendVerificationCode(String email){
        try {
            String redisKey = "verification:code:" + email;
            if (redisTemplate.hasKey(redisKey)){
                return Result.error(400,"验证码已发送请检查邮箱");
            }

            // 生成6位随机数
            int verificationCode = (int)((Math.random()*9+1)*100000);

            // 从静态资源读取HTML模板并替换验证码
            String htmlContent = loadAndReplaceTemplate(verificationCode);

            emailUtil.sendEmail(email, "验证码", htmlContent, true);

            // 保存验证码到Redis，设置5分钟过期
            redisTemplate.opsForValue().set(redisKey, String.valueOf(verificationCode), 5, TimeUnit.MINUTES);
            log.info("发送验证码邮件成功，邮箱：{}验证码：{}",email,verificationCode);
            return Result.success("发送验证码成功");
        } catch (Exception e) {
            log.warn("发送验证码邮件失败，邮箱：{}",email,e);
            return Result.error(500,"发送验证码邮件失败");
        }
    }

    /**
     * 从静态资源加载HTML模板并替换占位符
     * @param verificationCode 验证码
     * @return HTML内容
     */
    private String loadAndReplaceTemplate(int verificationCode) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/verification-code.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return template.replace("[[${verificationCode}]]", String.valueOf(verificationCode));
        } catch (Exception e) {
            log.error("读取邮件模板失败", e);
            throw new RuntimeException("读取邮件模板失败", e);
        }
    }

    /**
     * 验证验证码
     * @param email 收件邮箱
     * @param verificationCode 验证码
     * @return 验证结果
     */
    @Override
    public boolean verifyCode(String email, String verificationCode){
        String redisKey = "verification:code:" + email;
        String redisCode = (String) redisTemplate.opsForValue().get(redisKey);// 从Redis中获取验证码
        if (redisCode != null && redisCode.equals(verificationCode)){
            redisTemplate.delete(redisKey);// 验证成功，删除Redis中的验证码
        }
        return redisCode != null && redisCode.equals(verificationCode);
    }
}
