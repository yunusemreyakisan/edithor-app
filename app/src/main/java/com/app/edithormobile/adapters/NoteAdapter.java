package com.app.edithormobile.adapters;

import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.edithormobile.R;
import com.app.edithormobile.models.NoteModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;

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
        } else {
            holder.tvTitle.setText(mNote.getNotBaslik());
            holder.tvNote.setText(mNote.getNotIcerigi());
            holder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());
        }

        //Remove reference
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
        removeRef = FirebaseDatabase.getInstance()
                .getReference().child("Kullanicilar").child(user_id).child("Notlarim");

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
                                        if(task.isSuccessful()){
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


        //Delete Data from Realtime DB
        holder.card.setOnLongClickListener(v1 -> {
            String id = notes.get(position).getNoteID();
            //Toast.makeText(MainActivity.this, "Back pressed", Toast.LENGTH_SHORT).show();
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            // Pencere Baslik Tanımı
            builder.setTitle("Emin misiniz?");
            // Pencere Mesaj Tanımı
            builder.setMessage("Notu silmek istediğinize emin misiniz?");

            class AlertDialogClickListener implements DialogInterface.OnClickListener {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) {
                        Toast.makeText(context, "İşlem iptal edildi.",
                                Toast.LENGTH_SHORT).show();
                    } else if (which == androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) { // veya else
                        removeRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    notes.remove(notes.get(holder.getLayoutPosition()));
                                    notifyItemRemoved(holder.getLayoutPosition());
                                    notifyDataSetChanged();
                                    Toast.makeText(context, "Notunuz silindi.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Hata olustu", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            // AlertDialog Builder
            AlertDialogClickListener alertDialogClickListener = new AlertDialogClickListener();
            builder.setPositiveButton("EVET", alertDialogClickListener);
            builder.setNegativeButton("HAYIR", alertDialogClickListener);
            androidx.appcompat.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.button_active_color));
            alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.button_active_color));

            return true;
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
        notes = filterlist;
        notifyDataSetChanged();
    }



}

