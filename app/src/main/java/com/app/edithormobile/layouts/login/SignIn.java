package com.app.edithormobile.layouts.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.edithormobile.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignIn extends AppCompatActivity {

    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    TextInputEditText email, sifre;
    CheckBox hatirla;
    Button giris, googleGiris, kayitOl;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //Methods
        initComponents();
        kayitOlGonder();


    }

    //init
    private void initComponents() {
    email = findViewById(R.id.txtGrsEmail);
    sifre = findViewById(R.id.txtGrsSifre);
    hatirla = findViewById(R.id.beniHatirlaCheckbox);
    giris = findViewById(R.id.btnGirisYap);
    googleGiris = findViewById(R.id.btnGoogle);
    kayitOl = findViewById(R.id.btnKayitOl);
    }


    //kayit ol'a gonder
    private void kayitOlGonder() {
        kayitOl.setOnClickListener(view -> {
            Intent intent = new Intent(SignIn.this, SignUp.class);
            startActivity(intent);
        });
    }





}
