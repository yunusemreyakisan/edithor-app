package com.app.edithormobile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.edithormobile.adapters.NoteAdapter;
import com.app.edithormobile.layouts.AddNote;
import com.app.edithormobile.layouts.login.SignIn;
import com.app.edithormobile.models.NoteModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    TextView tvNote, noData;
    RecyclerView rvNotes;
    FloatingActionButton fabAddNote;
    ArrayList<NoteModel> notes;
    NoteAdapter noteAdapter;
    DatabaseReference mDatabaseReference;
    ProgressBar spinner;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    //TODO: Başlık ve tarih zamanı çekilecek.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Methods
        initComponents();
        notesViewRV();
        databaseRef();
        notesEventChangeListener();
        noteEkleyeGit();

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
        noteAdapter = new NoteAdapter(MainActivity.this, notes);
        rvNotes.setHasFixedSize(true);
        rvNotes.setLayoutManager(new GridLayoutManager(this, 2));
        rvNotes.setAdapter(noteAdapter);
    }

    //init comp.
    private void initComponents() {
        tvNote = findViewById(R.id.tvNote);
        rvNotes = findViewById(R.id.rvNotes);
        fabAddNote = findViewById(R.id.add_fab);
        spinner = findViewById(R.id.progressBar);
        noData = findViewById(R.id.no_data);
    }

    //Back Pressed
    @Override
    public void onBackPressed() {
        //Toast.makeText(MainActivity.this, "Back pressed", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        // Pencere Baslik Tanımı
        builder.setTitle("Emin misiniz?");
        // Pencere Mesaj Tanımı
        builder.setMessage("Çıkış yapmak istediğinizden emin misiniz?");

        class AlertDialogClickListener implements DialogInterface.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialog.BUTTON_NEGATIVE) {
                    Toast.makeText(MainActivity.this, "İşlem iptal edildi.",
                            Toast.LENGTH_SHORT).show();
                } else if (which == AlertDialog.BUTTON_POSITIVE) { // veya else
                    Toast.makeText(MainActivity.this, "Çıkış başarıyla gerçekleştirildi.",
                            Toast.LENGTH_SHORT).show();
                    MainActivity.this.finish(); // Activity’nin sonlandırılması

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
            spinner.setVisibility(View.INVISIBLE);
            noData.setVisibility(View.VISIBLE);
            mDatabaseReference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                    NoteModel model = dataSnapshot.getValue(NoteModel.class);
                    notes.add(model);
                    noteAdapter.notifyItemInserted(notes.size());
                    noData.setVisibility(View.INVISIBLE);
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
                        noData.setVisibility(View.INVISIBLE);
                        noteAdapter.notifyDataSetChanged();
                    }else{
                        noData.setVisibility(View.VISIBLE);
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


    private void noteEkleyeGit() {
        fabAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddNote.class);
            startActivity(intent);
        });
    }


    //Menu (Search)
    //TODO: Search eklenecek.
}