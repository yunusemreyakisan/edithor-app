package com.app.edithormobile.layouts.login;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.edithormobile.R;
import com.google.android.material.textfield.TextInputEditText;

public class SignUp extends AppCompatActivity {

    TextInputEditText ad, email, sifre;
    Button kayitOl, back;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Methods
        initComponents();
        geriGonder();


    }

    //init
    private void initComponents() {
        kayitOl = findViewById(R.id.btnKayitOl);
        ad = findViewById(R.id.txtKayitAd);
        email = findViewById(R.id.txtKayitEmail);
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


}
