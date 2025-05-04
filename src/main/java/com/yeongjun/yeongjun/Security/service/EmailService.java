package com.yeongjun.yeongjun.Security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    
    @Value("${spring.profiles.active}")
    private String activeProfile;
    
    @Value("${app.base-url.dev}")
    private String devBaseUrl;
    
    @Value("${app.base-url.prod}")
    private String prodBaseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) {
        String baseUrl = "dev".equals(activeProfile) ? devBaseUrl : prodBaseUrl;
        String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("이메일 인증");
        message.setText("이메일 인증을 완료하려면 다음 링크를 클릭하세요: " + verificationUrl);
        mailSender.send(message);
    }
} 