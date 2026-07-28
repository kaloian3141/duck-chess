package com.duckchess.duck_chess.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService 
{

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public MailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromAddress
    ) 
    {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationCode(String toEmail, String code) 
    {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Duck Chess — verify your email");
        msg.setText("""
                Welcome to Duck Chess!

                Your verification code is: %s

                This code expires in 60 minutes.
                """.formatted(code));
        mailSender.send(msg);
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(toEmail);
        msg.setSubject("Duck Chess — password reset");
        msg.setText("""
                A password reset was requested for your Duck Chess account.

                Your reset code is: %s

                This code expires in 30 minutes.

                If you didn't request this, you can safely ignore this email —
                your password won't change unless someone enters this code.
                """.formatted(code));
        mailSender.send(msg);
    }
}