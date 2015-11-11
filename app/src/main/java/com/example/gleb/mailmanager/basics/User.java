package com.example.gleb.mailmanager.basics;

import java.io.Serializable;

/**
 * Created by Gleb on 29.10.2015.
 */
public class User implements Serializable {
    private String email;
    private String password;
    private String imapServer;
    private String imapPort;
    private String smtpServer;
    private String smtpPort;
    private int numMails;
    private int offsetMail;

    public User(String email, String password, String imapServer, String imapPort, String smtpServer, String smtpPort, int numMails, int offsetMail) {
        this.email = email;
        this.password = password;
        this.imapServer = imapServer;
        this.imapPort = imapPort;
        this.smtpServer = smtpServer;
        this.smtpPort = smtpPort;
        this.numMails = numMails;
        this.offsetMail = offsetMail;
    }

    public int getNumMails() {
        return numMails;
    }

    public void setNumMails(int numMails) {
        this.numMails = numMails;
    }

    public int getOffsetMail() {
        return offsetMail;
    }

    public void setOffsetMail(int offsetMail) {
        this.offsetMail = offsetMail;
    }

    public String getImapServer() {
        return imapServer;
    }

    public void setImapServer(String imapServer) {
        this.imapServer = imapServer;
    }

    public String getImapPort() {
        return imapPort;
    }

    public void setImapPort(String imapPort) {
        this.imapPort = imapPort;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
