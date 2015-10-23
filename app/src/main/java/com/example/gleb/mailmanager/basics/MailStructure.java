package com.example.gleb.mailmanager.basics;

import android.os.Parcelable;

import java.io.Serializable;


/**
 * Created by Gleb on 18.10.2015.
 */
public class MailStructure implements Serializable {
    private String from;
    private String email;
    private String subject;
    private String content;
    private String date;
    private String[] attachFiles;

    public MailStructure(String from, String email, String subject, String content, String date, String[] attachFiles) {
        this.from = from;
        this.email = email;
        this.subject = subject;
        this.content = content;
        this.date = date;
        this.attachFiles = attachFiles;
    }

    public String[] getAttachFiles() {
        return attachFiles;
    }

    public void setAttachFiles(String[] attachFiles) {
        this.attachFiles = attachFiles;
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
