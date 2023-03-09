package com.app.edithormobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.edithormobile.R;
import com.app.edithormobile.models.GPTModel;

import java.util.ArrayList;

public final class MessageAdapter extends RecyclerView.Adapter {
    private ArrayList<GPTModel> messages;
    private static final int TYPE_USER = 1;
    private static final int TYPE_GPT = 2;


    public MessageAdapter(ArrayList<GPTModel> messages) {
        this.messages = messages;
    }

    //VH
    public static class VH extends RecyclerView.ViewHolder {
        LinearLayout leftChatView, rightChatView;
        TextView leftTextView, rightTextView, kullanici_adi;

        public VH(@NonNull View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            leftTextView = itemView.findViewById(R.id.left_chat_text_view);
            rightTextView = itemView.findViewById(R.id.right_chat_text_view);
            kullanici_adi = itemView.findViewById(R.id.chat_kullanici_adi);

        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String message = (String) messages.get(position).getMessage();

        if (!messages.get(position).getSender().equals("gpt")) {
            ((VH) holder).leftChatView.setVisibility(View.GONE);
            ((VH) holder).rightChatView.setVisibility(View.VISIBLE);
            ((VH) holder).rightTextView.setText(message);
            ((VH) holder).kullanici_adi.setText(messages.get(position).getSender());
        } else {
            ((VH) holder).rightChatView.setVisibility(View.GONE);
            ((VH) holder).leftChatView.setVisibility(View.VISIBLE);
            ((VH) holder).leftTextView.setText(message);
        }


        //click listener (TODO: GPT mesajına tıkladıgında kopyalanacak.)
        ((VH) holder).leftChatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (messages.get(position).getSender()) {
            case "user":
                return TYPE_USER;
            case "gpt":
                return TYPE_GPT;
        }
        return position;
    }
}