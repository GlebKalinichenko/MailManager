package com.example.gleb.mailmanager.viewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.gleb.mailmanager.fragments.DraftFragment;
import com.example.gleb.mailmanager.fragments.InboxFragment;
import com.example.gleb.mailmanager.fragments.OutboxFragment;
import com.example.gleb.mailmanager.fragments.TrashFragment;

/**
 * Created by gleb on 14.07.15.
 */
public class ProfileViewPagerAdapter extends FragmentStatePagerAdapter {
    private CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when AdminViewPagerAdapter is created
    private int NumbOfTabs; // Store the number of tabs, this will also be passed when the AdminViewPagerAdapter is created
    private String email;
    private String password;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ProfileViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb, String email, String password) {
        super(fm);

        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
        this.email = email;
        this.password = password;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                InboxFragment tab1 = new InboxFragment(email, password);
                return tab1;

            case 1:
                OutboxFragment tab2 = new OutboxFragment(email, password);
                return tab2;

            case 2:
                DraftFragment tab3 = new DraftFragment(email, password);
                return tab3;

            case 3:
                TrashFragment tab4 = new TrashFragment(email, password);
                return tab4;

        }

        return null;


//            if(position == 0) // if the position is 0 we are returning the First tab
//            {
//                InboxFragment tab1 = new InboxFragment();
//                return tab1;
//            }
//            else{
//                if(position == 1) // if the position is 0 we are returning the First tab
//                {
//                    InboxFragment tab2 = new InboxFragment();
//                    return tab2;
//                }
//                else{
//                    if(position == 2) // if the position is 0 we are returning the First tab
//                    {
//                        InboxFragment tab3 = new InboxFragment();
//                        return tab3;
//                    }
//                    else{
//                        if(position == 3) // if the position is 0 we are returning the First tab
//                        {
//                            InboxFragment tab4 = new InboxFragment();
//                            return tab4;
//                        }
//
//                    }
//
//                }
//
//            }
//
//            InboxFragment tab5 = new InboxFragment();
//            return null;
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
