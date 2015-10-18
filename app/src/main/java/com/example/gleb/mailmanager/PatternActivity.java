package com.example.gleb.mailmanager;

import android.content.res.TypedArray;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.gleb.mailmanager.navigationdrawer.NavDrawerItem;
import com.example.gleb.mailmanager.navigationdrawer.NavDrawerListAdapter;

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
}
