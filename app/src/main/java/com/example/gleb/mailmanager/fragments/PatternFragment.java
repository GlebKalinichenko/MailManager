package com.example.gleb.mailmanager.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.activities.MailManager;
import com.example.gleb.mailmanager.basics.MailStructure;
import com.example.gleb.mailmanager.recyclerview.DividerItemDecoration;
import com.example.gleb.mailmanager.recyclerview.RVAdapter;
import com.example.gleb.mailmanager.sliding.SlidingTabLayout;
import com.example.gleb.mailmanager.swipe.SuperSwipeRefreshLayout;
import com.example.gleb.mailmanager.viewpager.ProfileViewPagerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
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
    protected String imapHost;
    protected String imapPort;
    protected ActionMode actionMode;
    protected List<String> nameFolders;

    /*
    * Create file in root directory from data recieve from server
    * @param String subject             Subject of mail
    * @param String from                Name sender of mail
    * @param String emailMail           Email sender of mail
    * @param String content             Content of mail
    * @param String date                Send date of mail
    * @param String typeMail            Type mail receive from server f.e. inbox or outbox
    * @param String rootDirectory       Name directory for accounted user that has value of email of mail
    * @param List<String> attachFiles   List of path to attach files from subdirectory "Attach" in root directory
    * @return void
    * */
    protected void createMail(String subject, String from, String emailMail, String content, String date, String typeMail, String rootDirectory, List<String> attachFiles){
        if (subject == null){
            subject = "";
        }
        Log.d(TAG, "Subject " + subject + " from " + from + "Content " + content);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MailManager.MAIL_MANAGER + "/" + rootDirectory + "/" + typeMail + "/", from + "-" + date + ".txt");
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
            fileWriter.write(String.valueOf(attachFiles));
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
    * Read mails from user root directory + typeMail
    * @param String email              Name user of root directory
    * @param String typeMail           Name of directory for read files of mail
    * @return List<MailStructure>      List of mails load from files in root directory + typeMail
    * */
    protected List<MailStructure> readMail(String rootDirectory, String typeMail){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + MailManager.MAIL_MANAGER + "/" + rootDirectory + "/" + typeMail + "/";
        Log.d("Files", "Path: " + path);
        File f = new File(path);

        File file[] = f.listFiles();
        String fromMail;
        String emailMail;
        String dateMail;
        String[] attachFiles;
        String subjectMail;
        String contentMail;
        List<MailStructure> mails = new ArrayList<MailStructure>();
        if (file != null) {
            for (int i = 0; i < file.length; i++) {
                int value = 0;
                fromMail = "";
                emailMail = "";
                subjectMail = "";
                contentMail = "";
                dateMail = "";
                attachFiles = new String[]{};
                Log.d("Files", "FileName:" + file[i].getName());
                try {
                    InputStream instream = new FileInputStream(path + file[i].getName());
                    if (instream != null) {
                        InputStreamReader inputreader = new InputStreamReader(instream);
                        BufferedReader buffreader = new BufferedReader(inputreader);
                        String line = "";

                        while (line != null) {
                            line = buffreader.readLine();
                            switch (value) {
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
                                    Log.d(TAG, line);
                                    //parse string with attach files f.e. can be some files of one file
                                    String nameFile = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                                    String[] files = nameFile.split(", ");
                                    attachFiles = files;
                                    break;

                                case 4:
                                    subjectMail = line;
                                    break;

                                default:
                                    contentMail += line;
                                    break;
                            }
                            value += 1;
                        }
                        mails.add(new MailStructure(fromMail, emailMail, subjectMail, contentMail, dateMail, attachFiles));
                        instream.close();

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            return mails;
        }
        else{
            return null;
        }
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
            //read mail from file from directory of typeMail in root directory
            mailStructures = readMail(email, typeMail);
            RVAdapter adapter = new RVAdapter(mailStructures, context);
            RecyclerView.ItemDecoration itemDecoration = new
                    DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
            rv.addItemDecoration(itemDecoration);
            adapter.notifyDataSetChanged();
            rv.setAdapter(adapter);
        }
    }

    /*
    * Create dorectories with place path/name
    * @param String path        Path to root directory
    * @param String name        Name of creating directory
    * @rreturn boolean          Is create directory
    * */
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
    protected void deleteInRootDirectory(List<String> folders){
        delete(new File(Environment.getExternalStorageDirectory() + "/" + MailManager.MAIL_MANAGER + "/" + email, "Attach"));
        for (int i = 0; i < folders.size(); i++){
            delete(new File(Environment.getExternalStorageDirectory() + "/" + email, folders.get(i)));
        }
    }

    /*
    * Update all mails at first delete all directiry in mail box and load then again
    * @param String imapHost        Host of imap server
    * @param String imapPort        Port of imap sever
    * @param List<String> folders   Folders for delete
    * */
    protected void updateMail(String imapHost, String imapPort, List<String> folders, int numMails, int offsetMail){
        //clear files in root directory
        deleteInRootDirectory(folders);
        //create folder for attach
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), "Attach");
        //load mail from server to root directory for update mail
        nameFolders = new ArrayList<>();
        new Loader(imapHost, email, password, getContext(), imapPort, offsetMail, numMails).execute();
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
        private int offsetMail;
        private int numMails;

        public Loader(String imapHost, String user, String password, Context context, String imapPort, int offsetMail, int numMails) {
            this.imapHost = imapHost;
            this.user = user;
            this.password = password;
            this.context = context;
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
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), nameFolder);
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
        createDirIfNotExists(String.valueOf(Environment.getExternalStorageDirectory() + "/" + email), nameFolder);
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

    /*
    * Read mails from root directory + folder
    * @param String folder        Name of directory for readed files with mails
     * @return void
    * */
    protected void readMailFromStore(String folder){
        new Reader(folder, getContext()).execute();
    }
}
