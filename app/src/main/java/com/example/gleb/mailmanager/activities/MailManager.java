package com.example.gleb.mailmanager.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.basics.User;
import com.example.gleb.mailmanager.images.RoundImage;
import com.example.gleb.mailmanager.mailutils.Mail;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerItem;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerListAdapter;
import com.example.gleb.mailmanager.signin.SignIn;
import com.example.gleb.mailmanager.viewpager.ProfileViewPagerAdapter;
import com.example.gleb.mailmanager.sliding.SlidingTabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;

public class MailManager extends PatternActivity {
    public static final String TAG = "Tag";
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";
    public static final String IMAP_SERVER = "Imap server";
    public static final String IMAP_PORT = "Imap port";
    public static final String SMTP_SERVER = "Smtp server";
    public static final String SMTP_PORT = "Smtp port";
    public static final String NEW_INBOXMAIL = "New mail";
    public static final String ALL_INBOXMAIL = "All mail";
    public static final String DELETEDMAIL = "Deleted mail";
    public static final String OUTBOXMAIL = "Outbox mail";
    public static final String DRAFTMAIL = "Draft mail";
    public static final String LANGFOLDER = "Lang folder";
    public static final String SAVE_USERS = "Save users";
    public static final String IS_CHANGE = "Change";
    public static final String NUM_MAILS = "Num mails";
    public static final String OFFSET_MAIL = "Offset mail";
    public static final String MAIL_MANAGER = "MailManager";
    private ImageView userImageView;
    private String email;
    private String password;
    private String imapServer;
    private String imapPort;
    private String smtpServer;
    private String smtpPort;
    private String uniqId;
    private int newInboxMail;
    private int allInboxMail;
    private int deletedMail;
    private int outBoxMail;
    private int draftMail;
//    private int Numboftabs = 4;
    private ViewPager pager;
    private SlidingTabLayout tabs;
    private ActionMode actionMode;
    private FragmentStatePagerAdapter viewadapter;
//    private CharSequence[] Titles = {
//            "Входящие", "Отправленные", "Черновики", "Удаленные"
//    };
    private List<String> arrayFrom;
    private List<String> arrayEmail;
    private List<String> arraySubject;
    private List<String> arrayContent;
    private List<String> arrayDateMail;
    private List<String> nameFolders;
    private int offsetMail;
    private List<User> users;
    private SharedPreferences sPref;
    private int isChange;
    private int numMails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadAccounts();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MailManager.this, SignIn.class);
                startActivity(intent);
            }
        });

        initializeValues();
        createBaseFolder();

        mTitle = mDrawerTitle = getTitle();
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        //initialize navigation drawer
        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true, String.valueOf(newInboxMail) + "/" + String.valueOf(allInboxMail)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), true, String.valueOf(outBoxMail)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), true, String.valueOf(draftMail)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, String.valueOf(deletedMail)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1)));
        navMenuIcons.recycle();
        // setting the nav drawer list adapter
        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);

        //settings for header of drawer
        LayoutInflater inflater = getLayoutInflater();
        View listHeaderView = inflater.inflate(R.layout.drawer_header, null, false);
        userImageView = (ImageView) listHeaderView.findViewById(R.id.imageView);
        TextView emailHeader = (TextView) listHeaderView.findViewById(R.id.emailHeader);
        TextView passwordHeader = (TextView) listHeaderView.findViewById(R.id.passwordHeader);
        emailHeader.setText(email);
        passwordHeader.setText(password);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.user);
        RoundImage roundedImage = new RoundImage(bm);
        userImageView.setImageDrawable(roundedImage);
        listHeaderView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
        mDrawerList.addHeaderView(listHeaderView);

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(this);

        // enabling action bar app icon and behaving it as toggle button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //first enter to account of mail
        if (users == null) {
            users = new ArrayList<>();
        }

        //change between accounts
        if (isChange == 0) {
            User user = new User(email, password, imapServer, imapPort, smtpServer, smtpPort, numMails, offsetMail);
            users.add(user);
        }

        saveAccounts();

        nameFolders = new ArrayList<>();
        if (isChange == 0) {
            new Loader(imapServer, email, password, this, imapPort, offsetMail, numMails).execute();
        }
        else{
            File file = new File(String.valueOf(Environment.getExternalStorageDirectory()), email);
            String[] names = file.list();
            nameFolders = new ArrayList<>();
            CharSequence[] titles = new CharSequence[names.length - 2];
            for (int i = 0; i < names.length; i++){
                if (!names[i].equals("Attach") && !names[i].equals("Keys")){
                    nameFolders.add(names[i]);
                    titles[i - 2] = names[i];
                }
            }

            int numTabs = nameFolders.size();

            viewadapter =  new ProfileViewPagerAdapter(getSupportFragmentManager(), titles, numTabs,
                    email, password, imapServer, imapPort, nameFolders, offsetMail, numMails,
                    smtpServer, smtpPort);

            // Assigning ViewPager View and setting the adapter
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(viewadapter);

            // Assiging the Sliding Tab Layout View
            tabs = (SlidingTabLayout) findViewById(R.id.tabs);
            tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

            // Setting Custom Color for the Scroll bar indicator of the Tab View
            tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.colorPrimary);
                }
            });

            // Setting the ViewPager For the SlidingTabsLayout
            tabs.setViewPager(pager);
        }
    }

    /*
    * Initialize variables for show content about mails of user
    * @param void
    * @return void
    * */
    private void initializeValues(){
        email = getIntent().getStringExtra(MailManager.EMAIL);
        password = getIntent().getStringExtra(MailManager.PASSWORD);
        imapServer = getIntent().getStringExtra(MailManager.IMAP_SERVER);
        imapPort = getIntent().getStringExtra(MailManager.IMAP_PORT);
        smtpServer = getIntent().getStringExtra(MailManager.SMTP_SERVER);
        smtpPort = getIntent().getStringExtra(MailManager.SMTP_PORT);
        uniqId = getIntent().getStringExtra(MailManager.LANGFOLDER);
        newInboxMail = getIntent().getIntExtra(MailManager.NEW_INBOXMAIL, 0);
        allInboxMail = getIntent().getIntExtra(MailManager.ALL_INBOXMAIL, 0);
        deletedMail = getIntent().getIntExtra(MailManager.DELETEDMAIL, 0);
        outBoxMail = getIntent().getIntExtra(MailManager.OUTBOXMAIL, 0);
        draftMail = getIntent().getIntExtra(MailManager.DRAFTMAIL, 0);
        isChange = getIntent().getIntExtra(MailManager.IS_CHANGE, 0);
        numMails = getIntent().getIntExtra(MailManager.NUM_MAILS, 0);
        offsetMail = getIntent().getIntExtra(MailManager.OFFSET_MAIL, 1);
    }

    /*
    * Load accounts for show mails
    * @param void
    * @return void
    * */
    private void loadAccounts(){
        sPref = getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(SAVE_USERS, "");
        users = new Gson().fromJson(savedText, new TypeToken<List<User>>() {
        }.getType());
    }

    /*
    * Save accounts for show mails
    * @param void
    * @return void
    * */
    private void saveAccounts(){
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVE_USERS, new Gson().toJson(users));
        ed.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify headerAttach parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            sPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();

            for (int i = 0; i < users.size(); i++){
                if (users.get(i).getEmail().equals(email)){
                    users.remove(i);
                }
            }

            ed.putString(SAVE_USERS, new Gson().toJson(users));
            ed.commit();
            delete(new File(Environment.getExternalStorageDirectory(), email));
            finish();
            return true;
        }

        //load new mails from server
        if (id == R.id.action_update) {
            nameFolders.clear();
            nameFolders = new ArrayList<>();
            tabs = null;
            viewadapter = null;
            offsetMail++;
            new Loader(imapServer, email, password, this, imapPort, offsetMail, numMails).execute();
            return true;
        }

        //generate asymetric public and private key
        if (id == R.id.know_key) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            View view = (LinearLayout) getLayoutInflater().inflate(R.layout.send_key, null);
            final EditText toEmailEditText = (EditText) view.findViewById(R.id.editText);
            builder.setView(view);
            builder.setTitle("Запрос на получение ключа");
            builder.setMessage("Email получателя");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String toEmail = toEmailEditText.getText().toString();
                    String[] toArr = new String[]{toEmail};
                    Mail m = new Mail(email, password, smtpServer, smtpPort, imapServer, imapPort);
                    m.setTo(toArr);
                    m.setFrom(email); // who is sending the email
                    m.setSubject("Запрос на генерацию ключа для шифрования");
                    m.setBody("Сгенерируйте ключ для шифрования письма и отправьте мне");
                    try {
                        if (m.send()) {
                            // success
                            Toast.makeText(MailManager.this, "Письмо было отправлено.", Toast.LENGTH_LONG).show();
                        } else {
                            // failure
                            Toast.makeText(MailManager.this, "Произошел сбой при отправке письма.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDrawerList.setItemChecked(position, true);
        Log.d(TAG, "Value" + position);
        Intent intent;
        switch (position){
            case 5:
                intent = new Intent(MailManager.this, SenderMail.class);
                intent.putExtra(SenderMail.EMAIL, email);
                intent.putExtra(SenderMail.PASSWORD, password);
                intent.putExtra(SenderMail.SMTP_SERVER, smtpServer);
                intent.putExtra(SenderMail.SMTP_PORT, smtpPort);
                intent.putExtra(SenderMail.NEW_INBOXMAIL, newInboxMail);
                intent.putExtra(SenderMail.IMAP_SERVER, imapServer);
                intent.putExtra(SenderMail.IMAP_PORT, imapPort);
                startActivity(intent);
                break;

            case 6:
                intent = new Intent(MailManager.this, Accounts.class);
                intent.putExtra(Accounts.ACCOUNTS, new Gson().toJson(users));
                intent.putExtra(Accounts.EMAIL, email);
                intent.putExtra(Accounts.PASSWORD, password);
                startActivity(intent);
                break;
        }
    }

    /*
    * create base folders for account
    * MailManager
    *   email + Attach
    *   email + Keys
    * @param void
    * @return void
    * */
    public void createBaseFolder(){
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory()), MAIL_MANAGER);
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory()) + "/" + MAIL_MANAGER,  email);
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + MAIL_MANAGER + "/" + email), "Attach");
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory()+ "/" + MAIL_MANAGER  + "/" + email), "Keys");
    }

    public static boolean createDirIfNotExists(String path, String name) {
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
    * Class for async query load mails from server and write to root directory
    * @param String imapHost        Name of imap server
    * @param String imapPort        Port of imap server
    * @param int numMails           Num mails for load
    * @param int offsetMail         Offset for mail load from server
    * */
    public class Loader extends AsyncTask<String, String, String[]> {
        private String imapHost;
        private String password;
        private String imapPort;
        private int offsetMail;
        private int numMails;

        public Loader(String imapHost, String user, String password, Context context, String imapPort, int offsetMail, int numMails) {
            this.imapHost = imapHost;
            this.password = password;
            this.imapPort = imapPort;
            this.offsetMail = offsetMail;
            this.numMails = numMails;
        }

        @Override
        protected String[] doInBackground(String... params) {
            Properties props = new Properties();
            props.put("mail.imap.port", imapPort);
            props.put("mail.imap.socketFactory.port", imapPort);
            props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.imap.socketFactory.fallback", "false");
            props.setProperty("mail.store.protocol", "imaps");

            try {
                Session session = Session.getInstance(props, null);
                Store store = session.getStore();
                store.connect(imapHost, email, password);

                Folder[] folders = store.getDefaultFolder().list();
                for (Folder fd : folders) {
                    if (isGmailFolder(fd.getName())){
                        Log.d(TAG, fd.getName() + "/All Mail");
                        parseMailFromServer(store, fd.getName() + "/", "All Mail", offsetMail, numMails);
                        parseMailFromServer(store, fd.getName() + "/", "Drafts", offsetMail, numMails);
                        parseMailFromServer(store, fd.getName() + "/", "Sent Mail", offsetMail, numMails);
                        parseMailFromServer(store, fd.getName() + "/", "Spam", offsetMail, numMails);
                        parseMailFromServer(store, fd.getName() + "/", "Starred", offsetMail, numMails);
                        parseMailFromServer(store, fd.getName() + "/", "Trash", offsetMail, numMails);
                    }
                    else {
                        parseMailFromServer(store, fd.getName(), offsetMail, numMails);
                    }
                }
            } catch (Exception mex) {
                mex.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] value) {
            //header of tabs
            CharSequence[] titles = new CharSequence[nameFolders.size()];
            for (int i = 0; i < nameFolders.size(); i++){
                titles[i] = nameFolders.get(i);
            }

            int numTabs = nameFolders.size();

            viewadapter =  new ProfileViewPagerAdapter(getSupportFragmentManager(), titles, numTabs,
                    email, password, imapServer, imapPort, nameFolders, offsetMail, numMails,
                    smtpServer, smtpPort);

            // Assigning ViewPager View and setting the adapter
            pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(viewadapter);

            // Assiging the Sliding Tab Layout View
            tabs = (SlidingTabLayout) findViewById(R.id.tabs);
            tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

            // Setting Custom Color for the Scroll bar indicator of the Tab View
            tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
                @Override
                public int getIndicatorColor(int position) {
                    return getResources().getColor(R.color.colorPrimary);
                }
            });

            // Setting the ViewPager For the SlidingTabsLayout
            tabs.setViewPager(pager);
        }
    }

    /*
    * Is folder gmail
    * @param String folder    Name folder for check
    * @return void
    * */
    private boolean isGmailFolder(String folder){
        if (folder.equals("[Gmail]")){
            return true;
        }
        return false;
    }

    /*
    * Get mails from server to root directory
    * @param Store store        Store of settings
    * @param String nameFolder  Folder for save mails in root directory
    * @return void
    * */
    private void parseMailFromServer(Store store, String nameFolder, int offsetMail, int numMails) throws MessagingException, IOException {
        nameFolders.add(nameFolder);
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + MAIL_MANAGER + "/" + email), nameFolder);
        Folder folderBox = store.getFolder(nameFolder);
        folderBox.open(Folder.READ_ONLY);

        int newMail = folderBox.getNewMessageCount();
        int allMail = folderBox.getMessageCount();
        Log.d(TAG, "New mail " + newMail);
        Log.d(TAG, "All mail " + allMail);

        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.USER), false);
        Message[] messages = folderBox.search(ft);
        messages = reverseMessageOrder(messages);
        Log.d(TAG, "Новые сообщения " + messages.length);

        for (Folder folder : store.getDefaultFolder().list("*")) {
            Log.d(TAG, "Новые сообщения " + folder.getFullName());
        }
        arrayFrom = new ArrayList<>();
        arrayEmail = new ArrayList<>();
        arraySubject = new ArrayList<>();
        arrayContent = new ArrayList<>();
        arrayDateMail = new ArrayList<>();

        if (messages.length != 0) {
            for (int i = 0; i < numMails * offsetMail; i++) {
                try{
                    if (messages[i] != null) {
                        Address[] in = messages[i].getFrom();
                        for (Address address : in) {
                            String decodeAddress = MimeUtility.decodeText(address.toString());
                            System.out.println("FROM:" + address.toString());
                            if (decodeAddress.indexOf("<") == -1 && decodeAddress.indexOf(">") == -1) {
                                arrayEmail.add(decodeAddress);
                                arrayFrom.add(decodeAddress);
                            } else {
                                //add email of sender of mail
                                String emailValue = decodeAddress.substring(decodeAddress.indexOf("<"), decodeAddress.indexOf(">") + 1);
                                arrayEmail.add(emailValue);
                                //add name of sender of mail;
                                String nameValue = decodeAddress.substring(0, decodeAddress.indexOf("<"));
                                 arrayFrom.add(nameValue);
                            }
                        }

                        Object content = new MimeMessage((MimeMessage) messages[i]).getContent();
                        //content = messages[i].getContent();
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
                            createMail(arraySubject.get(i), arrayFrom.get(i), arrayEmail.get(i), arrayContent.get(i), arrayDateMail.get(i), nameFolder, email, new ArrayList<String>());
                        } else if (content instanceof Multipart) {
                            Multipart mp = (Multipart) content;
                            BodyPart bp = mp.getBodyPart(0);
                            arrayDateMail.add(String.valueOf(messages[i].getSentDate().getDate()) + "." + (messages[i].getSentDate().getMonth() + 1) + "."
                                    + (messages[i].getSentDate().getYear() % 100) + "-" + parts[3]);
                            Log.d(TAG, "SENT DATE Multipart: " + messages[i].getSentDate());
                            arraySubject.add(messages[i].getSubject());
                            Log.d(TAG, "SUBJECT Multipart: " + messages[i].getSubject());
                            arrayContent.add(bp.getContent().toString());

                            System.out.println("-------" + (i + 1) + "-------");
                            System.out.println(messages[i].getSentDate());
                            Multipart multipart = (Multipart) content;
                            List<String> arrayPathAttachFiles = new ArrayList<>();

                            for (int j = 0; j < multipart.getCount(); j++) {
                                BodyPart bodyPart = multipart.getBodyPart(j);
                                Log.d(TAG, "bodyPart " + bodyPart.getFileName());
                                Log.d(TAG, "BodyPart " + bodyPart);
                                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                                    System.out.println("Creating file with name without : " + bodyPart.getFileName());
                                    arrayPathAttachFiles.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + "Attach" + "/" + bodyPart.getFileName() +
                                            "-" + arrayDateMail.get(i));
                                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + "Attach" + "/", bodyPart.getFileName() +
                                            "-" + arrayDateMail.get(i));
                                    InputStream is = bodyPart.getInputStream();
                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(f);
                                        byte[] buf = new byte[4096];
                                        int bytesRead;
                                        while ((bytesRead = is.read(buf)) != -1) {
                                            fos.write(buf, 0, bytesRead);
                                        }
                                        fos.close();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            createMail(arraySubject.get(i), arrayFrom.get(i), arrayEmail.get(i), arrayContent.get(i), arrayDateMail.get(i), nameFolder, email, arrayPathAttachFiles);
                        }
                    }
                }
                catch(Exception e){
                    continue;
                }
            }
        }

    }

    /*
    * Get mails from server to root directory
    * @param Store store        Store of settings
    * @param String gmail       Folder of gmail f.e. [Gmail]
    * @param String nameFolder  Folder for save mails in root directory
    * @return void
    * */
    private void parseMailFromServer(Store store, String gmail, String nameFolder, int offsetMail, int numMails) throws MessagingException, IOException {
        nameFolders.add(nameFolder);
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + MAIL_MANAGER + "/" + email), nameFolder);
        Folder folderBox = store.getFolder(gmail + nameFolder);
        folderBox.open(Folder.READ_ONLY);

        int newMail = folderBox.getNewMessageCount();
        int allMail = folderBox.getMessageCount();
        Log.d(TAG, "New mail " + newMail);
        Log.d(TAG, "All mail " + allMail);

        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.USER), false);
        Message[] messages = folderBox.search(ft);
        messages = reverseMessageOrder(messages);
        Log.d(TAG, "Новые сообщения " + messages.length);

        for (Folder folder : store.getDefaultFolder().list("*")) {
            Log.d(TAG, "Новые сообщения " + folder.getFullName());
        }
        arrayFrom = new ArrayList<>();
        arrayEmail = new ArrayList<>();
        arraySubject = new ArrayList<>();
        arrayContent = new ArrayList<>();
        arrayDateMail = new ArrayList<>();

        if (messages.length != 0) {
            for (int i = 0; i < numMails * offsetMail; i++) {
                try {
                    if (messages[i] != null) {
                        Address[] in = messages[i].getFrom();
                        for (Address address : in) {
                            String decodeAddress = MimeUtility.decodeText(address.toString());
                            System.out.println("FROM:" + address.toString());
                            if (decodeAddress.indexOf("<") == -1 && decodeAddress.indexOf(">") == -1) {
                                arrayEmail.add(decodeAddress);
                                arrayFrom.add(decodeAddress);
                            } else {
                                //add email of sender of mail
                                String emailValue = decodeAddress.substring(decodeAddress.indexOf("<"), decodeAddress.indexOf(">") + 1);
                                arrayEmail.add(emailValue);
                                //add name of sender of mail;
                                String nameValue = decodeAddress.substring(0, decodeAddress.indexOf("<"));
                                arrayFrom.add(nameValue);
                            }
                        }

                        Object content = new MimeMessage((MimeMessage) messages[i]).getContent();
                        //content = messages[i].getContent();
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
                            createMail(arraySubject.get(i), arrayFrom.get(i), arrayEmail.get(i), arrayContent.get(i), arrayDateMail.get(i), nameFolder, email, new ArrayList<String>());
                        } else if (content instanceof Multipart) {
                            Multipart mp = (Multipart) content;
                            BodyPart bp = mp.getBodyPart(0);
                            arrayDateMail.add(String.valueOf(messages[i].getSentDate().getDate()) + "." + (messages[i].getSentDate().getMonth() + 1) + "."
                                    + (messages[i].getSentDate().getYear() % 100) + "-" + parts[3]);
                            Log.d(TAG, "SENT DATE Multipart: " + messages[i].getSentDate());
                            arraySubject.add(messages[i].getSubject());
                            Log.d(TAG, "SUBJECT Multipart: " + messages[i].getSubject());
                            arrayContent.add(bp.getContent().toString());

                            System.out.println("-------" + (i + 1) + "-------");
                            System.out.println(messages[i].getSentDate());
                            Multipart multipart = (Multipart) content;
                            List<String> arrayPathAttachFiles = new ArrayList<>();

                            for (int j = 0; j < multipart.getCount(); j++) {
                                BodyPart bodyPart = multipart.getBodyPart(j);
                                Log.d(TAG, "bodyPart " + bodyPart.getFileName());
                                Log.d(TAG, "BodyPart " + bodyPart);
                                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                                    System.out.println("Creating file with name without : " + bodyPart.getFileName());
                                    arrayPathAttachFiles.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + "Attach" + "/" + bodyPart.getFileName() +
                                            "-" + arrayDateMail.get(i));
                                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + "Attach" + "/", bodyPart.getFileName() +
                                            "-" + arrayDateMail.get(i));
                                    InputStream is = bodyPart.getInputStream();
                                    FileOutputStream fos = null;
                                    try {
                                        fos = new FileOutputStream(f);
                                        byte[] buf = new byte[4096];
                                        int bytesRead;
                                        while ((bytesRead = is.read(buf)) != -1) {
                                            fos.write(buf, 0, bytesRead);
                                        }
                                        fos.close();
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            createMail(arraySubject.get(i), arrayFrom.get(i), arrayEmail.get(i), arrayContent.get(i), arrayDateMail.get(i), nameFolder, email, arrayPathAttachFiles);
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }

    /*
      * reverse the order of the messages
      */
    private static Message[] reverseMessageOrder(Message[] messages) {
        Message revMessages[] = new Message[messages.length];
        int i = messages.length - 1;
        for (int j = 0; j < messages.length; j++, i--) {
            revMessages[j] = messages[i];

        }
        return revMessages;
    }
}
