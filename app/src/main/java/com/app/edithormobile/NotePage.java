package com.app.edithormobile;

import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.edithormobile.adapters.NoteAdapter;
import com.app.edithormobile.databinding.ActivityNotePageBinding;
import com.app.edithormobile.layouts.AddNote;
import com.app.edithormobile.layouts.login.SignIn;
import com.app.edithormobile.layouts.upload.UploadFile;
import com.app.edithormobile.models.NoteModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * @author yunusemreyakisan
 */

public class NotePage extends AppCompatActivity{

    ArrayList<NoteModel> notes;
    NoteAdapter noteAdapter;
    DatabaseReference mDatabaseReference;
    DatabaseReference removeRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Boolean isAllFabsVisible;

    NoteAdapter.ClickListener clickListener;

    ActivityNotePageBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);

        binding = ActivityNotePageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        binding.noData.setVisibility(View.INVISIBLE);
        binding.notFound.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        //Methods
        databaseRef();
        notesViewRV();
        notesEventChangeListener();
        fabControl();
        search(view);


        //TODO: Dialogplus kullanarak fotograf ve galeri seçimini yaptır.

    }


    @SuppressLint("ClickableViewAccessibility")
    private void fabControl() {
        binding.addNote.setVisibility(View.GONE);
        binding.addNoteTv.setVisibility(View.GONE);
        binding.addFile.setVisibility(View.GONE);
        binding.addFileTv.setVisibility(View.GONE);

        isAllFabsVisible = false;
        //Baslarken kucuk gosterir.
        binding.extendedFab.shrink();
        //extendable click listener
        binding.extendedFab.setOnClickListener(
                view -> {
                    if (!isAllFabsVisible) {
                        binding.addNoteTv.bringToFront();
                        binding.addFileTv.bringToFront();
                        //fab icerigi goster.
                        binding.addNote.show();
                        binding.addFile.show();
                        binding.addNoteTv.setVisibility(View.VISIBLE);
                        binding.addFileTv.setVisibility(View.VISIBLE);
                        //fab genislesin
                        binding.extendedFab.extend();

                        //TODO: FAB açıldığında arkaplanın solması gerekiyor.

                        isAllFabsVisible = true;
                    } else {
                        binding.addNote.hide();
                        binding.addFile.hide();
                        binding.addNoteTv.setVisibility(View.GONE);
                        binding.addFileTv.setVisibility(View.GONE);

                        //fab kucultme
                        binding.extendedFab.shrink();

                        isAllFabsVisible = false;
                    }
                });

        //fab shrink anywhere in screen
        binding.rvNotes.setOnTouchListener((view, motionEvent) -> {
            binding.addNote.setVisibility(View.GONE);
            binding.addNoteTv.setVisibility(View.GONE);
            binding.addFile.setVisibility(View.GONE);
            binding.addFileTv.setVisibility(View.GONE);

            isAllFabsVisible = false;
            binding.extendedFab.shrink();
            return false;
        });


        //Not ekleme FAB
        binding.addNote.setOnClickListener(
                view -> {
                    Intent intent = new Intent(NotePage.this, AddNote.class);
                    startActivity(intent);
                });

        //Fotograf Yukleme
        binding.addFile.setOnClickListener(
                view -> {
                    Intent intent = new Intent(NotePage.this, UploadFile.class);
                    startActivity(intent);
                });


    }


    //DB Reference
    private void databaseRef() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mUser).getUid();
        mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Kullanicilar").child(user_id).child("Notlarim");
    }

    //Recyclerview
    private void notesViewRV() {
        //Remove reference
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
        removeRef = FirebaseDatabase.getInstance()
                .getReference().child("Kullanicilar").child(user_id).child("Notlarim");

        notes = new ArrayList<NoteModel>();
        noteAdapter = new NoteAdapter(NotePage.this, notes,  new NoteAdapter.ClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(NotePage.this, "Kısa basıldı", Toast.LENGTH_SHORT).show();



            }

            @Override
            public void onItemLongClick(View v, int position) {
                String id = notes.get(position).getNoteID();

                AlertDialog.Builder builder = new AlertDialog.Builder(NotePage.this);
                builder.setTitle("Emin misiniz?");
                builder.setMessage("Notu silmek istediğinizden emin misiniz?");
                builder.setPositiveButton("EVET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    notes.remove(notes.get(position));
                                    noteAdapter.notifyItemRemoved(position);
                                    noteAdapter.notifyDataSetChanged();

                                    Toast.makeText(NotePage.this, "Notunuz silindi.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(e -> Toast.makeText(NotePage.this, "Hata olustu", Toast.LENGTH_SHORT).show());
                    }
                });
                builder.setNegativeButton("HAYIR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(NotePage.this, "Vazgeçildi", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        });
        binding.rvNotes.setHasFixedSize(true);
        binding.rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        binding.rvNotes.setAdapter(noteAdapter);
    }




    //TODO: Yukarıdaki Click.Listener arayüzünü method içerisine al.
    private NoteAdapter.ClickListener removeNoteByID() {
        return clickListener;
    }


    //Back Pressed
    @Override
    public void onBackPressed() {
        //Toast.makeText(MainActivity.this, "Back pressed", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(NotePage.this);
        // Pencere Baslik Tanımı
        builder.setTitle("Emin misiniz?");
        // Pencere Mesaj Tanımı
        builder.setMessage("Çıkış yapmak istediğinizden emin misiniz?");

        class AlertDialogClickListener implements DialogInterface.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialog.BUTTON_NEGATIVE) {
                    Toast.makeText(NotePage.this, "İşlem iptal edildi.",
                            Toast.LENGTH_SHORT).show();
                } else if (which == AlertDialog.BUTTON_POSITIVE) { // veya else
                    Toast.makeText(NotePage.this, "Çıkış başarıyla gerçekleştirildi.",
                            Toast.LENGTH_SHORT).show();
                    NotePage.this.finish(); // Activity’nin sonlandırılması

                    //Giriş ekranı için Pref. Kontrolü
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("Remember", "false");
                    editor.apply();
                    finish();

                    //Giriş aktivitesine dönülmesi
                    Intent intent = new Intent(getApplicationContext(), SignIn.class);
                    startActivity(intent);

                }
            }
        }

        // AlertDialog Builder
        AlertDialogClickListener alertDialogClickListener = new AlertDialogClickListener();
        builder.setPositiveButton("EVET", alertDialogClickListener);
        builder.setNegativeButton("HAYIR", alertDialogClickListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.button_active_color));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.button_active_color));
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
    }


    //Degisiklik izleme
    private void notesEventChangeListener() {
        //Child Listener
        bosKontrolu();
        binding.progressBar.setVisibility(View.VISIBLE);
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                NoteModel model = dataSnapshot.getValue(NoteModel.class);
                notes.add(model);
                noteAdapter.notifyItemInserted(notes.size());
                noteAdapter.notifyDataSetChanged();
                bosKontrolu();
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                //Update
                bosKontrolu();
                noteAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //Intent
                bosKontrolu();

                Log.d("note size", String.valueOf(notes.size()));

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                //Updated

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Veritabanı hatası!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //bos kontrolu
    private void bosKontrolu() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mUser).getUid();
        mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Kullanicilar").child(user_id).child("Notlarim");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get NoteModel object
                NoteModel model = dataSnapshot.getValue(NoteModel.class);

                if (model != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    noteAdapter.notifyDataSetChanged();
                } else {
                    binding.noData.setVisibility(View.VISIBLE);
                    binding.notFound.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Handle-Error
            }

        };
        mDatabaseReference.addValueEventListener(postListener);

    }


    //Menu (Search)
    private void search(View view) {
        //arama islemi
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return false;
            }
        });
    }

    private void filter(String text) {
        // creating a new array list to filter our data.
        ArrayList<NoteModel> filteredlist = new ArrayList<>();

        // running a for loop to compare elements.
        for (NoteModel item : notes) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.getNotBaslik().toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(this, "Veri bulunamadı.", Toast.LENGTH_SHORT).show();
        } else {
            noteAdapter.filterList(filteredlist);
        }
    }

}