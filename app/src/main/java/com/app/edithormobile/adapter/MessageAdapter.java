package com.app.edithormobile.adapter;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.edithormobile.R;
import com.app.edithormobile.model.GPTModel;

import java.util.ArrayList;

public final class MessageAdapter extends RecyclerView.Adapter {
    private ArrayList<GPTModel> messages;
    private static final int TYPE_USER = 1;
    private static final int TYPE_GPT = 2;
    Context context;


    public MessageAdapter(ArrayList<GPTModel> messages) {
        this.messages = messages;
    }

    //VH
    public static class VH extends RecyclerView.ViewHolder {
        ConstraintLayout leftChatView;
        LinearLayout rightChatView;
        TextView leftTextView, rightTextView, kullanici_adi;
        ImageButton kopyala;

        public VH(@NonNull View itemView) {
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            leftTextView = itemView.findViewById(R.id.left_chat_text_view);
            rightTextView = itemView.findViewById(R.id.right_chat_text_view);
            kullanici_adi = itemView.findViewById(R.id.chat_kullanici_adi);
            kopyala = itemView.findViewById(R.id.copy_chatgpt_response);

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
        ((VH) holder).kopyala.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCopiedObject(v.getContext(), ((VH) holder).leftTextView.getText().toString());
            }
        });

        //TODO: Click listener için interface oluştur, viewmodel üzerinden kopyalanan içeriği notlara kaydet.

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

    //İçeriği kopyala
    public void getCopiedObject(Context context, String icerik) {
        // ClipboardManager nesnesini al
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        // Metin değerini kopyala
        ClipData clip = ClipData.newPlainText("label", icerik);
        clipboard.setPrimaryClip(clip);
    }
}