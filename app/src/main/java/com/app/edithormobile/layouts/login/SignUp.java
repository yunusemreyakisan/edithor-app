package com.app.edithormobile.layouts.login;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.edithormobile.MainActivity;
import com.app.edithormobile.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    private HashMap<String, Object> mData;
    TextInputEditText ad, emailadresi, sifre;
    Button kayitOl, back;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Methods
        initComponents();
        geriGonder();
        btnKayitOlIslevi();


    }

    //init
    private void initComponents() {
        kayitOl = findViewById(R.id.btnKayitOl);
        ad = findViewById(R.id.txtKayitAd);
        emailadresi = findViewById(R.id.txtKayitEmail);
        sifre = findViewById(R.id.txtKayitSifre);
        back = findViewById(R.id.btnBack);
    }


    //geri gonder
    private void geriGonder() {
        back.setOnClickListener(view -> {
            Intent intent = new Intent(SignUp.this, SignIn.class);
            startActivity(intent);
        });
    }


    private void btnKayitOlIslevi() {
        mAuth = FirebaseAuth.getInstance();
        kayitOl.setOnClickListener(view -> {
            String name = ad.getText().toString();
            String email = emailadresi.getText().toString();
            String password = sifre.getText().toString();

            //E-Mail Validation
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";


            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
                Toast.makeText(SignUp.this, "Boş bırakılamaz.", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(SignUp.this, "Şifre 6 karakterden daha uzun olmalı.", Toast.LENGTH_SHORT).show();
            } else if (!email.matches(emailPattern)) {
                Log.i("E-Mail Valid", "Geçerli E-posta girildi.");
                Toast.makeText(getApplicationContext(), "Geçersiz E-Posta", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Veritabanına Canlı Kayıt Etme (Realtime Database)
                                    String user_id = mAuth.getCurrentUser().getUid();
                                    mUser = mAuth.getCurrentUser();
                                    mDatabase = FirebaseDatabase.getInstance().getReference()
                                            .child("Kullanicilar")
                                            .child(user_id)
                                            .child("Kullanıcı Bilgileri");

                                    HashMap<String, String> mData = new HashMap<>();
                                    mData.put("E-Mail", email);
                                    mData.put("Password", password);
                                    mData.put("Name", name);

                                    //Realtime Database
                                    mDatabase.setValue(mData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                                startActivity(intent);
                                                Toast.makeText(SignUp.this, "Hesap oluşturuldu.", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Toast.makeText(SignUp.this, "Hesap oluşturulamadı, yeniden deneyin.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }







}
