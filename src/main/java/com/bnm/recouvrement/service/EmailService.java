package com.bnm.recouvrement.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String content, boolean isHtmlContent) throws MessagingException {
        System.out.println("Tentative d'envoi d'email à: " + to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, isHtmlContent);
            
            mailSender.send(message);
            System.out.println("Email envoyé avec succès à: " + to);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi d'email à " + to + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}