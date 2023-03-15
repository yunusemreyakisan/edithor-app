package com.app.edithormobile.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;

import com.app.edithormobile.R;
import com.app.edithormobile.databinding.ActivitySignInBinding;
import com.app.edithormobile.model.UserModel;
import com.app.edithormobile.util.IToast;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.NotePage;
import com.app.edithormobile.viewmodel.sign_in.SignInViewModel;
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

import java.util.HashMap;
import java.util.Objects;


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

        //ViewModel Bağlama
        viewModel = ViewModelProviders.of(this).get(SignInViewModel.class);


        //Methods
        kayitOlGonder();
        girisIslemi();
        rememberUser();


        //configure google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
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


    //TODO: Google sign in sıkıntılı,düzenlenmesi gerekiyor
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

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Veritabanına Canlı Kayıt Etme (Realtime Database)
                    String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                    mUser = mAuth.getCurrentUser();
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Kullanıcı Bilgileri");

                    String hesapOlusturmaTarihi = util.olusturmaZamaniGetir();

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
