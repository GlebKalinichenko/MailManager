package com.example.gleb.mailmanager.recyclerview;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gleb.mailmanager.R;
import com.example.gleb.mailmanager.basics.MailStructure;

import java.util.List;

/**
 * Created by Gleb on 17.10.2015.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.MailViewHolder> {
    public static final String TAG = "Tag";
    private List<MailStructure> mailStructures;

    public RVAdapter(List<MailStructure> mailStructures){
        this.mailStructures = mailStructures;
    }

    public static class MailViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView fromName;
        TextView fromEmail;
        TextView subject;
        ImageView photoUser;

        MailViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            fromName = (TextView)itemView.findViewById(R.id.fromName);
            fromEmail = (TextView)itemView.findViewById(R.id.fromEmail);
            subject = (TextView)itemView.findViewById(R.id.subject);
            photoUser = (ImageView)itemView.findViewById(R.id.photoUser);
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
    public MailViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_mail_item, viewGroup, false);
        MailViewHolder pvh = new MailViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(MailViewHolder personViewHolder, int i) {
        personViewHolder.fromName.setText(mailStructures.get(i).getFrom());
        personViewHolder.fromEmail.setText(mailStructures.get(i).getEmail());
        personViewHolder.subject.setText(mailStructures.get(i).getSubject());
    }

    @Override
    public int getItemCount() {
        return mailStructures.size();
    }
}
