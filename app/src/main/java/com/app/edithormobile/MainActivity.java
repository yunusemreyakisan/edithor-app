package com.app.edithormobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.edithormobile.adapters.NoteAdapter;
import com.app.edithormobile.layouts.AddNote;
import com.app.edithormobile.models.NoteModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TextView tvNote;
    RecyclerView rvNotes;
    FloatingActionButton fabAddNote;
    ArrayList<NoteModel> notes;
    NoteAdapter noteAdapter;
    DatabaseReference mDatabaseReference;

    //TODO: Başlık ve tarih zamanı çekilecek.
    //TODO:

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();

        notes = new ArrayList<NoteModel>();
        noteAdapter = new NoteAdapter(MainActivity.this, notes);

        rvNotes.setHasFixedSize(true);
        rvNotes.setLayoutManager(new GridLayoutManager(this, 2));
        rvNotes.setAdapter(noteAdapter);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Kullanicilar").child("userID").child("notIcerigi");

        NotesEventChangeListener();
        noteEkleyeGit();

    }

    //init comp.
    private void initComponents() {
        tvNote = findViewById(R.id.tvNote);
        rvNotes = findViewById(R.id.rvNotes);
        fabAddNote = findViewById(R.id.add_fab);
    }

    //Back Pressed
    @Override
    public void onBackPressed() {
        Toast.makeText(MainActivity.this, "Back pressed", Toast.LENGTH_SHORT).show();
    }



    private void NotesEventChangeListener() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot != null) {
                        NoteModel model = dataSnapshot.getValue(NoteModel.class);
                        assert model != null;
                        if (model.getNotIcerigi() != null) {
                            notes.add(model);
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



    private void noteEkleyeGit() {
        fabAddNote.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddNote.class);
            startActivity(intent);
        });
    }






}