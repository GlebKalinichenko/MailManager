package com.example.gleb.mailmanager.recyclerview;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
    private Context context;

    public RVAdapter(List<MailStructure> mailStructures, Context context){
        this.mailStructures = mailStructures;
        this.context = context;
    }

    public static class MailViewHolder extends RecyclerView.ViewHolder {
        TextView fromName;
        TextView fromEmail;
        TextView contentMail;
        TextView subject;
        TextView photoUser;
        ImageView attachImageView;
        Context context;

        MailViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;
            fromName = (TextView)itemView.findViewById(R.id.fromName);
//            fromEmail = (TextView)itemView.findViewById(R.id.fromEmail);
            contentMail = (TextView)itemView.findViewById(R.id.content);
            subject = (TextView)itemView.findViewById(R.id.subject);
            photoUser = (TextView)itemView.findViewById(R.id.photoUser);
            attachImageView = (ImageView)itemView.findViewById(R.id.attachImageView);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public MailViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_mail_item, viewGroup, false);
        MailViewHolder pvh = new MailViewHolder(v, context);
        return pvh;
    }

    @Override
    public void onBindViewHolder(MailViewHolder personViewHolder, int i) {
        personViewHolder.fromName.setText(mailStructures.get(i).getFrom());
        personViewHolder.contentMail.setText(mailStructures.get(i).getContent());
//        personViewHolder.fromEmail.setText(mailStructures.get(i).getEmail());
        personViewHolder.subject.setText(mailStructures.get(i).getSubject());
        personViewHolder.photoUser.setText(mailStructures.get(i).getFrom().substring(0, 1));

        View v = personViewHolder.itemView.findViewById(R.id.photoUser);

        LayerDrawable bgDrawable = (LayerDrawable)v.getBackground();
        final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.shape_id);

        if (i % 2 == 0) {
            shape.setColor(context.getResources().getColor(R.color.colorAccent));
        }
        else{
            if (i % 3 == 0){
                shape.setColor(context.getResources().getColor(R.color.colorPrimary));
            }
            else{
                shape.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
            }
        }

        if (!isTheSame(mailStructures.get(i).getAttachFiles(), new String[]{""})){
            personViewHolder.attachImageView.setVisibility(ImageView.VISIBLE);
        }
        else{
            personViewHolder.attachImageView.setVisibility(ImageView.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mailStructures.size();
    }

    public boolean isTheSame(String[] arr1, String[] arr2) {
        if (arr1.length != arr2.length) return false;
        for (int i = 0; i < arr1.length; i++)
            if (!arr1[i].equals(arr2[i]))
                return false;
        return true;
    }
}
