package com.example.gleb.mailmanager.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.basics.MailStructure;
import com.example.gleb.mailmanager.filedialog.FileChooserActivity;
import com.example.gleb.mailmanager.images.RoundImage;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerItem;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerListAdapter;
import com.example.gleb.mailmanager.security.RSA;
import com.example.gleb.mailmanager.security.SHA1;
import com.example.gleb.mailmanager.security.TripleDes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * Created by Gleb on 21.10.2015.
 */
public class ItemMail extends PatternActivity {
    public static final String TAG = "Tag";
    public static final String MAIL = "Mail";
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";
    public static final String SMTP_SERVER = "Smtp server";
    public static final String SMTP_PORT = "Smtp port";
    public static final String IMAP_SERVER = "Imap server";
    public static final String IMAP_PORT = "Imap port";
    private TextView fromTextView;
    private TextView emailTextView;
    private TextView dateTextView;
    private TextView subjectTextView;
    private TextView contentTextView;
    private MailStructure mail;
    private int Numboftabs = 4;
    private String email;
    private String password;
    private String smtpHost;
    private String smtpPort;
    private String imapHost;
    private String imapPort;
    private ArrayList<String> filePath;
    public byte[] bytesRsaEncrypt;
    public byte[] byteSignature;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_mail);

        mail = (MailStructure) getIntent().getSerializableExtra(ItemMail.MAIL);
        email = getIntent().getStringExtra(ItemMail.EMAIL);
        password = getIntent().getStringExtra(ItemMail.PASSWORD);
        smtpHost = getIntent().getStringExtra(ItemMail.SMTP_SERVER);
        smtpPort = getIntent().getStringExtra(ItemMail.SMTP_PORT);
        imapHost = getIntent().getStringExtra(ItemMail.IMAP_SERVER);
        imapPort = getIntent().getStringExtra(ItemMail.IMAP_PORT);

        fromTextView = (TextView) findViewById(R.id.fromTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        dateTextView = (TextView) findViewById(R.id.dateTextView);
        subjectTextView = (TextView) findViewById(R.id.subjectTextView);
        contentTextView = (TextView) findViewById(R.id.contentTextView);
        webView = (WebView) findViewById(R.id.webView);

        fromTextView.setText(mail.getFrom());
        emailTextView.setText(mail.getEmail());
        dateTextView.setText(mail.getDate());
        subjectTextView.setText(mail.getSubject());
        contentTextView.setText(Html.fromHtml(mail.getContent()));
        webView.loadData(mail.getContent(), "text/html; charset=UTF-8;", null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ItemMail.this, Answer.class);
                intent.putExtra(Answer.EMAIL, email);
                intent.putExtra(Answer.PASSWORD, password);
                intent.putExtra(Answer.TO_EMAIL, emailTextView.getText().toString());
                intent.putExtra(Answer.TO_SUBJECT, subjectTextView.getText().toString());
                intent.putExtra(Answer.SMTP_SERVER, smtpHost);
                intent.putExtra(Answer.SMTP_PORT, smtpPort);
                intent.putExtra(Answer.IMAP_SERVER, imapHost);
                intent.putExtra(Answer.IMAP_PORT, imapPort);
                startActivity(intent);
            }
        });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_mail, menu);
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
            delete(new File(Environment.getExternalStorageDirectory(), email));
            finish();
            return true;
        }


        if (id == R.id.check_signature) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(ItemMail.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Проверка электронно-цифровая подписи");
            builder.setMessage("Проверить электронно-цифровую подпись?");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(ItemMail.this, FileChooserActivity.class);
                    startActivityForResult(intent, 2);
                }
            });
            builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //choose private key for decrypt 3des
                    Intent intent = new Intent(ItemMail.this, FileChooserActivity.class);
                    startActivityForResult(intent, 1);
                }
            });
            builder.show();
            return true;
        }

        if (id == R.id.dencrypt_mail) {
            //choose encrypt 3des by RSA public key
            Intent intent = new Intent(ItemMail.this, FileChooserActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //download file to folder "Download"
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                boolean fileCreated = false;
                String attachPath = "";
                String name = "";

                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    if (bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
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

                String message = fileCreated ? "File created" : "File opened";
                message += ": " + filePath;
                Toast.makeText(ItemMail.this, message, Toast.LENGTH_LONG).show();

                //get symetric key encrypt by RSA key
                bytesRsaEncrypt = readContentIntoByteArray(new File(attachPath));

                Intent intent = new Intent(ItemMail.this, FileChooserActivity.class);
                startActivityForResult(intent, 1);

            }
        } else {
            if (requestCode == 1) {
                //choose private key and decrypt mail
                if (resultCode == Activity.RESULT_OK) {
                    boolean fileCreated = false;
                    String attachPath = "";
                    String name = "";

                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        if (bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
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

                    //get bytes array of private key from files
                    byte[] bytesPrivateKey = readContentIntoByteArray(new File(attachPath));
                    PrivateKey privateKey = null;
                    try {
                        //create private key from array bytes
                        privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytesPrivateKey));
                        //decrypt encrypt RSA key
                        byte[] rsaDecrypt = RSA.decryptRSA(bytesRsaEncrypt, privateKey);
                        //change encode of byte array of decrypt key from ASCII to UTF-8
                        byte[] gen = Base64.decode(rsaDecrypt, Base64.DEFAULT);
                        String str = new String(gen);

                        TripleDes des = new TripleDes(str, emailTextView.getText().toString());

                        String s = contentTextView.getText().toString();
                        s = s.replace("null", "");
                        byte[] dataDecrypt = Base64.decode(s, Base64.DEFAULT);
                        byte[] textDecrypt = des.decrypt(dataDecrypt);
                        String text = new String(textDecrypt, "UTF-8");
                        //show decrypted text
                        contentTextView.setText(text);
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                //choose private key and decrypt mail
                if (resultCode == Activity.RESULT_OK) {
                    boolean fileCreated = false;
                    String attachPath = "";
                    String name = "";

                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        if (bundle.containsKey(FileChooserActivity.OUTPUT_NEW_FILE_NAME)) {
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

                    byteSignature = readContentIntoByteArray(new File(attachPath));

                    //string with public key from content of mail after &
                    String digest = contentTextView.getText().toString().substring(
                            contentTextView.getText().toString().indexOf("&") + 1,
                            contentTextView.getText().toString().length());
                    //delete last null from string of public key signature from content mail
                    digest = digest.replace("null", "");
                    //public key from signature in array bytes
                    byte[] dataDigest = Base64.decode(digest, Base64.DEFAULT);

                    //get main of content of mail without of public key of signature
                    String content = contentTextView.getText().toString().substring(0,
                            contentTextView.getText().toString().lastIndexOf('&'));
                    contentTextView.setText(content);
                    boolean verifies = true;
                    try {
                        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(dataDigest);
                        Signature verifyalg = Signature.getInstance("SHA1withDSA");
                        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
                        //generate public key for signature
                        PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);
                        verifyalg.initVerify(pubKey);
                        //add bytes of hash for check signature
                        verifyalg.update(SHA1.hexSha1Byte(contentTextView.getText().toString()));
                        verifies = verifyalg.verify(byteSignature);
                        Log.d(TAG, String.valueOf(verifies));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeySpecException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (SignatureException e) {
                        e.printStackTrace();
                    }

                    if (!verifies){
                        Toast.makeText(ItemMail.this, "Письмо было изменено", Toast.LENGTH_LONG).show();
                    }
                    else{
                        if (verifies){
                            Toast.makeText(ItemMail.this, "Письмо не было изменено", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
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
        } else {
            Toast.makeText(context, "Файл уже скачан", Toast.LENGTH_LONG).show();
        }
    }
}
