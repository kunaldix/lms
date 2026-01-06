package com.lms.utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to handle all email-related operations for CreditHub.
 * Uses JavaMail API and Gmail SMTP to send OTPs and notifications.
 */
public class EmailUtility {

    // Logger instance for tracking email delivery and failures
    private static final Logger logger = LogManager.getLogger(EmailUtility.class);

    // Configuration for the sender account
    private static final String SENDER_EMAIL = "kunald15049@gmail.com"; 
    private static final String SENDER_PASSWORD = "xplt bjsc uwzr lzki";

    /**
     * Sends a verification email containing an OTP to new users.
     * @param recipientEmail The address to send the code to.
     * @param otp The 6-digit verification code.
     */
    public static void sendVerificationEmail(String recipientEmail, String otp) {
        String subject = "CreditHub - Verify Your Email Address";
        String body = "<h3>Welcome to CreditHub!</h3>"
                    + "<p>Thank you for choosing us. Please use the following code to verify your account:</p>"
                    + "<h2 style='color: #004a99; letter-spacing: 5px;'>" + otp + "</h2>"
                    + "<p>If you did not create an account, please ignore this email.</p>";
        
        logger.info("Initiating verification email to: {}", recipientEmail);
        sendEmail(recipientEmail, subject, body);
    }

    /**
     * Sends a password reset email with a unique OTP.
     * @param recipientEmail The registered email of the user.
     * @param otp The reset code.
     */
    public static void sendPasswordResetEmail(String recipientEmail, String otp) {
        String subject = "CreditHub - Password Reset Request";
        String body = "<h3>Password Reset Request</h3>"
                    + "<p>We received a request to reset your password. Use the code below to proceed:</p>"
                    + "<h2 style='color: #e74c3c; letter-spacing: 5px;'>" + otp + "</h2>"
                    + "<p>This code expires in 10 minutes. If you didn't request this, secure your account immediately.</p>";
        
        logger.info("Initiating password reset email to: {}", recipientEmail);
        sendEmail(recipientEmail, subject, body);
    }

    /**
     * Internal core method to handle SMTP connection and email delivery logic.
     * Removes the use of System.out and replaces e.printStackTrace with Log4j.
     */
    private static void sendEmail(String recipientEmail, String subject, String htmlContent) {
        // SMTP Properties configuration
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Create the session with the Authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            // Set sender name as "CreditHub Security" for professional appearance
            message.setFrom(new InternetAddress(SENDER_EMAIL, "CreditHub Security"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Attempt to send the email
            Transport.send(message);
            
            // Replaced System.out.println with logger.info
            logger.info("SUCCESS: Email sent successfully to [{}]", recipientEmail);

        } catch (Exception e) {
            // Replaced e.printStackTrace() with logger.error to capture the stack trace in log files
            logger.error("FAILURE: Could not send email to [{}]. Error: {}", recipientEmail, e.getMessage(), e);
            throw new RuntimeException("Email delivery failed. Please check server logs for details.");
        }
    }
}