package com.example.gleb.mailmanager.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gleb.mailmanager.recyclerview.ItemRecycler;
import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.recyclerview.RVAdapter;
import com.example.gleb.mailmanager.swipe.SuperSwipeRefreshLayout;
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
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

/**
 * Created by Gleb on 18.10.2015.
 */
public class InboxFragment extends PatternFragment {
    public static final String TAG = "TAG";

    public InboxFragment(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_mail_manager, container, false);
        rv=(RecyclerView) v.findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

//        initializeData();
//        RVAdapter adapter = new RVAdapter(mailStructures);
//        rv.setAdapter(adapter);

        rv.addOnItemTouchListener(
                new ItemRecycler.RecyclerItemClickListener(getActivity(), new ItemRecycler.RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d(TAG, "Value" + position);
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                })
        );
        swipeRefreshLayout = (SuperSwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        View child = LayoutInflater.from(swipeRefreshLayout.getContext()).inflate(R.layout.layout_head, null);
        progressBar = (ProgressBar) child.findViewById(R.id.progressBar);
        textView = (TextView) child.findViewById(R.id.text_view);
        swipeRefreshLayout
                .setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {

                    @Override
                    public void onRefresh() {
                        textView.setText("К вам летит письмо...");
                        progressBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                                progressBar.setVisibility(View.GONE);
                            }
                        }, 2000);

                    }

                    @Override
                    public void onPullDistance(int distance) {
                        //myAdapter.updateHeaderHeight(distance);
                    }

                    @Override
                    public void onPullEnable(boolean enable) {
//                        imageView.setVisibility(View.VISIBLE);
//                        imageView.setRotation(enable ? 180 : 0);
                    }
                });

        String host = email.substring(email.lastIndexOf("@") + 1);
        Log.d(TAG, "Host email " + host);
        switch (host){
            case "yandex.ru":
                new Loader("imap.yandex.ru", email, password, "INBOX", getContext()).execute();
            break;

            case "yandex.ua":
                new Loader("imap.yandex.ru", email, password, "INBOX", getContext()).execute();
            break;

            case "gmail.com":
                new Loader("imap.googlemail.com", email, password, "INBOX", getContext()).execute();
            break;

            case "ukr.net":
                new Loader("imap.ukr.net", email, password, "INBOX", getContext()).execute();
            break;

            case "rambler.ru":
                new Loader("imap.rambler.ru", email, password, "INBOX", getContext()).execute();
            break;
        }

        return v;
    }
}
