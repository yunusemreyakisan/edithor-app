package com.app.edithormobile.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.edithormobile.R;
import com.app.edithormobile.layouts.AddNote;
import com.app.edithormobile.models.NoteModel;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {
    //TODO: iki çeşit gösterim olacak. (with images and without images)
    Context context;
    ArrayList<NoteModel> notes;
    DatabaseReference removeRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    private static int TYPE_IMAGE = 1;
    private static int TYPE_TEXT = 2;


    //Constructor
    public NoteAdapter(Context context, ArrayList<NoteModel> notes) {
        this.context = context;
        this.notes = notes;
    }


    //ViewHolder with images
    public static class NoteHolder extends RecyclerView.ViewHolder {
        TextView tvNote, tvTitle, tvOlusturmaTarihi;
        CardView card;
        ImageView imageUri;


        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvOlusturmaTarihi = itemView.findViewById(R.id.tvOlusturmaTarihi);
            imageUri = itemView.findViewById(R.id.imageUri);

        }

    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       if (viewType == TYPE_IMAGE) {
            return new NoteHolder(LayoutInflater.from(context).inflate(R.layout.activity_note_item, parent, false));
        } else {
            return new NoteHolder(LayoutInflater.from(context).inflate(R.layout.activity_note_item_without_image, parent, false));
        }





/*
        View view = LayoutInflater.from(context).inflate(R.layout.activity_note_item, parent, false);
        return new NoteHolder(view);


 */




       /*
        View view = null;
        // check here the viewType and return RecyclerView.ViewHolder based on view type
        if (viewType == ITEM_TYPE_TWO) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_note_item, parent, false);
            return new NoteHolder(view);
        } else if (viewType == ITEM_TYPE_ONE) {
            view = LayoutInflater.from(context).inflate(R.layout.button_two, parent, false);
            return new NoteHolderWithoutImages(view);
        }else {
            return  null;
        }

        */

    }


    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        NoteModel mNote = notes.get(position);

        if(getItemViewType(position) == TYPE_IMAGE){
            holder.tvTitle.setText(mNote.getNotBaslik());
            holder.tvNote.setText(mNote.getNotIcerigi());
            holder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());
            Bitmap bitmap = BitmapFactory.decodeFile(mNote.getImageUri());
            holder.imageUri.setImageBitmap(bitmap);

            //glide
            Glide.with(context)
                    .load(mNote.getImageUri())
                    .into(holder.imageUri);
        }else{
            holder.tvTitle.setText(mNote.getNotBaslik());
            holder.tvNote.setText(mNote.getNotIcerigi());
            holder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());
        }



/*
        NoteHolder noteHolder = (NoteHolder) holder;
        noteHolder.
                noteHolder.tvNote.setText(mNote.getNotIcerigi());
        noteHolder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());


        // holder.imageUri.setImageURI(Uri.parse(mNote.getImageUri()));

 */


        //Long press remove item
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        removeRef = FirebaseDatabase.getInstance()
                .getReference().child("Kullanicilar").child(user_id).child("Notlarim").child(mNote.getNoteID());

        //long delete

        holder.card.setOnLongClickListener(v -> {
          /*  notes.get(position).setSelected(true);
            holder.card.setCardBackgroundColor(Color.BLUE);
            notifyDataSetChanged();

           */

          


                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Emin misiniz?");
                builder.setMessage("Notu silmek istediğinize emin misiniz?");
                builder.setNegativeButton("Hayır", (dialog, which) -> Toast.makeText(context, "Vazgeçildi.", Toast.LENGTH_SHORT).show());
                builder.setPositiveButton("Evet", (dialogInterface, i) -> {
                    removeRef.setValue(null);
                    notes.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Notunuz silindi.", Toast.LENGTH_SHORT).show();
                });
                builder.show();


            return true;
        });

        //Veri alma
        holder.card.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddNote.class);
            intent.putExtra("baslik", mNote.getNotBaslik());
            intent.putExtra("icerik", mNote.getNotIcerigi());
            intent.putExtra("image", mNote.getImageUri());
            context.startActivity(intent);

            //Update

        });

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
        // below line is to add our filtered
        // list in our course array list.
        notes = filterlist;
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged();
    }


}
