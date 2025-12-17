package com.lms.utils;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtility {

    // 1. GMAIL CONFIGURATION
    // CRITICAL: For Gmail, you MUST generate an "App Password".
    // Do NOT use your real Gmail login password.
    private static final String SENDER_EMAIL = "kunald15049@gmail.com"; 
    private static final String SENDER_PASSWORD = "xplt bjsc uwzr lzki";

    public static void sendOtpEmail(String recipientEmail, String otp) {
        
        // 2. Setup Mail Server Properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // 3. Create Session with Authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            // 4. Create the Email Message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("CreditHub - Password Reset OTP");
            
            // HTML Content for a nicer look
            String htmlContent = "<h3>CreditHub Password Reset</h3>"
                    + "<p>Your One-Time Password (OTP) is:</p>"
                    + "<h2 style='color: #004a99;'>" + otp + "</h2>"
                    + "<p>This code expires in 10 minutes.</p>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // 5. Send Email
            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}
