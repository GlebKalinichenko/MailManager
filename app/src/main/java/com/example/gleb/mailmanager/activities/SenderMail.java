package com.example.gleb.mailmanager.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.example.gleb.mailmanager.filedialog.FileChooserActivity;
import com.example.gleb.mailmanager.images.RoundImage;
import com.example.gleb.mailmanager.mailutils.Mail;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerItem;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerListAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Gleb on 18.10.2015.
 */
public class SenderMail extends PatternActivity {
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";
    public static final String NEW_INBOXMAIL = "Inbox";
    public static final String ALL_INBOXMAIL = "All mail";
    public static final String DELETEDMAIL = "Deleted mail";
    public static final String OUTBOXMAIL = "Outbox mail";
    public static final String DRAFTMAIL = "Draft mail";
    private String email;
    private String password;
    private EditText toEditText;
    private EditText subjectEditText;
    private EditText textEditText;
    private int newInboxMail;
    private int allInboxMail;
    private int deletedMail;
    private int outBoxMail;
    private int draftMail;
    private ArrayList<String> filePath;
    public ArrayList<String> a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_mail);

        initializeData();

        filePath = new ArrayList<String>();
        a = new ArrayList<String>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toEditText = (EditText) findViewById(R.id.toEditText);
        subjectEditText = (EditText) findViewById(R.id.subjectEditText);
        textEditText = (EditText) findViewById(R.id.textEditText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toEditText.getText().toString().equals("") || subjectEditText.getText().toString().equals("") || textEditText.getText().toString().equals("")) {
                    Toast.makeText(SenderMail.this, "Заполните все поля", Toast.LENGTH_LONG).show();
                } else {
                    Mail m = new Mail(email, password);
                    String[] toArr = {toEditText.getText().toString()};
                    m.setTo(toArr);
                    m.setFrom(email); // who is sending the email
                    m.setSubject(subjectEditText.getText().toString());
                    m.setBody(textEditText.getText().toString());
                try {
                    if (!filePath.equals(""))
                    m.addAttachment(filePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                    try {
                        if (m.send()) {
                            // success
                            Toast.makeText(SenderMail.this, "Письмо было отправлено.", Toast.LENGTH_LONG).show();
                        } else {
                            // failure
                            Toast.makeText(SenderMail.this, "Произошел сбой при отправке письма.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mTitle = mDrawerTitle = getTitle();
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), true, String.valueOf(newInboxMail)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), true, String.valueOf(outBoxMail)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), true, String.valueOf(draftMail)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), true, String.valueOf(deletedMail)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
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
    }

    public void initializeData(){
        email = getIntent().getStringExtra(SenderMail.EMAIL);
        password = getIntent().getStringExtra(SenderMail.PASSWORD);
        newInboxMail = getIntent().getIntExtra(SenderMail.NEW_INBOXMAIL, 0);
        allInboxMail = getIntent().getIntExtra(SenderMail.ALL_INBOXMAIL, 0);
        outBoxMail = getIntent().getIntExtra(SenderMail.OUTBOXMAIL, 0);
        draftMail = getIntent().getIntExtra(SenderMail.DRAFTMAIL, 0);
        deletedMail = getIntent().getIntExtra(SenderMail.DELETEDMAIL, 0);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        delete(new File(Environment.getExternalStorageDirectory(), email));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mail_magaer, menu);
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
            Intent intent = new Intent(SenderMail.this, FileChooserActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            boolean fileCreated = false;
            String attachPath = "";
            String name = "";

            Bundle bundle = data.getExtras();
            if(bundle != null)
            {
                if(bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
                    fileCreated = true;
                    File folder = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                    name = bundle.getString(FileChooserActivity.OUTPUT_NEW_FILE_NAME);
                    attachPath = folder.getAbsolutePath() + "/" + name;
                } else {
                    fileCreated = false;
                    File file = (File) bundle.get(FileChooserActivity.OUTPUT_FILE_OBJECT);
                    attachPath = file.getAbsolutePath();
                }
            }

            filePath.add(attachPath);

            String message = fileCreated? "File created" : "File opened";
            message += ": " + filePath;
            Toast.makeText(SenderMail.this, message, Toast.LENGTH_LONG).show();

            a.add(attachPath.substring(attachPath.lastIndexOf("/") + 1));
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Log.d(TAG, "Name " + filePath);
            ArchiveFragment fragment = new ArchiveFragment(a);
            fragmentTransaction.add(R.id.fragment_container, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private class ArchiveFragment extends Fragment {
        public static final String TAG = "TAG";
        public ArrayList<String> name;

        public ArchiveFragment(ArrayList<String> name) {
            this.name = name;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v =inflater.inflate(R.layout.attach_archive, container,false);
            LinearLayout layout = (LinearLayout) v.findViewById(R.id.LinearLayout1);
            for (int i = 0; i < name.size(); i++) {
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView tv = new TextView(getApplicationContext());
                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(R.drawable.zip50);
                tv.setId(i);
                tv.setText(name.get(i));
                tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                layout.addView(imageView, params1);
                layout.addView(tv, params1);
            }
            return v;
        }


    }
}
