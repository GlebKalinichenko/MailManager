package com.example.gleb.mailmanager.viewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.gleb.mailmanager.fragments.InboxFragment;

import java.util.List;

/**
 * Created by gleb on 14.07.15.
 */
public class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {
    private CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when AdminViewPagerAdapter is created
    private int NumbOfTabs; // Store the number of tabs, this will also be passed when the AdminViewPagerAdapter is created
    private String email;
    private String password;
    private String imapHost;
    private String imapPort;
    private List<String> nameFolders;
    private int numMails;
    private int offsetMails;
    private String smtpHost;
    private String smtpPort;

    // Build headerAttach Constructor and assign the passed Values to appropriate values in the class
    public ProfileViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb,
                                   String email, String password, String imapHost, String imapPort,
                                   List<String> nameFolders, int numMails, int offsetMails, String smtpHost, String smtpPort) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
        this.email = email;
        this.password = password;
        this.imapHost = imapHost;
        this.imapPort = imapPort;
        this.nameFolders = nameFolders;
        this.numMails = numMails;
        this.offsetMails = offsetMails;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        InboxFragment tab1 = new InboxFragment(email, password, imapHost, imapPort, nameFolders.get(position), nameFolders, offsetMails, numMails, smtpHost, smtpPort);
        return tab1;
    }




    // This method return the titles for the Tabs in the Tab Strip

    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }

}
