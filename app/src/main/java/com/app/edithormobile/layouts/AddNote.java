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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Random;

public class AddNote extends AppCompatActivity {

    Button btnNotuKaydet, btnBack;
    EditText note, title;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;


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
        title = findViewById(R.id.txtTitle);
    }


    private void notKaydetmeIslevi() {
        mAuth = FirebaseAuth.getInstance();
        btnNotuKaydet.setOnClickListener(view -> {
            //Veritabanına Canlı Kayıt Etme (Realtime Database)
            String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            //TODO:Silme islemi yapilacak.
            mUser = mAuth.getCurrentUser();
            mDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("Kullanicilar").child(user_id).child("Notlarim");

            //Alan Tanımları
            String notIcerigi = note.getText().toString();
            String notBaslik = title.getText().toString();

            if (TextUtils.isEmpty(notIcerigi)) {
                Toast.makeText(AddNote.this, "Not içeriği giriniz.", Toast.LENGTH_SHORT).show();
            }else if(TextUtils.isEmpty(notBaslik)){
                Toast.makeText(AddNote.this, "Başlık boş bırakılamaz.", Toast.LENGTH_SHORT).show();
            } else {
                //Olusturma zamanini al.
                Calendar calendar = new GregorianCalendar();
                int month = calendar.get(Calendar.MONTH) + 1; //0 ile basladigi icin 1 eklendi.
                String notOlusturmaTarihi = calendar.get(Calendar.DAY_OF_MONTH) + "/" +  month
                        + " " +calendar.get(Calendar.HOUR_OF_DAY) + ":" +  calendar.get(Calendar.MINUTE) ;

                //Essiz anahtar gerekiyor. (for remove)
                String id = mDatabase.push().getKey();
                NoteModel mNotes = new NoteModel(id, notIcerigi, notBaslik, notOlusturmaTarihi, false);
                mDatabase.push().setValue(mNotes);

                Intent intent = new Intent(AddNote.this, MainActivity.class);
                startActivity(intent);
                Toast.makeText(AddNote.this, "Not başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
            }
        });
    }




}
