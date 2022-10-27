package com.app.edithormobile.layouts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.edithormobile.MainActivity;
import com.app.edithormobile.R;
import com.app.edithormobile.models.NoteModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class AddNote extends AppCompatActivity {

    Button btnNotuKaydet, btnBack;
    EditText note;
    private DatabaseReference mDatabase;

    //TODO: AddNot ekranı tasarıma uygun hale getirilecek.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        initComponents();
        notKaydetmeIslevi();
        islemdenVazgec();

    }

    private void islemdenVazgec() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddNote.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void initComponents() {
        note = findViewById(R.id.txtNote);
        btnNotuKaydet = findViewById(R.id.btnNotuKaydet);
        btnBack = findViewById(R.id.btnBack);
    }


    private void notKaydetmeIslevi() {
        btnNotuKaydet.setOnClickListener(view -> {
            //Veritabanına Canlı Kayıt Etme (Realtime Database)
            mDatabase = FirebaseDatabase.getInstance().getReference("Kullanicilar").child("userID").child("notIcerigi");
            //Alan Tanımları
            String notIcerigi = note.getText().toString();
            if (TextUtils.isEmpty(notIcerigi)) {
                Toast.makeText(AddNote.this, "Boş bırakılamaz.", Toast.LENGTH_SHORT).show();
            } else {

                NoteModel mNotes = new NoteModel(notIcerigi);
                mDatabase.push().setValue(mNotes);
                Intent intent = new Intent(AddNote.this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(AddNote.this, "Not başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
            }

        });
    }




}
