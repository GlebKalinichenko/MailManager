package com.example.gleb.mailmanager;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


/**
 * Created by gleb on 17.07.15.
 */
public class Mail extends javax.mail.Authenticator {
    public static final String TAG = "TAG";
    private String user;
    private String password;

    private String[] to;
    private String from;

    private String port;
    private String sport;

    private String host;
    private String subject;
    private String body;

    private boolean auth;
    private Multipart multipart;

    public Mail() {
        host = "smtp.yandex.ru"; // default smtp server
        port = "465"; // default smtp port
        sport = "465"; // default socketfactory port

        user = "";
        password = "";
        from = ""; // email sent from
        subject = ""; // email subject
        body = ""; // email body

        auth = true; // smtp authentication - default on
        multipart = new MimeMultipart();

        // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added.
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public Mail(String user, String password) {
        this();

        this.user = user;
        this.password = password;
    }

    public Properties setProperties(){
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);

        if(auth) {
            properties.put("mail.smtp.auth", "true");
        }

        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.socketFactory.port", sport);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");

        return properties;
    }

    public boolean send(){
        new Sender().execute();
        return true;

    }

    public class Sender extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {
            Properties props = setProperties();

            if(!user.equals("") && !password.equals("") && to.length > 0 && !from.equals("") && !subject.equals("") && !body.equals("")) {
                Session session = Session.getInstance(props, Mail.this);

                MimeMessage msg = new MimeMessage(session);
                try {
                    msg.setFrom(new InternetAddress(from));

                InternetAddress[] addressTo = new InternetAddress[to.length];
                for (int i = 0; i < to.length; i++) {
                    addressTo[i] = new InternetAddress(to[i]);
                }
                msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

                msg.setSubject(subject);
                msg.setSentDate(new Date());
                Log.d(TAG, "Body " + body);
                // setup message body
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(body);
                multipart.addBodyPart(messageBodyPart);

                // Put parts in message
                msg.setContent(multipart);

                // send email
                Transport.send(msg);

                } catch (MessagingException e) {
                    e.printStackTrace();
                }

                return body;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    public void addAttachment(String filename) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        multipart.addBodyPart(messageBodyPart);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String[] getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getPort() {
        return port;
    }

    public String getSport() {
        return sport;
    }

    public String getHost() {
        return host;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public boolean isAuth() {
        return auth;
    }

    public Multipart getMultipart() {
        return multipart;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTo(String[] to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public void setMultipart(Multipart multipart) {
        this.multipart = multipart;
    }
}
