package com.gxl.plancore.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务
 */
@Service
public class EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * 发送找回密码邮件
     * 
     * @param toEmail 收件人邮箱
     * @param newPassword 新密码
     */
    public void sendPasswordResetEmail(String toEmail, String newPassword) {
        log.info("发送找回密码邮件: toEmail={}", toEmail);
        
        String subject = "MaidenPlan - Password Recovery";
        String content = buildPasswordResetEmailContent(newPassword);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        
        try {
            mailSender.send(message);
            log.info("找回密码邮件发送成功: toEmail={}", toEmail);
        } catch (Exception e) {
            log.error("找回密码邮件发送失败: toEmail={}, error={}", toEmail, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
    
    /**
     * 构建找回密码邮件内容
     */
    private String buildPasswordResetEmailContent(String password) {
        StringBuilder sb = new StringBuilder();
        sb.append("Hi,\n\n");
        sb.append("Here is the password for your MaidenPlan account:\n\n");
        sb.append(password);
        sb.append("\n\n");
        sb.append("We recommend updating your password once you sign in to keep your account secure.\n\n");
        sb.append("If you didn't request this email, please ignore it.\n\n");
        sb.append("Best,\n");
        sb.append("The MaidenPlan Team");
        return sb.toString();
    }
}
