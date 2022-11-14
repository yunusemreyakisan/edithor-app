package com.app.edithormobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.edithormobile.adapters.NoteAdapter;
import com.app.edithormobile.layouts.AddNote;
import com.app.edithormobile.layouts.login.SignIn;
import com.app.edithormobile.layouts.upload.UploadFile;
import com.app.edithormobile.models.NoteModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class NotePage extends AppCompatActivity {

    TextView tvNote, tvNoData, tvAddNoteFab, tvUploadFab;
    RecyclerView rvNotes;
    FloatingActionButton fabAddNote, fabUploadFile;
    ExtendedFloatingActionButton fabActions;
    ArrayList<NoteModel> notes;
    NoteAdapter noteAdapter;
    DatabaseReference mDatabaseReference;
    ProgressBar spinner;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Boolean isAllFabsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_page);

        //Methods
        initComponents();
        notesViewRV();
        databaseRef();
        notesEventChangeListener();
        fabControl();

    }

    private void fabControl() {
        fabAddNote.setVisibility(View.GONE);
        tvAddNoteFab.setVisibility(View.GONE);
        fabUploadFile.setVisibility(View.GONE);
        tvUploadFab.setVisibility(View.GONE);

        isAllFabsVisible = false;
        //Baslarken kucuk gosterir.
        fabActions.shrink();
        //extendable click listener
        fabActions.setOnClickListener(
               view -> {
                   if (!isAllFabsVisible) {
                       //fab icerigi goster.
                       fabAddNote.show();
                       fabUploadFile.show();
                       tvAddNoteFab.setVisibility(View.VISIBLE);
                       tvUploadFab.setVisibility(View.VISIBLE);
                       //fab genislesin
                       fabActions.extend();

                       isAllFabsVisible = true;
                   } else {
                       fabAddNote.hide();
                       fabUploadFile.hide();
                       tvAddNoteFab.setVisibility(View.GONE);
                       tvUploadFab.setVisibility(View.GONE);

                       //fab kucultme
                       fabActions.shrink();

                       isAllFabsVisible = false;
                   }
               });


        //Not ekleme FAB
        fabAddNote.setOnClickListener(
                view -> {
                    Intent intent = new Intent(NotePage.this, AddNote.class);
                    startActivity(intent);
                });

        //Fotograf Yukleme
        fabUploadFile.setOnClickListener(
                view -> {
                    Intent intent = new Intent(NotePage.this, UploadFile.class);
                    startActivity(intent);
                    Toast.makeText(NotePage.this, "Under Cons.", Toast.LENGTH_SHORT).show();
                });


    }


    //DB Reference
    private void databaseRef() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference().child("Kullanicilar").child(user_id).child("Notlarim");
    }

    //Recyclerview
    private void notesViewRV() {
        notes = new ArrayList<NoteModel>();
        noteAdapter = new NoteAdapter(NotePage.this, notes);
        rvNotes.setHasFixedSize(true);
        rvNotes.setLayoutManager(new GridLayoutManager(this, 2));
        rvNotes.setAdapter(noteAdapter);
    }

    //init comp.
    private void initComponents() {
        //TV's
        tvAddNoteFab = findViewById(R.id.add_note_tv);
        tvUploadFab = findViewById(R.id.add_file_tv);
        tvNote = findViewById(R.id.tvNote);
        //RV's
        rvNotes = findViewById(R.id.rvNotes);
        spinner = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.no_data);
        //Fabs
        fabAddNote = findViewById(R.id.add_note);
        fabActions = findViewById(R.id.extended_fab);
        fabUploadFile = findViewById(R.id.add_file);
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



    }



    //Degisiklik izleme
    private void notesEventChangeListener() {
        //Child Listener
        spinner.setVisibility(View.VISIBLE);
            mDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    NoteModel model = dataSnapshot.getValue(NoteModel.class);
                    notes.add(model);
                    noteAdapter.notifyItemInserted(notes.size());
                    tvNoData.setVisibility(View.INVISIBLE);
                    spinner.setVisibility(View.GONE);
                }


                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                    //Update
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    notes.remove(dataSnapshot.getValue(NoteModel.class));
                    noteAdapter.notifyDataSetChanged();
                    //empty control
                    if(!notes.isEmpty()) {
                        tvNoData.setVisibility(View.INVISIBLE);
                        noteAdapter.notifyDataSetChanged();
                    }else{
                        tvNoData.setVisibility(View.VISIBLE);
                    }

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

        //empty control
        if(notes.size() == 0) {
            spinner.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        }else{
            tvNoData.setVisibility(View.INVISIBLE);
        }
    }




//elleme dursun
/*
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot != null) {
                        NoteModel model = dataSnapshot.getValue(NoteModel.class);
                        assert model != null;
                        if (model.getNotIcerigi() != null) {
                            notes.add(model);
                            spinner.setVisibility(View.GONE);
                        }
                    }
                }
                noteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Veritabanı hatası!", Toast.LENGTH_SHORT).show();

            }
        });
    }

 */


    //Menu (Search)
    //TODO: Search eklenecek.
}