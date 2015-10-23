package com.example.gleb.mailmanager.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.activities.ItemMail;
import com.example.gleb.mailmanager.recyclerview.ItemRecycler;
import com.example.gleb.mailmanager.swipe.SuperSwipeRefreshLayout;

/**
 * Created by Gleb on 20.10.2015.
 */
public class OutboxFragment extends PatternFragment {
    public static final String TAG = "Tag";

    public OutboxFragment(String email, String password) {
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

        rv.addOnItemTouchListener(
                new ItemRecycler.RecyclerItemClickListener(getActivity(), new ItemRecycler.RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Log.d(TAG, "Value" + position);
                        Intent intent = new Intent(getContext(), ItemMail.class);
                        intent.putExtra(ItemMail.MAIL, mailStructures.get(position));
                        intent.putExtra(ItemMail.EMAIL, email);
                        intent.putExtra(ItemMail.PASSWORD, password);
                        startActivity(intent);
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
                        updateMail();
                        readMailFromStore("Отправленные");
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

        readMailFromStore("Отправленные");

        return v;
    }

}
