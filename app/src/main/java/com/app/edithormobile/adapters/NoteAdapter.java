package com.app.edithormobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.app.edithormobile.R;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.util.Util;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    Context context;
    ArrayList<NoteModel> notes;
    DatabaseReference removeRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Util util = new Util();

    static ClickListener clickListener;

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_TEXT = 2;


    //Constructor
    public NoteAdapter(Context context, ArrayList<NoteModel> notes, ClickListener clickListener) {
        this.context = context;
        this.notes = notes;
        NoteAdapter.clickListener = clickListener;
    }


    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_IMAGE) {
            return new NoteHolder(LayoutInflater.from(context).inflate(R.layout.note_item, parent, false));
        } else {
            return new NoteHolder(LayoutInflater.from(context).inflate(R.layout.note_item_without_image, parent, false));
        }
    }


    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        NoteModel mNote = notes.get(position);

        if (getItemViewType(position) == TYPE_IMAGE) {
            holder.tvTitle.setText(util.bosKontrolluDeger(mNote.getNotBaslik()));
            holder.tvNote.setText(util.bosKontrolluDeger(mNote.getNotIcerigi()));
            holder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());

            // Placeholder
            CircularProgressDrawable drawable = new CircularProgressDrawable(context);
            drawable.setCenterRadius(40f);
            drawable.setStrokeWidth(8f);
            drawable.start();
            // Glide
            Glide.with(context)
                    .load(mNote.getImageUri())
                    .centerCrop()
                    .placeholder(drawable)
                    .into(holder.imageUri);


            //holder.color.setBackgroundColor(mNote.getColor());
            // holder.card.setBackgroundColor(mNote.getColor());
            holder.card.setStrokeColor(mNote.getColor());


            //TODO:Talha hocaya sor. (Referans alma ile alakalı)
            //TODO: Fotoğrafın referansı cihazın kendi depolama alanıyla sınırlı. Storage üzerinden URL alıp göstermeli.


        } else {
            holder.tvTitle.setText(util.bosKontrolluDeger(mNote.getNotBaslik()));
            //TODO: Eger icerik boyutu 30'dan buyukse sonuna uc nokta koyulmalı.
            holder.tvNote.setText(util.bosKontrolluDeger(mNote.getNotIcerigi()));
            holder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());
            // holder.color.setBackgroundColor(mNote.getColor());
            //holder.card_layout.setBackgroundColor(mNote.getColor());
            //TODO: Eger boyle yaparsak notların arkaplanı değiştirilecek ve mantık aynı olacak.
            holder.card.setStrokeColor(mNote.getColor());

        }

    }


    //ViewHolder with images
    public static class NoteHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvNote, tvTitle, tvOlusturmaTarihi;
        public MaterialCardView card;
        ImageView imageUri;
        LinearLayout card_layout;
        public ProgressBar bar;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            //TODO: İki farklı kart düzenimiz var, düzenlerin long click methodunda hata var, çözülecek.
            card = itemView.findViewById(R.id.card);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvOlusturmaTarihi = itemView.findViewById(R.id.tvOlusturmaTarihi);
            imageUri = itemView.findViewById(R.id.imageUri);
            card_layout = itemView.findViewById(R.id.card_layout);
            bar = itemView.findViewById(R.id.progressForImage);

            //click
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int positionID = getAdapterPosition();
            v = card;
            if (positionID >= 0) {
                clickListener.onItemClick(v, positionID);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getAdapterPosition();
            v = card;
            if (position >= 0) {
                clickListener.onItemLongClick(v, position);
                return true;
            }
            return false;
        }
    }


    //Interface
    public interface ClickListener {
        void onItemClick(View v, int position);

        void onItemLongClick(View v, int position);
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (notes.get(position).getImageUri() != null) {
            return TYPE_IMAGE;
        } else {
            return TYPE_TEXT;
        }
    }


    // method for filtering our recyclerview items.
    public void filterList(ArrayList<NoteModel> filterlist) {
        notes = filterlist;
        notifyDataSetChanged();
    }

    //Listeyi Guncelleme
    public void listeyiGuncelle(ArrayList<NoteModel> list) {
        this.notes = list;
        notifyDataSetChanged();
    }


}

