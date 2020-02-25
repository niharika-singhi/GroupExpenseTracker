package com.niharika.android.groupexpensetracker;

import android.os.AsyncTask;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendEmailTask  extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        final String username = "supremeventurestech@gmail.com";
        final String password = "rdikkyya";
        String toRecipient=null,mailMsg=null,subject="Welcome to Team Expenses";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        toRecipient=params[0];
        mailMsg=params[1];

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("supremeventurestech@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(toRecipient));
            message.setSubject(subject);
            message.setText(mailMsg);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
