package com.example.gleb.mailmanager.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gleb.mailmanager.basics.MailStructure;
import com.example.gleb.mailmanager.recyclerview.RVAdapter;
import com.example.gleb.mailmanager.swipe.SuperSwipeRefreshLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;

/**
 * Created by Gleb on 20.10.2015.
 */
abstract class PatternFragment extends Fragment {
    public static final String TAG = "Tag";
    protected RecyclerView rv;
    protected List<MailStructure> mailStructures;
    protected SuperSwipeRefreshLayout swipeRefreshLayout;
    protected ProgressBar progressBar;
    protected TextView textView;
    protected List<String> arrayFrom;
    protected List<String> arrayEmail;
    protected List<String> arraySubject;
    protected List<String> arrayContent;
    protected List<String> arrayDateMail;
    protected List<String> arrayAttachFiles;
    protected String email;
    protected String password;

    /*
    * Create mail file in user root directory for user
    * @param String subject        Subject of mail
    * @param String from           Name of sender of mail
    * @param String emailMail      Email of sender of mail
    * @param String date           Send date of mail
    * @return void
    * */
    protected void createMail(String subject, String from, String emailMail, String content, String date, String typeMail, String rootDirectory){
        if (subject == null){
            subject = "";
        }
        Log.d(TAG, "Subject " + subject + " from " + from + "Content " + content);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + rootDirectory + "/" + typeMail + "/", from + "-" + date + ".txt");
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            //add name of sender in file
            fileWriter.write(from);
            fileWriter.append("\n");
            //add email of sender in file
            fileWriter.write(emailMail);
            fileWriter.append("\n");
            //add date of mail in file
            fileWriter.write(date);
            fileWriter.append("\n");
            //add subject of mail in file
            fileWriter.write(subject);
            fileWriter.append("\n");
            //add content of mail in file
            fileWriter.write(content);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Read mails from user root directory for user
    * @param String email        Name user of root directory
    * @return void
    * */
    protected List<MailStructure> readMail(String rootDirectory, String typeMail){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + rootDirectory + "/" + typeMail + "/";
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();
        String fromMail;
        String emailMail;
        String dateMail;
        String subjectMail;
        String contentMail;
        List<MailStructure> mails = new ArrayList<MailStructure>();
        for (int i = 0; i < file.length; i++) {
            int value = 0;
            fromMail = "";
            emailMail = "";
            subjectMail = "";
            contentMail = "";
            dateMail = "";
            Log.d("Files", "FileName:" + file[i].getName());
            try {
                InputStream instream = new FileInputStream(path + file[i].getName());
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line = "";

                    while (line != null){
                        line = buffreader.readLine();
                        switch (value){
                            case 0:
                                fromMail = line;
                                break;

                            case 1:
                                emailMail = line;
                                break;

                            case 2:
                                dateMail = line;
                                break;

                            case 3:
                                subjectMail = line;
                                break;

                            default:
                                contentMail += line;
                                break;
                        }
                        value += 1;
                    }
                    mails.add(new MailStructure(fromMail, emailMail, subjectMail, contentMail, dateMail));
                    instream.close();

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return mails;
    }

    /*
    * Class for async query read mails from root directory
    * */
    public class Reader extends AsyncTask<String, String, String[]> {
        public String typeMail;
        public Context context;

        public Reader(String typeMail, Context context) {
            this.typeMail = typeMail;
            this.context = context;
        }

        @Override
        protected String[] doInBackground(String... params) {
            return null;
        }

        @Override
        protected void onPostExecute(String[] value) {
            mailStructures = new ArrayList<>();
            mailStructures = readMail(email, typeMail);
            RVAdapter adapter = new RVAdapter(mailStructures, context);
            adapter.notifyDataSetChanged();
            rv.setAdapter(adapter);
        }
    }

    protected static boolean createDirIfNotExists(String path, String name) {
        boolean ret = true;
        File file = new File(path, name);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder");
                ret = false;
            }
        }
        return ret;
    }

    /*
    * Delete all files in current directory
    * @param File file        Directory for deleted
    * @return boolean         Is directory deleted
    * */
    protected boolean delete(File file) {
        File[] files = file.listFiles();
        if (files != null)
            for (File f : files) delete(f);
        return file.delete();
    }

    /*
    * Delete directories from root directory for update mail
    * */
    protected void deleteInRootDirectory(){
        delete(new File(Environment.getExternalStorageDirectory() + "/" + email, "Inbox"));
        delete(new File(Environment.getExternalStorageDirectory() + "/" + email, "Отправленные"));
        delete(new File(Environment.getExternalStorageDirectory() + "/" + email, "Удаленные"));
        delete(new File(Environment.getExternalStorageDirectory() + "/" + email, "Черновики"));
    }

    /*
    * Create again directroty that would be deleted after update mail
    * */
    protected void createInRootDirectory(){
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), "Inbox");
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), "Отправленные");
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), "Удаленные");
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), "Черновики");
    }

    /*
    * Update all mails at first delete all directiry in mail box and load then again
    * */
    protected void updateMail(){
        //clear files in root directory
        deleteInRootDirectory();
        //create directories in root directory
        createInRootDirectory();

        String host = email.substring(email.lastIndexOf("@") + 1);
        Log.d(TAG, "Host email " + host);
        switch (host){
            case "yandex.ru":
                new Loader("imap.yandex.ru", email, password, "INBOX", getContext()).execute();
                new Loader("imap.yandex.ru", email, password, "Отправленные", getContext()).execute();
                new Loader("imap.yandex.ru", email, password, "Удаленные", getContext()).execute();
                new Loader("imap.yandex.ru", email, password, "Черновики", getContext()).execute();
                break;

            case "yandex.ua":
                new Loader("imap.yandex.ru", email, password, "INBOX", getContext()).execute();
                new Loader("imap.yandex.ru", email, password, "Отправленные", getContext()).execute();
                new Loader("imap.yandex.ru", email, password, "Удаленные", getContext()).execute();
                new Loader("imap.yandex.ru", email, password, "Черновики", getContext()).execute();
                break;

            case "gmail.com":
                new Loader("imap.googlemail.com", email, password, "INBOX", getContext()).execute();
                new Loader("imap.googlemail.com", email, password, "Отправленные", getContext()).execute();
                new Loader("imap.googlemail.com", email, password, "Отправленные", getContext()).execute();
                new Loader("imap.googlemail.com", email, password, "Удаленные", getContext()).execute();
                new Loader("imap.googlemail.com", email, password, "Черновики", getContext()).execute();
                break;

            case "ukr.net":
                new Loader("imap.ukr.net", email, password, "INBOX", getContext()).execute();
                new Loader("imap.ukr.net", email, password, "Отправленные", getContext()).execute();
                new Loader("imap.ukr.net", email, password, "Отправленные", getContext()).execute();
                new Loader("imap.ukr.net", email, password, "Удаленные", getContext()).execute();
                new Loader("imap.ukr.net", email, password, "Черновики", getContext()).execute();
                break;

            case "rambler.ru":
                new Loader("imap.rambler.ru", email, password, "INBOX", getContext()).execute();
                new Loader("imap.rambler.ru", email, password, "Отправленные", getContext()).execute();
                new Loader("imap.rambler.ru", email, password, "Отправленные", getContext()).execute();
                new Loader("imap.rambler.ru", email, password, "Удаленные", getContext()).execute();
                new Loader("imap.rambler.ru", email, password, "Черновики", getContext()).execute();
                break;
        }
    }

    /*
    * Class for async query load mails from server and write to root directory
    * */
    public class Loader extends AsyncTask<String, String, String[]> {
        public String imapHost;
        public String user;
        public String password;
        public String typeMail;
        public Context context;

        public Loader(String imapHost, String user, String password, String typeMail, Context context) {
            this.imapHost = imapHost;
            this.user = user;
            this.password = password;
            this.typeMail = typeMail;
            this.context = context;
        }

        @Override
        protected String[] doInBackground(String... params) {
            Properties props = new Properties();
            props.put("mail.imap.port", 993);
            props.put("mail.imap.socketFactory.port", 993);
            props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.imap.socketFactory.fallback", "false");
            props.setProperty("mail.store.protocol", "imaps");

            try {
                Session session = Session.getInstance(props, null);
                Store store = session.getStore();
                store.connect(imapHost, email, password);
                Folder folderBox = store.getFolder(typeMail);
                folderBox.open(Folder.READ_ONLY);
                int newMail = folderBox.getNewMessageCount();
                int allMail = folderBox.getMessageCount();
                Log.d(TAG, "New mail " + newMail);
                Log.d(TAG, "All mail " + allMail);

                FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.USER), false);
                Message[] messages = folderBox.search(ft);
                Log.d(TAG, "Новые сообщения " + messages.length);

                for (Folder folder : store.getDefaultFolder().list("*")) {
                    Log.d(TAG, "Новые сообщения " + folder.getFullName());
                }
                arrayFrom = new ArrayList<>();
                arrayEmail = new ArrayList<>();
                arraySubject = new ArrayList<>();
                arrayContent = new ArrayList<>();
                arrayDateMail = new ArrayList<>();

                for (int i = 0; i < messages.length; i++) {
                    Address[] in = messages[i].getFrom();
                    for (Address address : in) {
                        String decodeAddress = MimeUtility.decodeText(address.toString());
                        System.out.println("FROM:" + address.toString());
                        //add email of sender of mail
                        String emailValue = decodeAddress.substring(decodeAddress.indexOf("<"), decodeAddress.indexOf(">") + 1);
                        arrayEmail.add(emailValue);
                        //add name of sender of mail;
                        String nameValue = decodeAddress.substring(0, decodeAddress.indexOf("<"));
                        arrayFrom.add(nameValue);
                    }

                    Object content = messages[i].getContent();
                    String[] parts = messages[i].getSentDate().toString().split(" ");

                    //mail content only string values or mail content images with different string values
                    if (content instanceof String) {
                        String body = (String) content;
                        Log.d(TAG, "SENT DATE String: " + messages[i].getSentDate());
                        //add date of mail
                        arrayDateMail.add(String.valueOf(messages[i].getSentDate().getDate()) + "." + (messages[i].getSentDate().getMonth() + 1) + "."
                                + (messages[i].getSentDate().getYear() % 100) + "-" + parts[3]);
                        //add subject of mail
                        arraySubject.add(messages[i].getSubject());
                        Log.d(TAG, "SUBJECT String: " + messages[i].getSubject());
                        arrayContent.add(body);
                        Log.d(TAG, "Content String " + body);
                        createMail(arraySubject.get(i), arrayFrom.get(i), arrayEmail.get(i), arrayContent.get(i), arrayDateMail.get(i), typeMail, email);
                    } else if (content instanceof Multipart) {
                        Multipart mp = (Multipart) content;
                        BodyPart bp = mp.getBodyPart(0);
                        arrayDateMail.add(String.valueOf(messages[i].getSentDate().getDate()) + "." + (messages[i].getSentDate().getMonth() + 1) + "."
                                + (messages[i].getSentDate().getYear() % 100) + "-" + parts[3]);
                        Log.d(TAG, "SENT DATE Multipart: " + messages[i].getSentDate());
                        arraySubject.add(messages[i].getSubject());
                        Log.d(TAG, "SUBJECT Multipart: " + messages[i].getSubject());
                        arrayContent.add(bp.getContent().toString());
                        Log.d(TAG, "Content Multipart " + bp.getContent());
                        createMail(arraySubject.get(i), arrayFrom.get(i), arrayEmail.get(i), arrayContent.get(i), arrayDateMail.get(i), typeMail, email);
                    }
                }

            } catch (Exception mex) {
                mex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] value) {

        }
    }

    protected void contentEmail(String folder){
        String host = email.substring(email.lastIndexOf("@") + 1);
        Log.d(TAG, "Host email " + host);
        switch (host){
            case "yandex.ru":
                new Reader(folder, getContext()).execute();
                break;

            case "yandex.ua":
                new Reader(folder, getContext()).execute();
                break;

            case "gmail.com":
                new Reader(folder, getContext()).execute();
                break;

            case "ukr.net":
                new Reader(folder, getContext()).execute();
                break;

            case "rambler.ru":
                new Reader(folder, getContext()).execute();
                break;
        }

    }

}
