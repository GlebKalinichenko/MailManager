package com.example.gleb.mailmanager.signin;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.gleb.mailmanager.activities.MailManager;
import com.example.gleb.mailmanager.R;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by Gleb on 17.10.2015.
 */
public class SignIn extends AppCompatActivity {
    public static final String TAG = "Tag";
    private FloatingActionButton fab;
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String host = emailEditText.getText().toString().substring(emailEditText.getText().toString().lastIndexOf("@") + 1);

                if (host.equals("yandex.ru") || host.equals("yandex.ua")) {
                    new Loader("imap.yandex.ru", emailEditText.getText().toString(), passwordEditText.getText().toString()).execute();
                }
                else{
                    if (host.equals("gmail.com")) {
                        new Loader("imap.googlemail.com", emailEditText.getText().toString(), passwordEditText.getText().toString()).execute();
                    }
                    else{
                        if (host.equals("ukr.net")) {
                            new Loader("imap.ukr.net", emailEditText.getText().toString(), passwordEditText.getText().toString()).execute();
                        }
                        else{
                            if (host.equals("rambler.ru")) {
                                new Loader("imap.rambler.ru", emailEditText.getText().toString(), passwordEditText.getText().toString()).execute();
                            }
                        }
                    }
                }
            }
        });
    }

    public class Loader extends AsyncTask<String, String, String[]> {
        private String imapHost;
        private String user;
        private String password;
        private int newMail;
        private int allMail;
        private int deletedMail;
        private int outBoxMail;
        private int draftMail;

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
                store.connect(imapHost, user, password);
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                newMail = inbox.getNewMessageCount();
                allMail = inbox.getMessageCount();
                Folder deleted = store.getFolder("Удаленные");
                deletedMail = deleted.getMessageCount();
                Folder outBox = store.getFolder("Отправленные");
                outBoxMail = outBox.getMessageCount();
                Folder draft = store.getFolder("Черновики");
                draftMail = draft.getMessageCount();
            } catch (Exception mex) {
                mex.printStackTrace();
                Toast.makeText(SignIn.this, "Ошибка при подключении", Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] value) {
            Intent intent = new Intent(SignIn.this, MailManager.class);
            intent.putExtra(MailManager.EMAIL, emailEditText.getText().toString());
            intent.putExtra(MailManager.PASSWORD, passwordEditText.getText().toString());
            intent.putExtra(MailManager.NEW_INBOXMAIL, newMail);
            intent.putExtra(MailManager.ALL_INBOXMAIL,  allMail);
            intent.putExtra(MailManager.DELETEDMAIL,  deletedMail);
            intent.putExtra(MailManager.OUTBOXMAIL, outBoxMail);
            intent.putExtra(MailManager.DRAFTMAIL, draftMail);
            startActivity(intent);
        }
    }
}
