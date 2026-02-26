package com.example.demo.service.impl;

import com.example.demo.pojo.Result;
import com.example.demo.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;

    public EmailServiceImpl(JavaMailSender javaMailSender, TemplateEngine templateEngine, RedisTemplate<String, Object> redisTemplate) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
        this.redisTemplate = redisTemplate;
        // 解决 Windows 系统下 mailcap 文件找不到的问题
        System.setProperty("mail.mime.contenthandler.autoinit", "false");
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

            // 创建模板上下文并设置变量
            Context context = new Context();
            context.setVariable("verificationCode", verificationCode);

            // 处理HTML模板
            String htmlContent = templateEngine.process("verification-code", context);

            if (!sendEmail(email, "验证码", htmlContent, true)) {
                log.warn("发送验证码邮件失败，邮箱：{}",email);
                return Result.error(500,"发送验证码邮件失败");
            }

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

    /**
     * 发送邮件
     * @param email 收件邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    @Override
    public Boolean sendEmail(String email, String subject, String content, Boolean isHtml){
        log.debug("发送邮件开始，邮箱：{} , 主题 {} ,内容 {} ",email,subject,content);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("1874743565@qq.com");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(content,isHtml);
            javaMailSender.send(message);
            return true;
        } catch (Exception e) {
            log.warn("发送邮件失败，邮箱：{}",email,e);
            return false;
        }
    }
}
