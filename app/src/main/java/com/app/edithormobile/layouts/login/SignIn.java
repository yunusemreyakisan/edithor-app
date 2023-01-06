package com.app.edithormobile.layouts.login;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.edithormobile.NotePage;
import com.app.edithormobile.R;
import com.app.edithormobile.databinding.ActivitySignInBinding;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

import io.reactivex.rxjava3.core.Single;


public class SignIn extends AppCompatActivity {

    FirebaseAuth mAuth;
    ActivitySignInBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);


        //Methods
        kayitOlGonder();
        girisIslemi();
        beniHatirla();


        //TODO: UpdateUI methodu eklenmeli, for sign in google.

    }

    //Toast Method
    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


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


    //Shared Preferences (Beni Hatırla)
    private void beniHatirla() {
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = preferences.getString("Remember", "");

        if (checkbox.equals("true")) {
            Intent intent = new Intent(SignIn.this, NotePage.class);
            startActivity(intent);
        }


        binding.beniHatirlaCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                SharedPreferences preferences1 = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.putString("Remember", "true");
                editor.apply();
                //  Toast.makeText(getApplicationContext(), "Beni hatırla açık!", Toast.LENGTH_SHORT).show();
            } else if (!compoundButton.isChecked()) {
                SharedPreferences preferences1 = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.putString("Remember", "false");
                editor.apply();
                //  Toast.makeText(getApplicationContext(), "Beni hatırla kapalı!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //E-Mail ve Şifre ile Giriş İşlemi
    private void girisIslemi() {
        //Kaydolunan şifre ve ad soyad şartlanacak.
        mAuth = FirebaseAuth.getInstance();
        binding.btnGirisYap.setOnClickListener(v -> kayitliKullaniciGirisi());
    }

    private void kayitliKullaniciGirisi() {
        // Firebase üzerinden email ve şifre alınması
        String email, password;
        email = Objects.requireNonNull(binding.txtGrsEmail.getText()).toString();
        password = Objects.requireNonNull(binding.txtGrsSifre.getText()).toString();

        // Email ve Şifre Giriş Kontrolü (Dolu-Boş)
        if (TextUtils.isEmpty(email)) {
            displayToast("Lütfen emailinizi giriniz");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            displayToast("Lütfen şifrenizi giriniz");
            return;
        }

        binding.pBarGiris.setVisibility(View.VISIBLE);
        // Kayıtlı Kullanıcı Girişi
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                binding.pBarGiris.setVisibility(View.GONE);
                                // Eğer giriş bilgileri doğruysa:
                                // Anasayfaya geç.
                                Intent intent = new Intent(getApplicationContext(), NotePage.class);
                                startActivity(intent);
                            } else {

                                // Giriş hatalı ise:
                                displayToast("E-Mail veya şifre hatalı!");
                            }
                        });
    }


}
