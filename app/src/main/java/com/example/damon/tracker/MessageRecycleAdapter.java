package com.example.damon.tracker;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Scanner;

public class MessageRecycleAdapter extends RecyclerView.Adapter<MessageRecycleAdapter.VH> {
    public static class VH extends RecyclerView.ViewHolder{
        private final ImageView imageView;
        private final TextView titleTV;
        private final TextView contentTV;
        private final TextView dateTV;
        public VH(View v) {
            super(v);
            imageView = (ImageView)v.findViewById(R.id.item_message_image);
            titleTV = (TextView)v.findViewById(R.id.item_message_title);
            contentTV = (TextView)v.findViewById(R.id.item_message_content);
            dateTV = (TextView)v.findViewById(R.id.item_message_date);
        }
    }

    private List<String> mDatas;
    public MessageRecycleAdapter(List<String> data){
        this.mDatas = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageRecycleAdapter.VH holder, int position) {
        Scanner in = new Scanner(mDatas.get(position));
        in.useDelimiter(",");
        int imageID = in.nextInt();
        String title = in.next();
        String content = in.next();
        String date = in.next();
        holder.imageView.setImageResource(imageID);
        holder.titleTV.setText(title);
        holder.contentTV.setText(content);
        holder.dateTV.setText(date);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }
}
