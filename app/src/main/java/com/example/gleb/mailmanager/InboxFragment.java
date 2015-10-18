package com.example.gleb.mailmanager;


import android.os.Bundle;
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

import com.example.gleb.mailmanager.basics.MailStructure;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gleb on 18.10.2015.
 */
public class InboxFragment extends Fragment {
    public static final String TAG = "TAG";
    private RecyclerView rv;
    private List<MailStructure> mailStructures;
    private SuperSwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView textView;


    public InboxFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_mail_manager, container, false);
        rv=(RecyclerView) v.findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        RVAdapter adapter = new RVAdapter(mailStructures);
        rv.setAdapter(adapter);

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
        return v;
    }


    private void initializeData() {
        mailStructures = new ArrayList<>();
        mailStructures.add(new MailStructure("Chernishova A.V.", "chernyshova.alla@rambler.ru", "Subject of mail", "Content of mail", "18.10.2015"));
        mailStructures.add(new MailStructure("Fediaev O.I.", "Fediaev@yandex.ua", "Subject of mail", "Content of mail", "18.10.2015"));
        mailStructures.add(new MailStructure("Kolomoitseva I.A.", "Kolomoitseva@yandex.ua", "Subject of mail", "Content of mail", "18.10.2015"));
        mailStructures.add(new MailStructure("Kolomoitseva I.A.", "Kolomoitseva@yandex.ua", "Subject of mail", "Content of mail", "18.10.2015"));
    }
}
