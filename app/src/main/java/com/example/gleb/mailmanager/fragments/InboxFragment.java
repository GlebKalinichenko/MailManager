package com.example.gleb.mailmanager.fragments;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.gleb.mailmanager.activities.ItemMail;
import com.example.gleb.mailmanager.recyclerview.ItemRecycler;
import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.swipe.SuperSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Created by Gleb on 18.10.2015.
 */
public class InboxFragment extends PatternFragment {
    public static final String TAG = "TAG";
    private List<Integer> positions;
    private String nameFolder;
    private List<String> folders;
    private int numMails;
    private int offsetMails;
    private String smtpHost;
    private String smtpPort;

    public InboxFragment(String email, String password, String imapHost, String imapPort, String nameFolder, List<String> folders, int numMails, int offsetMails,
                         String smtpHost, String smtpPort) {
        this.email = email;
        this.password = password;
        this.imapHost = imapHost;
        this.imapPort = imapPort;
        this.nameFolder = nameFolder;
        this.folders = folders;
        this.numMails = numMails;
        this.offsetMails = offsetMails;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        Log.d(TAG, nameFolder);
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
                        intent.putExtra(ItemMail.SMTP_SERVER, smtpHost);
                        intent.putExtra(ItemMail.SMTP_PORT, smtpPort);
                        intent.putExtra(ItemMail.IMAP_SERVER, imapHost);
                        intent.putExtra(ItemMail.IMAP_PORT, imapPort);
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {
                        positions = new ArrayList<Integer>();
                        positions.add(position);
                        actionMode = getActivity().startActionMode(callback);
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
                        updateMail(imapHost, imapPort, folders, offsetMails, numMails);
                        readMailFromStore(nameFolder);
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
            //begin to read mails from root directory + directory "Inbox"
            readMailFromStore(nameFolder);

        return v;
    }

    private ActionMode.Callback callback = new ActionMode.Callback() {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_header, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                        new Deleter(positions).execute();
                    return true;
                default:
                    return false;
            }
        }

        public void onDestroyActionMode(ActionMode mode) {
            Log.d(TAG, "destroy");
            actionMode = null;
        }

    };

    /*
    * Delete item mail copy mail into folder Trash and delete from add flag deleted
    * */
    public class Deleter extends AsyncTask<String, String, String> {
        private List<Integer> positionMail;

        public Deleter(List<Integer> positionMail) {
            this.positionMail = positionMail;
        }

        @Override
        protected String doInBackground(String... params) {
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
                Folder folderBox = store.getFolder("INBOX");
                folderBox.open(Folder.READ_WRITE);

                Message[] messages = folderBox.getMessages();
                for (int i = 0; i < positionMail.size(); i++) {
                    folderBox.copyMessages(new Message[]{messages[positionMail.get(i)]}, store.getFolder("Удаленные"));
                    messages[positionMail.get(i)].setFlag(Flags.Flag.DELETED, true);
                }
                folderBox.close(true);
                store.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
