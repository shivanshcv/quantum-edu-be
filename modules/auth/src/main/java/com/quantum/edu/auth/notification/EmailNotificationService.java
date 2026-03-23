package com.quantum.edu.auth.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    @Value("${spring.mail.host:localhost}")
    private String mailHost;

    @Value("${spring.mail.port:587}")
    private int mailPort;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerification(String recipient, String verificationToken) {
        String verificationLink = verificationBaseUrl + "?token=" + verificationToken;

        log.info("[EMAIL] sendVerification called: recipient={}, enabled={}, mailHost={}, mailPort={}",
                recipient, enabled, mailHost, mailPort);

        if (!enabled) {
            log.info("[EMAIL] Email disabled. Verification link for {}: {}", recipient, verificationLink);
            return;
        }

        try {
            log.debug("[EMAIL] Building MimeMessage for recipient={}", recipient);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject("Verify your Quantum Education email");
            String htmlBody = """
                <p>Please verify your email by clicking the link below:</p>
                <p><a href="%s">Verify my email</a></p>
                <p>This link expires in 24 hours.</p>
                <p>If you did not create an account, please ignore this email.</p>
                """.formatted(verificationLink);
            helper.setText(htmlBody, true);

            log.info("[EMAIL] Sending verification email to {} via {}:{}", recipient, mailHost, mailPort);
            mailSender.send(message);
            log.info("[EMAIL] Verification email sent successfully to {}", recipient);
        } catch (MessagingException e) {
            log.error("[EMAIL] Failed to send verification email to {} (MessagingException): {}", recipient, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send verification email to {} ({}): {}", recipient, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }
}
