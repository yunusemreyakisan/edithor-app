package com.app.edithormobile.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;

import com.app.edithormobile.databinding.ActivitySignInBinding;
import com.app.edithormobile.util.IToast;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.NotePage;
import com.app.edithormobile.viewmodel.login.SignInViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;


public class SignIn extends AppCompatActivity implements IToast {
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    ActivitySignInBinding binding;
    private static final int RC_SIGN_IN = 0;
    private GoogleSignInClient mGoogleSignInClient;

    Util util = new Util();
    SignInViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);
        //ViewModel Bind
        viewModel = ViewModelProviders.of(this).get(SignInViewModel.class);

        //Methods
        kayitOlGonder();
        girisIslemi();
        rememberUser();
    }
    //eof onCreate()

    //onStart() methodu ile kullanicinin onceden giris yapıp yapmadiginin kontrolu
    @Override
    protected void onStart() {
        super.onStart();

    }

    //Back-pressed methodu
    @Override
    public void onBackPressed() {
        //nothing
    }

    //kayit ol'a gonder
    private void kayitOlGonder() {
        binding.btnKayitOl.setOnClickListener(view -> {
            Intent intent = new Intent(SignIn.this, SignUp.class);
            startActivity(intent);
        });

    }

    public void rememberUser() {
        if (viewModel.getSharedPreferences(getApplication())) {
            Intent intent = new Intent(SignIn.this, NotePage.class);
            startActivity(intent);
        }
        binding.beniHatirlaCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                viewModel.setSharedPreference(getApplication(), "true");
                //  Toast.makeText(getApplicationContext(), "Beni hatırla açık!", Toast.LENGTH_SHORT).show();
            } else if (!compoundButton.isChecked()) {
                viewModel.setSharedPreference(getApplication(), "false");
                //  Toast.makeText(getApplicationContext(), "Beni hatırla kapalı!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //E-Mail ve Şifre ile Giriş İşlemi
    private void girisIslemi() {
        binding.btnGirisYap.setOnClickListener(v -> viewModel.kayitliKullaniciGirisi(getApplication(), binding));
    }


    @Override
    public void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
