package com.example.gleb.mailmanager.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.images.RoundImage;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerItem;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerListAdapter;
import com.example.gleb.mailmanager.signin.SignIn;
import com.example.gleb.mailmanager.viewpager.ProfileViewPagerAdapter;
import com.example.gleb.mailmanager.sliding.SlidingTabLayout;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true, String.valueOf(newInboxMail)));
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

        nameFolders = new ArrayList<>();
        String host = email.substring(email.lastIndexOf("@") + 1);
        new Loader(imapServer, email, password, this, imapPort).execute();
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
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            delete(new File(Environment.getExternalStorageDirectory(), email));
            finish();
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
                intent = new Intent(MailManager.this, SignIn.class);
                startActivity(intent);
                break;
        }
    }

    public void createBaseFolder(){
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory()), email);
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), "Attach");
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
    * */
    public class Loader extends AsyncTask<String, String, String[]> {
        private String imapHost;
        private String user;
        private String password;
        private Context context;
        private String imapPort;

        public Loader(String imapHost, String user, String password, Context context, String imapPort) {
            this.imapHost = imapHost;
            this.user = user;
            this.password = password;
            this.context = context;
            this.imapPort = imapPort;
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
                        parseMailFromServer(store, fd.getName() + "/", "All Mail");
                        parseMailFromServer(store, fd.getName() + "/", "Drafts");
                        parseMailFromServer(store, fd.getName() + "/", "Sent Mail");
                        parseMailFromServer(store, fd.getName() + "/", "Spam");
                        parseMailFromServer(store, fd.getName() + "/", "Starred");
                        parseMailFromServer(store, fd.getName() + "/", "Trash");
                    }
                    else {
                        parseMailFromServer(store, fd.getName());
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

            viewadapter =  new ProfileViewPagerAdapter(getSupportFragmentManager(), titles, numTabs, email, password, imapServer, imapPort, nameFolders);

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
    private void parseMailFromServer(Store store, String nameFolder) throws MessagingException, IOException {
        nameFolders.add(nameFolder);
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), nameFolder);
        Folder folderBox = store.getFolder(nameFolder);
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

        if (messages.length != 0) {
            for (int i = 0; i < 5; i++) {
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
                                    arrayPathAttachFiles.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + "Attach" + "/" + bodyPart.getFileName());
                                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + "Attach" + "/", bodyPart.getFileName());
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
    * @param String gmail       Folder of gmail f.r. [Gmail]
    * @param String nameFolder  Folder for save mails in root directory
    * @return void
    * */
    private void parseMailFromServer(Store store, String gmail, String nameFolder) throws MessagingException, IOException {
        nameFolders.add(nameFolder);
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), nameFolder);
        Folder folderBox = store.getFolder(gmail + nameFolder);
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
                if (decodeAddress.indexOf("<") == -1 && decodeAddress.indexOf(">") == -1){
                    arrayEmail.add(decodeAddress);
                    arrayFrom.add(decodeAddress);
                }
                else {
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

                System.out.println("-------"+(i+1)+"-------");
                System.out.println(messages[i].getSentDate());
                Multipart multipart = (Multipart)content;
                List<String> arrayPathAttachFiles = new ArrayList<>();

                for(int j = 0; j < multipart.getCount(); j++) {
                    BodyPart bodyPart = multipart.getBodyPart(j);
                    Log.d(TAG, "bodyPart " + bodyPart.getFileName());
                    Log.d(TAG, "BodyPart " + bodyPart);
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        System.out.println("Creating file with name without : " + bodyPart.getFileName());
                        arrayPathAttachFiles.add(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + "Attach" + "/" + bodyPart.getFileName());
                        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + "Attach" + "/", bodyPart.getFileName());
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
}
