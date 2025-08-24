package com.allenhouse.hireeasyuser;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GMailSender {
    public static void send(String toEmail, String subject, String message) throws Exception {
        final String fromEmail = "hireeasy1@gmail.com";
        final String password = "your_password";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

        Message mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(fromEmail,"HireEasy"));
        mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        mimeMessage.setSubject(subject);
        mimeMessage.setContent(message, "text/html; charset=utf-8");

        Transport.send(mimeMessage);
    }
}

