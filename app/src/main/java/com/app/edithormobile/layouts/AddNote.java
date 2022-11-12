package com.app.edithormobile.layouts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.edithormobile.NotePage;
import com.app.edithormobile.R;
import com.app.edithormobile.models.NoteModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class AddNote extends AppCompatActivity {

    Button btnNotuKaydet, btnBack;
    EditText note, title;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        initComponents();
        notKaydetmeIslevi();
        islemdenVazgec();

        //deger alma (update first step)
        title.setText(getIntent().getStringExtra("baslik"));
        note.setText(getIntent().getStringExtra("icerik"));

    }
    private void islemdenVazgec() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddNote.this, NotePage.class);
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
                int hours = calendar.get(Calendar.HOUR);
                int minutes = calendar.get(Calendar.MINUTE);
                String time = String.format("%02d:%02d", hours, minutes);
                String notOlusturmaTarihi = calendar.get(Calendar.DAY_OF_MONTH) + "/" +  month
                        + " " + time ;

                //unique getKey()
                String id = mDatabase.push().getKey();
                NoteModel mNotes = new NoteModel(id, notIcerigi, notBaslik, notOlusturmaTarihi, false);
                assert id != null;
                mDatabase.child(id).setValue(mNotes);

                Intent intent = new Intent(AddNote.this, NotePage.class);
                startActivity(intent);
                Toast.makeText(AddNote.this, "Not başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
            }
        });
    }




}
