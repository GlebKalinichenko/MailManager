package com.example.gleb.mailmanager.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.basics.MailStructure;
import com.example.gleb.mailmanager.images.RoundImage;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerItem;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerListAdapter;
import com.example.gleb.mailmanager.signin.SignIn;
import com.example.gleb.mailmanager.sliding.SlidingTabLayout;
import com.example.gleb.mailmanager.viewpager.ProfileViewPagerAdapter;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Gleb on 21.10.2015.
 */
public class ItemMail extends PatternActivity {
    public static final String TAG = "Tag";
    public static final String MAIL = "Mail";
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";
    private TextView fromTextView;
    private TextView emailTextView;
    private TextView dateTextView;
    private TextView subjectTextView;
    private TextView contentTextView;
    private MailStructure mail;
    private int Numboftabs = 4;
    private String email;
    private String password;
    private int newInboxMail;
    private int allInboxMail;
    private int deletedMail;
    private int outBoxMail;
    private int draftMail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_mail);

        mail = (MailStructure) getIntent().getSerializableExtra(ItemMail.MAIL);
        email = getIntent().getStringExtra(ItemMail.EMAIL);
        password = getIntent().getStringExtra(ItemMail.PASSWORD);

        fromTextView = (TextView) findViewById(R.id.fromTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        subjectTextView = (TextView) findViewById(R.id.subjectTextView);
        contentTextView = (TextView) findViewById(R.id.contentTextView);

        fromTextView.setText(mail.getFrom());
        emailTextView.setText(mail.getEmail());
        dateTextView.setText(mail.getDate());
        subjectTextView.setText(mail.getSubject());
        contentTextView.setText(mail.getContent());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!isTheSame(mail.getAttachFiles(), new String[]{""})) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ArchiveFragment fragment = new ArchiveFragment(mail);
            fragmentTransaction.add(R.id.fragment_container, fragment);
            fragmentTransaction.commitAllowingStateLoss();
        }

        mTitle = mDrawerTitle = getTitle();
        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        //initialize navigation drawer
        navDrawerItems = new ArrayList<NavDrawerItem>();
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
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
        ) {
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    /*
    * Equals first array of String second array of String
    * @param String[] arr1        First array of String
    * @param String[] arr2        Second array of String
    * @return boolean             Is same first array like second array
    * */
    public boolean isTheSame(String[] arr1, String[] arr2) {
        if (arr1.length != arr2.length) return false;
        for (int i = 0; i < arr1.length; i++)
            if (!arr1[i].equals(arr2[i]))
                return false;
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        delete(new File(Environment.getExternalStorageDirectory(), email));
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

    /*
    * Class for show attach to receive file
    * */
    private class ArchiveFragment extends Fragment {
        public static final String TAG = "TAG";
        private MailStructure mail;
        private ProgressBar progressBar;

        public ArchiveFragment(MailStructure mail) {
            this.mail = mail;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.attach_archive, container, false);
            final LinearLayout layout = (LinearLayout) v.findViewById(R.id.LinearLayout1);
            for (int i = 0; i < mail.getAttachFiles().length; i++) {
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView tv = new TextView(getApplicationContext());
                ImageView imageView = new ImageView(getContext());
                imageView.setImageResource(R.drawable.zip50);
                tv.setId(i);
                tv.setText(mail.getAttachFiles()[i].split("/")[mail.getAttachFiles()[i].split("/").length - 1]);
                tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
                final Button button = new Button(getContext());
                button.setText("Скачать");
                button.setTextColor(getResources().getColor(R.color.colorPrimary));
                button.setId(i);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setIndeterminate(true);
                        progressBar.setVisibility(View.VISIBLE);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String[] values = mail.getAttachFiles()[button.getId()].split("/");
                                try {
                                    copy(new File(mail.getAttachFiles()[button.getId()]),
                                            new File(Environment.getExternalStorageDirectory() + "/" + "Download" + "/", values[values.length - 1]),
                                            getContext());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }, 2000);
                    }
                });
                layout.addView(imageView, params1);
                layout.addView(tv, params1);
                layout.addView(button, params1);
            }
            return v;
        }
    }

    /*
    * Copy of first file to second file
    * @param File src        First file that will be copied
    * @param File dst        Second file that copy first file
    * @return void
    * */
    public void copy(File src, File dst, Context context) throws IOException {
        InputStream in = new FileInputStream(src);
        if (dst.createNewFile()) {
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        else{
            Toast.makeText(context, "Файл уже скачан", Toast.LENGTH_LONG).show();
        }
    }
}
