package com.example.gleb.mailmanager.activities;

import android.content.res.TypedArray;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.gleb.mailmanager.navigationdrawer.NavDrawerItem;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerListAdapter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Created by Gleb on 18.10.2015.
 */
abstract class PatternActivity extends AppCompatActivity implements ListView.OnItemClickListener {
    public static final String TAG = "Tag";
    protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;
    // slide menu items
    protected String[] navMenuTitles;
    protected TypedArray navMenuIcons;
    protected List<NavDrawerItem> navDrawerItems;
    protected NavDrawerListAdapter adapter;
    // nav drawer title
    protected CharSequence mDrawerTitle;
    // used to store app title
    protected CharSequence mTitle;
    protected ImageView userImageView;

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
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + rootDirectory + "/" + typeMail + "/", from + "-" + date + ".txt");
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


    boolean delete(File file) {
        File[] files = file.listFiles();
        if (files != null)
            for (File f : files) delete(f);
        return file.delete();
    }
}
