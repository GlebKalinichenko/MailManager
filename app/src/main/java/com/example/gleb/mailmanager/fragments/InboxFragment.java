package com.example.gleb.mailmanager.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gleb.mailmanager.recyclerview.ItemRecycler;
import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.recyclerview.RVAdapter;
import com.example.gleb.mailmanager.swipe.SuperSwipeRefreshLayout;
import com.example.gleb.mailmanager.basics.MailStructure;

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
import javax.mail.search.FlagTerm;

/**
 * Created by Gleb on 18.10.2015.
 */
public class InboxFragment extends Fragment {
    public static final String TAG = "TAG";
    private RecyclerView rv;
    private List<MailStructure> mailStructures;
    private SuperSwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textView;
    private List<String> arrayFrom;
    private List<String> arrayEmail;
    private List<String> arraySubject;
    private List<String> arrayContent;
    private List<String> arrayDateMail;
    private String email;
    private String password;

    public InboxFragment(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_mail_manager, container, false);
        rv=(RecyclerView) v.findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

//        initializeData();
//        RVAdapter adapter = new RVAdapter(mailStructures);
//        rv.setAdapter(adapter);

        rv.addOnItemTouchListener(
                new ItemRecycler.RecyclerItemClickListener(getActivity(), new ItemRecycler.RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d(TAG, "Value" + position);
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                })
        );
        swipeRefreshLayout = (SuperSwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        View child = LayoutInflater.from(swipeRefreshLayout.getContext()).inflate(R.layout.layout_head, null);
        progressBar = (ProgressBar) child.findViewById(R.id.progressBar);
        textView = (TextView) child.findViewById(R.id.text_view);
        swipeRefreshLayout
                .setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {

                    @Override
                    public void onRefresh() {
                        textView.setText("К вам летит письмо...");
                        progressBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                                progressBar.setVisibility(View.GONE);
                            }
                        }, 2000);

                    }

                    @Override
                    public void onPullDistance(int distance) {
                        //myAdapter.updateHeaderHeight(distance);
                    }

                    @Override
                    public void onPullEnable(boolean enable) {
//                        imageView.setVisibility(View.VISIBLE);
//                        imageView.setRotation(enable ? 180 : 0);
                    }
                });

        String host = email.substring(email.lastIndexOf("@") + 1);
        Log.d(TAG, "Host email " + host);
        switch (host){
            case "yandex.ru":
                new Loader("imap.yandex.ru", email, password).execute();
            break;

            case "yandex.ua":
                new Loader("imap.yandex.ru", email, password).execute();
            break;

            case "gmail.com":
                new Loader("imap.googlemail.com", email, password).execute();
            break;

            case "ukr.net":
                new Loader("imap.ukr.net", email, password).execute();
            break;

            case "rambler.ru":
                new Loader("imap.rambler.ru", email, password).execute();
            break;
        }

        return v;
    }

    /*
    * Class for async query for server for mails
    * */
    public class Loader extends AsyncTask<String, String, String[]> {
        public String imapHost;
        public String user;
        public String password;

        public Loader(String imapHost, String user, String password) {
            this.imapHost = imapHost;
            this.user = user;
            this.password = password;
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
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                int newMail = inbox.getNewMessageCount();
                int allMail = inbox.getMessageCount();
                Log.d(TAG, "New mail " + newMail);
                Log.d(TAG, "All mail " + allMail);

                FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.USER), false);
                Message[] messages = inbox.search(ft);
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
                        System.out.println("FROM:" + address.toString());
                        //add email of sender of mail
                        String emailValue = address.toString().substring(address.toString().indexOf("<"), address.toString().indexOf(">") + 1);
                        arrayEmail.add(emailValue);
                        //add name of sender of mail;
                        String nameValue = address.toString().substring(0, address.toString().indexOf("<"));
                        arrayFrom.add(nameValue);
                    }

                    Object content = messages[i].getContent();

                    //mail content only string values or mail content images with different string values
                    if (content instanceof String) {
                        String body = (String) content;
                        Log.d(TAG, "SENT DATE String: " + messages[i].getSentDate());
                        //add date of mail
                        arrayDateMail.add(String.valueOf(messages[i].getSentDate().getDate()) + "." + (messages[i].getSentDate().getMonth() + 1) + "." + (messages[i].getSentDate().getYear() % 100));
                        //add subject of mail
                        arraySubject.add(messages[i].getSubject());
                        Log.d(TAG, "SUBJECT String: " + messages[i].getSubject());
                        arrayContent.add(body);
                        Log.d(TAG, "Content String " + body);
                        createMail(arraySubject.get(i), arrayFrom.get(i), arrayEmail.get(i), arrayContent.get(i), arrayDateMail.get(i));
                    } else if (content instanceof Multipart) {
                        Multipart mp = (Multipart) content;
                        BodyPart bp = mp.getBodyPart(0);
                        arrayDateMail.add(String.valueOf(messages[i].getSentDate().getDate()) + "." + (messages[i].getSentDate().getMonth() + 1) + "." + (messages[i].getSentDate().getYear() % 100));
                        Log.d(TAG, "SENT DATE Multipart: " + messages[i].getSentDate());
                        arraySubject.add(messages[i].getSubject());
                        Log.d(TAG, "SUBJECT Multipart: " + messages[i].getSubject());
                        arrayContent.add(bp.getContent().toString());
                        Log.d(TAG, "Content Multipart " + bp.getContent());
                        createMail(arraySubject.get(i), arrayFrom.get(i), arrayEmail.get(i), arrayContent.get(i), arrayDateMail.get(i));
                    }
                }

            } catch (Exception mex) {
                mex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] value) {
            mailStructures = new ArrayList<>();
            mailStructures = readMail(email);
//            for (int i = 0; i < arrayFrom.size(); i++){
//                mailStructures.add(new MailStructure(arrayFrom.get(i), arrayEmail.get(i), arraySubject.get(i), arrayContent.get(i), arrayDateMail.get(i)));
//            }
            RVAdapter adapter = new RVAdapter(mailStructures);
            rv.setAdapter(adapter);
        }
    }

    /*
    * Create mail file in user root directory for user
    * @param String subject        Subject of mail
    * @param String from           Name of sender of mail
    * @param String emailMail      Email of sender of mail
    * @param String date           Send date of mail
    * @return void
    * */
    public void createMail(String subject, String from, String emailMail, String content, String date){
        if (subject == null){
            subject = "";
        }
        Log.d(TAG, "Subject " + subject + " from " + from + "Content " + content);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/Inbox/", from + "-" + date + ".txt");
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
    public List<MailStructure> readMail(String email){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/Inbox/";
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
                InputStream instream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/Inbox/" + file[i].getName());
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
}