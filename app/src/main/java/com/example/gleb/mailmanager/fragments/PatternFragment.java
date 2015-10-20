package com.example.gleb.mailmanager.fragments;

import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;

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

/**
 * Created by Gleb on 20.10.2015.
 */
abstract class PatternFragment extends Fragment {
    public static final String TAG = "Tag";
    /*
    * Create mail file in user root directory for user
    * @param String subject        Subject of mail
    * @param String from           Name of sender of mail
    * @param String emailMail      Email of sender of mail
    * @param String date           Send date of mail
    * @return void
    * */
    protected void createMail(String subject, String from, String emailMail, String content, String date, String typeFolder, String emailDirectory){
        if (subject == null){
            subject = "";
        }
        Log.d(TAG, "Subject " + subject + " from " + from + "Content " + content);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + emailDirectory + "/Inbox/", from + "-" + date + ".txt");
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
    protected List<MailStructure> readMail(String email, String folder){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + email + "/" + folder + "/";
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
