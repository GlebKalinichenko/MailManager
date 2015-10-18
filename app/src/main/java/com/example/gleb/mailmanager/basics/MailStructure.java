package com.example.gleb.mailmanager.basics;

/**
 * Created by Gleb on 18.10.2015.
 */
public class MailStructure {
    private String from;
    private String email;
    private String subject;
    private String content;
    private String date;

    public MailStructure(String from, String email, String subject, String content, String date) {
        this.from = from;
        this.email = email;
        this.subject = subject;
        this.content = content;
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
