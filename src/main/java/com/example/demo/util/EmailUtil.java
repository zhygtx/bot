package com.example.demo.util;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailUtil {

    private final JavaMailSender javaMailSender;

    public EmailUtil(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }


    /**
     * 发送邮件
     * @param email 收件邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    @Async
    public void sendEmail(String email, String subject, String content, Boolean isHtml){
        log.debug("发送邮件开始，邮箱：{} , 主题 {} ,内容 {} ",email,subject,content);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("1874743565@qq.com");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(content,isHtml);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.warn("发送邮件失败，邮箱：{}",email,e);
        }
    }

}
