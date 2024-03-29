package com.app.edithormobile.view.login;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.edithormobile.databinding.ActivitySignUpBinding;
import com.app.edithormobile.util.IToast;
import com.app.edithormobile.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;

public class SignUp extends AppCompatActivity implements IToast {

    ActivitySignUpBinding binding;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    Util util = new Util();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);

        //Methods
        kayitOl();
    }

    //Geri gonderme
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SignUp.this, SignIn.class);
        startActivity(intent);
    }

    //kayit ol
    private void kayitOl() {
        mAuth = FirebaseAuth.getInstance();
        binding.btnKayitOl.setOnClickListener(view -> {
            String name = Objects.requireNonNull(binding.txtKayitAd.getText()).toString();
            String email = Objects.requireNonNull(binding.txtKayitEmail.getText()).toString();
            String password = Objects.requireNonNull(binding.txtKayitSifre.getText()).toString();

            //E-Mail Validation
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
                Toast("Boş bırakılamaz");
            } else if (password.length() < 6) {
                Toast("Şifre 6 karakterden daha uzun olmalı");
            } else if (!email.matches(emailPattern)) {
                Log.i("E-Mail Valid", "Geçerli E-posta girildi.");
                Toast("Geçersiz E-Posta");
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                binding.pBar.setVisibility(View.VISIBLE);
                                //Veritabanına Canlı Kayıt Etme (Realtime Database)
                                String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                                mUser = mAuth.getCurrentUser();
                                mDatabase = FirebaseDatabase.getInstance().getReference()
                                        .child("Kullanicilar")
                                        .child(user_id)
                                        .child("Kullanıcı Bilgileri");

                                String notOlusturmaTarihi = util.olusturmaZamaniGetir(getApplicationContext());

                                HashMap<String, String> mData = new HashMap<>();
                                mData.put("mail", email);
                                mData.put("password", password);
                                mData.put("name", name);
                                mData.put("id", user_id);
                                mData.put("olusturma_tarihi", notOlusturmaTarihi);

                                //Realtime Database
                                mDatabase.setValue(mData).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        binding.pBar.setVisibility(View.GONE);
                                        Intent intent = new Intent(SignUp.this, SignIn.class);
                                        startActivity(intent);
                                        Toast("Hesap oluşturuldu");
                                    } else {
                                        binding.pBar.setVisibility(View.GONE);
                                        Toast("Hesap oluşturulamadı, yeniden deneyin");
                                    }
                                });
                            }
                        });
            }
        });
    }

    @Override
    public void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
