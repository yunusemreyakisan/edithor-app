package com.app.edithormobile.adapters;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.edithormobile.R;
import com.app.edithormobile.models.NoteModel;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    Context context;
    ArrayList<NoteModel> notes;
    DatabaseReference removeRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

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
            return new NoteHolder(LayoutInflater.from(context).inflate(R.layout.activity_note_item, parent, false));
        } else {
            return new NoteHolder(LayoutInflater.from(context).inflate(R.layout.activity_note_item_without_image, parent, false));
        }

    }


    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        NoteModel mNote = notes.get(position);

        if (getItemViewType(position) == TYPE_IMAGE) {
            holder.tvTitle.setText(mNote.getNotBaslik());
            holder.tvNote.setText(mNote.getNotIcerigi());
            holder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());
            //glide
            Glide.with(context)
                    .load(mNote.getImageUri())
                    .into(holder.imageUri);

            holder.color.setBackgroundColor(mNote.getColor());

            holder.card.setBackgroundColor(mNote.getColor());


            //TODO:Talha hocaya sor. (Referans alma ile alakalı)
            //TODO: Fotoğrafın referansı cihazın kendi depolama alanıyla sınırlı. Storage üzerinden URL alıp göstermeli.


        } else {
            holder.tvTitle.setText(mNote.getNotBaslik());
            //TODO: Eger icerik boyutu 30'dan buyukse sonuna uc nokta koyulmalı.
            holder.tvNote.setText(mNote.getNotIcerigi());
            holder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());
            holder.color.setBackgroundColor(mNote.getColor());
            //holder.card_layout.setBackgroundColor(mNote.getColor());
            //TODO: Eger boyle yaparsak notların arkaplanı değiştirilecek ve mantık aynı olacak.
            holder.card.setStrokeColor(mNote.getColor());

        }

        //Remove reference
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
        removeRef = FirebaseDatabase.getInstance()
                .getReference().child("Kullanicilar").child(user_id).child("Notlarim");

        /*
        //Card Update
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: update edilecek.
                final DialogPlus dialog = DialogPlus.newDialog(context)
                        .setContentHolder(new ViewHolder(R.layout.update_dialog_plus))
                        .setContentBackgroundResource(R.color.bg_color_light)
                        .setExpanded(true, 900)
                        .create();


                View view = dialog.getHolderView();

                Button update = view.findViewById(R.id.btnNotuGuncelle);
                EditText baslik = view.findViewById(R.id.dialogTxtTitle);
                EditText icerik = view.findViewById(R.id.dialogTxtNote);

                baslik.setText(mNote.getNotBaslik());
                icerik.setText(mNote.getNotIcerigi());

                //dialog show
                dialog.show();

                //Update Note
                update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("notBaslik", baslik.getText().toString());
                        map.put("notIcerigi", icerik.getText().toString());

                        //db ref
                        mAuth = FirebaseAuth.getInstance();
                        mUser = mAuth.getCurrentUser();
                        String user_id = requireNonNull(mUser).getUid();
                        FirebaseDatabase.getInstance()
                                .getReference().child("Kullanicilar").child(user_id).child("Notlarim").child(mNote.getNoteID()).updateChildren(map)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            notes.get(holder.getLayoutPosition()).setNotBaslik(String.valueOf(baslik.getText()));
                                            notes.get(holder.getLayoutPosition()).setNotIcerigi(String.valueOf(icerik.getText()));
                                            notifyItemChanged(holder.getLayoutPosition());
                                            notifyDataSetChanged();
                                            Toast.makeText(context, "Notunuz güncellendi", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Hata oluştu", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                    }
                });
            }
        });

         */

    }


    //ViewHolder with images
    public static class NoteHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvNote, tvTitle, tvOlusturmaTarihi;
        public MaterialCardView card;
        ImageView imageUri, color;
        LinearLayout card_layout;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvOlusturmaTarihi = itemView.findViewById(R.id.tvOlusturmaTarihi);
            imageUri = itemView.findViewById(R.id.imageUri);
            color = itemView.findViewById(R.id.notColor);
            card_layout = itemView.findViewById(R.id.card_layout);

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


}

