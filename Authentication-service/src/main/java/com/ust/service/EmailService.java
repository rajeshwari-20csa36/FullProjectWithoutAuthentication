package com.ust.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String to, String token) {
        // Compose email content
        String subject = "Password Reset Request";
        String text = "You have requested to reset your password. Please use the following token to reset your password: " + token;
        // Create a SimpleMailMessage object
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        // Send the email
        mailSender.send(message);
    }
}