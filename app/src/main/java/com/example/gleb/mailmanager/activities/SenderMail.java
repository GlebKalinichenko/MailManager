package com.example.gleb.mailmanager.activities;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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
import com.example.gleb.mailmanager.security.RSA;
import com.example.gleb.mailmanager.security.SHA1;
import com.example.gleb.mailmanager.security.TripleDes;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by Gleb on 18.10.2015.
 */
public class SenderMail extends PatternActivity {
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";
    public static final String SMTP_SERVER = "Smtp server";
    public static final String SMTP_PORT = "Smtp port";
    public static final String NEW_INBOXMAIL = "Inbox";
    public static final String ALL_INBOXMAIL = "All mail";
    public static final String DELETEDMAIL = "Deleted mail";
    public static final String OUTBOXMAIL = "Outbox mail";
    public static final String DRAFTMAIL = "Draft mail";
    public static final String IMAP_SERVER = "Imap server";
    public static final String IMAP_PORT = "Imap port";
    private String email;
    private String password;
    private String smtpHost;
    private String smtpPort;
    private EditText toEditText;
    private EditText subjectEditText;
    private EditText textEditText;
    private int newInboxMail;
    private int allInboxMail;
    private int deletedMail;
    private int outBoxMail;
    private int draftMail;
    private String imapHost;
    private String imapPort;
    private ArrayList<String> filePath;
    public ArrayList<String> headerAttach;
    private String genKey;
    private TripleDes des;
    private RSA rsa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_mail);

        initializeData();

        filePath = new ArrayList<String>();
        headerAttach = new ArrayList<String>();
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
                    Mail m = new Mail(email, password, smtpHost, smtpPort, imapHost, imapPort);
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
//                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                            Date date = new Date();
                            // success
                            Toast.makeText(SenderMail.this, "Письмо было отправлено.", Toast.LENGTH_LONG).show();
//                            createMail(subjectEditText.getText().toString(), toEditText.getText().toString(),
//                                    toEditText.getText().toString(), textEditText.getText().toString(),
//                                    date.toString(), "Отправленные", email, filePath);
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


    public void initializeData() {
        email = getIntent().getStringExtra(SenderMail.EMAIL);
        password = getIntent().getStringExtra(SenderMail.PASSWORD);
        smtpHost = getIntent().getStringExtra(SenderMail.SMTP_SERVER);
        smtpPort = getIntent().getStringExtra(SenderMail.SMTP_PORT);
        newInboxMail = getIntent().getIntExtra(SenderMail.NEW_INBOXMAIL, 0);
        allInboxMail = getIntent().getIntExtra(SenderMail.ALL_INBOXMAIL, 0);
        outBoxMail = getIntent().getIntExtra(SenderMail.OUTBOXMAIL, 0);
        draftMail = getIntent().getIntExtra(SenderMail.DRAFTMAIL, 0);
        deletedMail = getIntent().getIntExtra(SenderMail.DELETEDMAIL, 0);
        imapHost = getIntent().getStringExtra(SenderMail.IMAP_SERVER);
        imapPort = getIntent().getStringExtra(SenderMail.IMAP_PORT);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_mail, menu);
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
            Intent intent = new Intent(SenderMail.this, FileChooserActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }

        if (id == R.id.signature_mail) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(SenderMail.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Электронно-цифровая подпись");
            builder.setMessage("Подписать письмо электронно-цифровой подписью?");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        hashText(textEditText.getText().toString());
                    } catch (NoSuchProviderException e) {
                        Toast.makeText(SenderMail.this, "Value 1", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        Toast.makeText(SenderMail.this, "Value 2", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } catch (SignatureException e) {
                        Toast.makeText(SenderMail.this, "Value 3", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        Toast.makeText(SenderMail.this, "Value 4", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        Toast.makeText(SenderMail.this, "Value 5", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } catch (GeneralSecurityException e) {
                        Toast.makeText(SenderMail.this, "Value 6", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    } catch (IOException e) {
                        Toast.makeText(SenderMail.this, "Value 7", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton("Отмена", null);
            builder.show();
            return true;
        }

        //encrypt mail with using public key
        if (id == R.id.encrypt_mail) {
            Intent intent = new Intent(SenderMail.this, FileChooserActivity.class);
            startActivityForResult(intent, 1);
            return true;
        }

        //generate key for encryption and decryption
        if (id == R.id.generator_key) {
            try {
                File[] files = new File(Environment.getExternalStorageDirectory() + "/" + email, "/Keys").listFiles();
                if (findGenerateKey(files, toEditText.getText().toString())) {
                    Toast.makeText(SenderMail.this, "Ключ уже сгенерирован", Toast.LENGTH_LONG).show();
                } else {
                    rsa = new RSA();
                    String publicPath = RSA.createFile(toEditText.getText().toString() + "-" + "Public.txt", rsa.getPublicKey().getEncoded(), email);
                    String privatePath = RSA.createFile(toEditText.getText().toString() + "-" + "Private.txt", rsa.getPrivateKey().getEncoded(), email);

                    filePath.add(publicPath);
                    headerAttach.add(publicPath.substring(publicPath.lastIndexOf("/") + 1));
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Log.d(TAG, "Name " + filePath);
                    ArchiveFragment fragment = new ArchiveFragment(headerAttach);
                    fragmentTransaction.add(R.id.fragment_container, fragment);
                    fragmentTransaction.commitAllowingStateLoss();
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //attach file
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

                filePath.add(attachPath);

                String message = fileCreated ? "File created" : "File opened";
                message += ": " + filePath;
                Toast.makeText(SenderMail.this, message, Toast.LENGTH_LONG).show();

                headerAttach.add(attachPath.substring(attachPath.lastIndexOf("/") + 1));
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                Log.d(TAG, "Name " + filePath);
                ArchiveFragment fragment = new ArchiveFragment(headerAttach);
                fragmentTransaction.add(R.id.fragment_container, fragment);
                fragmentTransaction.commitAllowingStateLoss();
            }
        } else {
            //choose public key
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

                genKey = TripleDes.generateString("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
                try {
                    des = new TripleDes(genKey, email);

                    byte[] textEncrypt = des.encrypt(textEditText.getText().toString().getBytes("UTF-8"));
                    byte[] textEncryptBase64 = Base64.encode(textEncrypt, Base64.DEFAULT);
                    textEditText.setText(new String(textEncryptBase64, "UTF-8"));

                    byte[] dataDecrypt = Base64.decode(textEditText.getText().toString(), Base64.DEFAULT);
                    byte[] textDecrypt = des.decrypt(dataDecrypt);
                    String text = new String(textDecrypt, "UTF-8");

                    //read bytes from file for public key
                    byte[] bytes = readContentIntoByteArray(new File(attachPath));
                    //generate public key from bytes from file
                    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
                    //encrypt bytes of key for 3des with ASCII
                    byte[] rsaEncrypt = RSA.encryptRSA(Base64.encode(genKey.getBytes(), Base64.DEFAULT), publicKey);
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    String encryptDesKey = RSA.createFile(toEditText.getText().toString() + "-" + timeStamp + "-" + "RsaDes.txt", rsaEncrypt, email);
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
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
    * Create ElGamal digest signature for encrypted text
    * */
    private void hashText(String message) throws GeneralSecurityException, IOException {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("DSA");
        kg.initialize(1024);
        KeyPair pair = kg.generateKeyPair();
        PrivateKey privKey = pair.getPrivate();
        PublicKey pubKey = pair.getPublic();

        Signature dsa = Signature.getInstance("SHA1withDSA");
        dsa.initSign(privKey);
        if (message.lastIndexOf('=') != -1){
            dsa.update(SHA1.hexSha1Byte(message.substring(0, message.lastIndexOf('=') + 1)));
        }
        else{
            dsa.update(SHA1.hexSha1Byte(message));
        }
        byte[] realSig = dsa.sign();

        //add public key for verify
        byte[] arrayPubKeyEncrypt = Base64.encode(pubKey.getEncoded(), Base64.DEFAULT);
        textEditText.append("&" + new String(arrayPubKeyEncrypt, "UTF-8"));

        //add signature key for verify
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        String encryptDesKey = RSA.createFile(toEditText.getText().toString() + "-" + timeStamp + "-" + "Signature.txt", realSig, email);

//        Signature verifyalg = Signature.getInstance("SHA256withDSA");
//        verifyalg.initVerify(pubKey);
//        message = "aaaaaa" + message;
//        verifyalg.update(SHA256.hexSha1Byte(message.substring(0, message.lastIndexOf('=') + 1)));
//        boolean verifies = verifyalg.verify(realSig);
    }

    private class ArchiveFragment extends Fragment {
        public static final String TAG = "TAG";
        public ArrayList<String> name;

        public ArchiveFragment(ArrayList<String> name) {
            this.name = name;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.attach_archive, container, false);
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
