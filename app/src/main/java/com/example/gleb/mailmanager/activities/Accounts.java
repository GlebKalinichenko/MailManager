package com.example.gleb.mailmanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.basics.User;
import com.example.gleb.mailmanager.recyclerview.AccountAdapter;
import com.example.gleb.mailmanager.recyclerview.ItemRecycler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gleb on 29.10.2015.
 */
public class Accounts extends PatternActivity {
    public static final String TAG = "Tag";
    public static final String ACCOUNTS = "Accounts";
    public static final String EMAIL = "Email";
    public static final String PASSWORD = "Password";
    private RecyclerView rv;
    private String accountJSON;
    private List<User>  accounts;
    private AccountAdapter accountAdapter;
    private List<Integer> positions;
    private ActionMode actionMode;
    private SharedPreferences sPref;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accounts);

        initializeValues();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rv = (RecyclerView) findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        rv.addOnItemTouchListener(
                new ItemRecycler.RecyclerItemClickListener(Accounts.this, new ItemRecycler.RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(Accounts.this, MailManager.class);
                        intent.putExtra(MailManager.EMAIL, accounts.get(position).getEmail());
                        intent.putExtra(MailManager.PASSWORD, accounts.get(position).getPassword());
                        intent.putExtra(MailManager.SMTP_SERVER, accounts.get(position).getSmtpServer());
                        intent.putExtra(MailManager.SMTP_PORT, accounts.get(position).getSmtpPort());
                        intent.putExtra(MailManager.IMAP_SERVER, accounts.get(position).getImapServer());
                        intent.putExtra(MailManager.IMAP_PORT, accounts.get(position).getImapPort());
                        intent.putExtra(MailManager.IS_CHANGE, 1);
                        intent.putExtra(MailManager.NUM_MAILS, accounts.get(position).getNumMails());
                        intent.putExtra(MailManager.OFFSET_MAIL, accounts.get(position).getOffsetMail());
                        startActivity(intent);
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {
//                        positions = new ArrayList<Integer>();
//                        positions.add(position);
//                        actionMode = startActionMode(callback);
                    }
                })
        );

        accountAdapter = new AccountAdapter(accounts);
        rv.setAdapter(accountAdapter);
    }

    /*
    * Initialize variables for show content about mails of user
    * @param void
    * @return void
    * */
    private void initializeValues(){
        accountJSON = getIntent().getStringExtra(Accounts.ACCOUNTS);
        email = getIntent().getStringExtra(Accounts.EMAIL);
        password = getIntent().getStringExtra(Accounts.PASSWORD);
        accounts = new Gson().fromJson(accountJSON, new TypeToken<List<User>>(){}.getType());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
                    sPref = getPreferences(MODE_PRIVATE);
                    SharedPreferences.Editor ed = sPref.edit();

//                    for (int i = 0; i < accounts.size(); i++){
//                        int headerAttach = positions.get(0);
//                        if (accounts.get(i).getEmail().equals(accounts.get(headerAttach))){
//                            accounts.remove(i);
//                        }
//                    }
                    int a = positions.get(0);
                    accounts.remove(a);

                    ed.putString(MailManager.SAVE_USERS, new Gson().toJson(accounts));
                    ed.commit();
                    delete(new File(Environment.getExternalStorageDirectory(), email));
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
}
