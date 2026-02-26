package com.quantum.edu.auth.notification;

/**
 * Strategy interface for sending notifications.
 * Implementations handle channel-specific delivery (email, SMS, push, etc.).
 */
public interface NotificationService {

    void sendVerification(String recipient, String verificationToken);
}
