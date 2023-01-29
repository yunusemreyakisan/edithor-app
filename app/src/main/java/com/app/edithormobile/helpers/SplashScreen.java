package com.app.edithormobile.helpers;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.app.edithormobile.layouts.login.SignIn;

public class SplashScreen extends AppCompatActivity {
    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent =new Intent(this, SignIn.class);
        startActivity(intent);
        finish();
    }
}
