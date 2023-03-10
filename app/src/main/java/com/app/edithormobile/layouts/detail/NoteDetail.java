package com.app.edithormobile.layouts.detail;

import static java.util.Objects.requireNonNull;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.app.edithormobile.NotePage;
import com.app.edithormobile.R;
import com.app.edithormobile.databinding.ActivityNoteDetailBinding;
import com.app.edithormobile.models.NoteModel;
import com.app.edithormobile.utils.IToast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import yuku.ambilwarna.AmbilWarnaDialog;

public class NoteDetail extends AppCompatActivity implements IToast {

    ActivityNoteDetailBinding binding;
    String notBasligi, notIcerigi, notOlusturmaZamani, notID, olusturma_zamani;
    int notRengi;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    //color picker
    int defaultColor;

    NoteModel position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Window nesnesi alma
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_color_light));
        }

        //get intent data
        getIntentData();
        assignData(notBasligi, notIcerigi, notOlusturmaZamani, notRengi);
        //notGuncelleme();


        //TODO: Textviewler edittexte donusturulup otomatik not kaydedilecek.
        //TODO: Edittext klavyenin üzerinde görünmeli.

    }

    @Override
    public void onBackPressed() {
        //Eğer not aynı kaldıysa olusturma zamanını guncellemesin.
        if (!notBasligi.equals(binding.txtDetailTitle.getText().toString())) {
            olusturma_zamani = olusturmaZamaniGetir();
            notGuncelleme();
        } else if (!notIcerigi.equals(binding.txtDetailContent.getText().toString())) {
            olusturma_zamani = olusturmaZamaniGetir();
            notGuncelleme();
        } else {
            Intent intent = new Intent(NoteDetail.this, NotePage.class);
            startActivity(intent);
        }
    }


    //onStart()
    @Override
    protected void onStart() {
        super.onStart();
        //Button tasks
        buttonTasks();


    }


    //Note Update
    public void notGuncelleme() {
        notID = getIntent().getStringExtra("id");
        position = (NoteModel) getIntent().getSerializableExtra("position");
        Log.e("position degeri", String.valueOf(position));

        //Düzenleme tarihi eklenmesi
        binding.tvDetailOlusturmaZamani.setText(olusturma_zamani);

        HashMap<String, Object> map = new HashMap<>();
        map.put("notBaslik", binding.txtDetailTitle.getText().toString());
        map.put("notIcerigi", binding.txtDetailContent.getText().toString());
        map.put("notOlusturmaTarihi", binding.tvDetailOlusturmaZamani.getText().toString());
        map.put("color", defaultColor);

        //db ref
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mUser).getUid();
        FirebaseDatabase.getInstance()
                .getReference().child("Kullanicilar").child(user_id).child("Notlarim").child(notID).updateChildren(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            position.setNotBaslik(binding.txtDetailTitle.getText().toString());
                            position.setNotIcerigi(binding.txtDetailContent.getText().toString());
                            position.setNotOlusturmaTarihi(olusturma_zamani);
                            position.setColor(defaultColor);
                            binding.tvSonDuzenlemeZamani.setText(olusturma_zamani);

                            //TODO: Position nesnesi ile o pozisyona ait object alınıyor.
                            //adapter.notifyDataSetChanged(); //null dönüyor!!!


                            Toast("Notunuz güncellendi");
                            Intent intent = new Intent(NoteDetail.this, NotePage.class);
                            startActivity(intent);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast("Hata oluştu");
                    }
                });

    }


    //assign data
    public void assignData(String baslik, String icerik, String olusturmaZamani, int notRengi) {
        binding.txtDetailTitle.setText(baslik);
        binding.txtDetailContent.setText(icerik);
        binding.tvDetailOlusturmaZamani.setText(olusturmaZamani);
        binding.ivDetailColor.setBackgroundColor(notRengi);
        binding.tvSonDuzenlemeZamani.setText(olusturmaZamani); //Toolbar üzerinde son düzenleme tarihinin gosterilmesi
    }

    //Button Tasks
    public void buttonTasks() {
        //Button Back
        binding.btnDetailBack.setOnClickListener(v -> {
            //Eğer not aynı kaldıysa olusturma zamanını guncellemesin.
            if (!notBasligi.equals(binding.txtDetailTitle.getText().toString())) {
                olusturma_zamani = olusturmaZamaniGetir();
                notGuncelleme();
            } else if (!notIcerigi.equals(binding.txtDetailContent.getText().toString())) {
                olusturma_zamani = olusturmaZamaniGetir();
                notGuncelleme();
            } else {
                onBackPressed();
            }
        });


        //Button Color Picker
        binding.btnToolbarColorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();
            }
        });

    }

    //Color picker dialog
    private void openColorPicker() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                //binding.addNoteBG.setBackgroundColor(getResources().getColor(R.color.bg_color_light));
                binding.btnToolbarColorPicker.setColorFilter(getResources().getColor(R.color.bg_color_light));
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;
                //binding.btnToolbarColorPicker.setBackgroundColor(color);
                binding.btnToolbarColorPicker.setColorFilter(color);


            }
        });
        dialog.show();
        dialog.getDialog().getButton(dialog.getDialog().BUTTON_NEGATIVE).setTextColor(getColor(R.color.button_active_color));
        dialog.getDialog().getButton(dialog.getDialog().BUTTON_POSITIVE).setTextColor(getColor(R.color.button_active_color));
    }

    //Olusturma zamani al
    public String olusturmaZamaniGetir() {
        //Olusturma zamanini al.
        Calendar calendar = new GregorianCalendar();
        int month = calendar.get(Calendar.MONTH) + 1; //0 ile basladigi icin 1 eklendi.
        int hours = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        String time = String.format("%02d:%02d", hours, minutes);
        String ay = null;
        int ayinGunu = calendar.get(Calendar.DAY_OF_MONTH);

        /*
        if (calendar.get(Calendar.DAY_OF_MONTH) < 9) {
            ayinGunu = "0" + calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            ayinGunu = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }

         */

        //Ay
        switch (month) {
            case 1:
                ay = "Ocak";
                break;
            case 2:
                ay = "Şubat";
                break;
            case 3:
                ay = "Mart";
                break;

            case 4:
                ay = "Nisan";
                break;
            case 5:
                ay = "Mayıs";
                break;
            case 6:
                ay = "Haziran";
                break;
            case 7:
                ay = "Temmuz";
                break;
            case 8:
                ay = "Ağustos";
                break;
            case 9:
                ay = "Eylül";
                break;
            case 10:
                ay = "Ekim";
                break;
            case 11:
                ay = "Kasım";
                break;
            case 12:
                ay = "Aralık";
                break;
        }

        String notOlusturmaTarihi = ayinGunu + " " + ay + " " + time;

        return notOlusturmaTarihi;
    }


    //Intent data
    public void getIntentData() {
        Intent data = getIntent();
        notID = data.getStringExtra("id");
        notBasligi = data.getStringExtra("baslik");
        notIcerigi = data.getStringExtra("icerik");
        notOlusturmaZamani = data.getStringExtra("olusturma_zamani");
        notRengi = data.getIntExtra("color", 0);
    }


    @Override
    public void Toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}