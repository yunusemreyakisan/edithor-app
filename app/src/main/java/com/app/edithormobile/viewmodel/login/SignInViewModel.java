package com.app.edithormobile.viewmodel.login;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import com.app.edithormobile.databinding.ActivitySignInBinding;
import com.app.edithormobile.view.NotePage;
import com.google.firebase.auth.FirebaseAuth;

import org.w3c.dom.Text;

import java.util.Objects;

public class SignInViewModel extends ViewModel {
    private final String successMessage = "Giriş başarılı";
    private final String errorMessage = "Email veya şifre hatalı";

    //Toast message
    public void setToastMessage(Application app, String toastMessage) {
        Toast.makeText(app, toastMessage, Toast.LENGTH_SHORT).show();
    }

    //Kayıtlı kullanıcı girisi
    public void kayitliKullaniciGirisi(Application app, ActivitySignInBinding binding) {
        //Kaydolunan şifre ve ad soyad şartlanacak.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        // Firebase üzerinden email ve şifre alınması
        String email, password;
        email = Objects.requireNonNull(binding.txtGrsEmail.getText()).toString();
        password = Objects.requireNonNull(binding.txtGrsSifre.getText()).toString();

        // Email ve Şifre Giriş Kontrolü (Dolu-Boş)
        if (TextUtils.isEmpty(email)) {
            setToastMessage(app, "Lütfen emailinizi giriniz");
        }

        if (TextUtils.isEmpty(password)) {
            setToastMessage(app, "Lütfen şifrenizi giriniz");
        }

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
            binding.pBarGiris.setVisibility(View.VISIBLE);
            // Kayıtlı Kullanıcı Girişi
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    binding.pBarGiris.setVisibility(View.GONE);
                    // Eğer giriş bilgileri doğruysa:
                    // Anasayfaya geç.
                    Intent intent = new Intent(app, NotePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    app.startActivity(intent);
                    //girisSonuc = true;
                    setToastMessage(app, successMessage);
                } else {
                    binding.pBarGiris.setVisibility(View.GONE);
                    // Giriş hatalı ise:
                    setToastMessage(app, errorMessage);

                    // girisSonuc = false;
                }
            });
        }
    }

    //Shared Preferences (Beni Hatırla)
    public Boolean getSharedPreferences(Application app) {
        Boolean sonuc = null;
        SharedPreferences preferences = app.getSharedPreferences("checkbox", MODE_PRIVATE);
        String checkbox = preferences.getString("Remember", "");
        //Checkbox true ise sonuc true
        sonuc = checkbox.equals("true");
        return sonuc;
    }

    public String setSharedPreference(Application app, String preference) {
        SharedPreferences preferences = app.getSharedPreferences("checkbox", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Remember", preference);
        editor.apply();
        //Seçim degerini dondur.
        return preference;
    }


}
