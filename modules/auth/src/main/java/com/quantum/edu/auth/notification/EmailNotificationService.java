package com.quantum.edu.auth.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.email.verification-base-url:http://localhost:3000/verify-email}")
    private String verificationBaseUrl;

    @Value("${app.email.from:noreply@quantumedu.com}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean enabled;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerification(String recipient, String verificationToken) {
        String verificationLink = verificationBaseUrl + "?token=" + verificationToken;

        if (!enabled) {
            log.info("Email disabled. Verification link for {}: {}", recipient, verificationLink);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipient);
            message.setSubject("Verify your Quantum Education email");
            message.setText("Please verify your email by clicking the link below:\n\n" + verificationLink +
                    "\n\nThis link expires in 24 hours.\n\nIf you did not create an account, please ignore this email.");
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send verification email to {}. Link: {}", recipient, verificationLink, e);
        }
    }
}
