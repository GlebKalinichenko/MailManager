package com.example.gleb.mailmanager.recyclerview;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.basics.User;

import java.util.List;

/**
 * Created by Gleb on 17.10.2015.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    public static final String TAG = "Tag";
    private List<User> accounts;

    public AccountAdapter(List<User> accounts){
        this.accounts = accounts;
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView emailAccount;
        TextView passwordAccount;

        AccountViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            emailAccount = (TextView)itemView.findViewById(R.id.emailAccount);
            passwordAccount = (TextView)itemView.findViewById(R.id.passwordAccount);
        }

//        public void setItem(String item) {
//            mItem = item;
//            mTextView.setText(item);
//        }

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_account_item, viewGroup, false);
        AccountViewHolder pvh = new AccountViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(AccountViewHolder personViewHolder, int i) {
        personViewHolder.emailAccount.setText(accounts.get(i).getEmail());
        personViewHolder.passwordAccount.setText(accounts.get(i).getPassword());
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public boolean isTheSame(String[] arr1, String[] arr2) {
        if (arr1.length != arr2.length) return false;
        for (int i = 0; i < arr1.length; i++)
            if (!arr1[i].equals(arr2[i]))
                return false;
        return true;
    }
}
