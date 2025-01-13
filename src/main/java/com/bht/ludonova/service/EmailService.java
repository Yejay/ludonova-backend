package com.bht.ludonova.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your LudoNova Verification Code");

        String emailContent = String.format("""
            <html>
                <body>
                    <h2>Welcome to LudoNova!</h2>
                    <p>Your verification code is:</p>
                    <h1 style="font-size: 32px; letter-spacing: 2px; color: #4F46E5; text-align: center; padding: 20px; background-color: #F3F4F6; border-radius: 8px;">%s</h1>
                    <p>Enter this code in the app to verify your email address.</p>
                    <p>This code will expire in 24 hours.</p>
                    <p>If you did not create an account, please ignore this email.</p>
                </body>
            </html>
            """, code);

        helper.setText(emailContent, true);
        mailSender.send(message);
    }
} 