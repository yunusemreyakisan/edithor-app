package com.app.edithormobile.layouts.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.edithormobile.NotePage;
import com.app.edithormobile.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class SignIn extends AppCompatActivity {

    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    TextInputEditText emailadresi, sifre;
    CheckBox hatirla;
    Button giris, googleGiris;
    TextView kayitOl;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //Methods
        initComponents();
        kayitOlGonder();
        girisIslemi();
        beniHatirla();


    }

    @Override
    public void onBackPressed() {
       //nothing
    }

    //init
    private void initComponents() {
    emailadresi = findViewById(R.id.txtGrsEmail);
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


    //Shared Preferences (Beni Hatırla)
    private void beniHatirla() {
        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = preferences.getString("Remember", "");

        if (checkbox.equals("true")) {
            Intent intent = new Intent(SignIn.this, NotePage.class);
            startActivity(intent);
        }


        hatirla.setOnCheckedChangeListener((compoundButton, b) -> {
            if (compoundButton.isChecked()) {
                SharedPreferences preferences1 = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.putString("Remember", "true");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Beni hatırla açık!", Toast.LENGTH_SHORT).show();
            } else if (!compoundButton.isChecked()) {
                SharedPreferences preferences1 = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.putString("Remember", "false");
                editor.apply();
                Toast.makeText(getApplicationContext(), "Beni hatırla kapalı!", Toast.LENGTH_SHORT).show();
            }
        });
    }





    //E-Mail ve Şifre ile Giriş İşlemi
    private void girisIslemi() {
        //Kaydolunan şifre ve ad soyad şartlanacak.
        mAuth = FirebaseAuth.getInstance();
        giris.setOnClickListener(v -> kayitliKullaniciGirisi());
    }

    private void kayitliKullaniciGirisi() {
        // Firebase üzerinden email ve şifre alınması
        String email, password;
        email = Objects.requireNonNull(emailadresi.getText()).toString();
        password = Objects.requireNonNull(sifre.getText()).toString();

        // Email ve Şifre Giriş Kontrolü (Dolu-Boş)
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(),
                            "Lütfen email giriniz.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                            "Lütfen şifrenizi giriniz.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Kayıtlı Kullanıcı Girişi
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),
                                                "Giriş Başarılı!",
                                                Toast.LENGTH_LONG)
                                        .show();

                                // Eğer giriş bilgileri doğruysa:
                                // Anasayfaya geç.
                                Intent intent = new Intent(getApplicationContext(), NotePage.class);
                                startActivity(intent);


                            } else {

                                // Giriş hatalı ise:
                                Toast.makeText(getApplicationContext(),
                                                "E-Mail veya Şifre Hatalı!",
                                                Toast.LENGTH_LONG)
                                        .show();

                            }
                        });

    }


}
