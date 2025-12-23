package com.lms.utils;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailUtility {

    private static final String SENDER_EMAIL = "kunald15049@gmail.com"; 
    private static final String SENDER_PASSWORD = "xplt bjsc uwzr lzki";

    // Method for Email Verification (Sign-up)
    public static void sendVerificationEmail(String recipientEmail, String otp) {
        String subject = "CreditHub - Verify Your Email Address";
        String body = "<h3>Welcome to CreditHub!</h3>"
                    + "<p>Thank you for choosing us. Please use the following code to verify your account:</p>"
                    + "<h2 style='color: #004a99; letter-spacing: 5px;'>" + otp + "</h2>"
                    + "<p>If you did not create an account, please ignore this email.</p>";
        
        sendEmail(recipientEmail, subject, body);
    }

    // Method for Password Reset
    public static void sendPasswordResetEmail(String recipientEmail, String otp) {
        String subject = "CreditHub - Password Reset Request";
        String body = "<h3>Password Reset Request</h3>"
                    + "<p>We received a request to reset your password. Use the code below to proceed:</p>"
                    + "<h2 style='color: #e74c3c; letter-spacing: 5px;'>" + otp + "</h2>"
                    + "<p>This code expires in 10 minutes. If you didn't request this, secure your account immediately.</p>";
        
        sendEmail(recipientEmail, subject, body);
    }

    // Private core method to handle the actual sending logic
    private static void sendEmail(String recipientEmail, String subject, String htmlContent) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "CreditHub Security"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("Email successfully sent to: " + recipientEmail);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Email delivery failed.");
        }
    }
}