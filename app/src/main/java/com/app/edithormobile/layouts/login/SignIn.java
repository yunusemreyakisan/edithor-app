package com.app.edithormobile.layouts.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.edithormobile.NotePage;
import com.app.edithormobile.R;
import com.app.edithormobile.databinding.ActivitySignInBinding;
import com.app.edithormobile.models.UserModel;
import com.app.edithormobile.utils.IToast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Objects;


public class SignIn extends AppCompatActivity implements IToast {
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    ActivitySignInBinding binding;
    private static final int RC_SIGN_IN = 0;
    private GoogleSignInClient mGoogleSignInClient;


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


        //configure google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Click to sign in button
        binding.btnGoogle.setOnClickListener(v1 -> {
            switch (v1.getId()) {
                case R.id.btnGoogle:
                    signIn();
                    break;
            }
        });
    }
    //eof onCreate()

    //Google Sign-in
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    //Google ile giris isleminin sonucunun elde edilmesi
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast("Giris başarılı");
            googleHesabiniKaydet(account);

            Intent intent = new Intent(this, NotePage.class);
            startActivity(intent);
        } catch (ApiException e) {
            Log.e("Error", "SignInResult:Failed Code = " + e.getStatusCode() + "error" + e.getMessage());
        }
    }

    //Google ile giris yapildiginda kullanici bilgilerini Realtime DB uzerine kaydedilmesi
    private void googleHesabiniKaydet(GoogleSignInAccount account) {
        mAuth = FirebaseAuth.getInstance();

        String name = Objects.requireNonNull(account.getGivenName());
        String email = Objects.requireNonNull(account.getEmail());
        String password = Objects.requireNonNull(account.getId());

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Veritabanına Canlı Kayıt Etme (Realtime Database)
                            String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            mUser = mAuth.getCurrentUser();
                            mDatabase = FirebaseDatabase.getInstance().getReference()
                                    .child("Kullanicilar")
                                    .child(user_id)
                                    .child("Kullanıcı Bilgileri");
                            //Olusturma zamanini al.
                            Calendar calendar = new GregorianCalendar();
                            int month = calendar.get(Calendar.MONTH) + 1; //0 ile basladigi icin 1 eklendi.
                            int hours = calendar.get(Calendar.HOUR);
                            int minutes = calendar.get(Calendar.MINUTE);
                            String time = String.format("%02d:%02d", hours, minutes);
                            String hesapOlusturmaTarihi = calendar.get(Calendar.DAY_OF_MONTH) + "/" + month
                                    + " " + time;

                            HashMap<String, UserModel> mData = new HashMap<>();
                            mData.put(user_id, new UserModel(user_id, email, password, hesapOlusturmaTarihi));

                            //Realtime Database
                            mDatabase.setValue(mData).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Intent intent = new Intent(SignIn.this, NotePage.class);
                                    startActivity(intent);
                                    Toast("Hesap oluşturuldu");
                                } else {
                                    Toast("Hesap oluşturulamadı, yeniden deneyin");
                                }
                            });
                        }
                    }
                });
    }

    //onStart() methodu ile kullanicinin onceden giris yapıp yapmadiginin kontrolu
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            //Toast.makeText(this, "Kullanıcı zaten giriş yaptı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), NotePage.class);
            startActivity(intent);
        }
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
            Toast("Lütfen emailinizi giriniz");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast("Lütfen şifrenizi giriniz");
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
                                Toast("E-Mail veya şifre hatalı!");
                            }
                        });
    }


    @Override
    public void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
