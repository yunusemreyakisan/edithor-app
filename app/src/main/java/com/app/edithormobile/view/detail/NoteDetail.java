package com.app.edithormobile.view.detail;

import static java.util.Objects.requireNonNull;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;

import com.app.edithormobile.R;
import com.app.edithormobile.databinding.ActivityNoteDetailBinding;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.NotePage;
import com.app.edithormobile.viewmodel.NoteDetailViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class NoteDetail extends AppCompatActivity {

    ActivityNoteDetailBinding binding;
    String notBasligi, notIcerigi, notOlusturmaZamani, notID, olusturma_zamani;
    int notRengi;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference removeRef, removedReference;
    //color picker
    int defaultColor;
    NoteDetailViewModel viewModel;
    Util util = new Util();

    String url = "https://api.openai.com/v1/chat/completions";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //ViewModel Bağlama
        viewModel = ViewModelProviders.of(this).get(NoteDetailViewModel.class);
        System.out.println("URL: " + viewModel.getAPIUrl()); //ViewModel Test

        //Window nesnesi alma
        if (Build.VERSION.SDK_INT >= 26) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_color_light));
        }

        //get intent data
        getIntentData();
        viewModel.assignData(binding, notBasligi, notIcerigi, notOlusturmaZamani, notRengi);
        //notGuncelleme();


        //TODO: Textviewler edittexte donusturulup otomatik not kaydedilecek.
        //TODO: Edittext klavyenin üzerinde görünmeli.

        buttonTasks();

    }

    //onStart()
    @Override
    protected void onStart() {
        super.onStart();
        //Button tasks
        buttonTasks();


    }

    @Override
    public void onBackPressed() {
        //Eğer not aynı kaldıysa olusturma zamanını guncellemesin.
        if (!notBasligi.equals(binding.txtDetailTitle.getText().toString())) {
            olusturma_zamani = util.olusturmaZamaniGetir();
            notGuncelleme();
        } else if (!notIcerigi.equals(binding.txtDetailContent.getText().toString())) {
            olusturma_zamani = util.olusturmaZamaniGetir();
            notGuncelleme();
        } else {
            Intent intent = new Intent(NoteDetail.this, NotePage.class);
            startActivity(intent);
        }
    }


    //Note Update
    public void notGuncelleme() {
        notID = getIntent().getStringExtra("id");
        NoteModel position = (NoteModel) getIntent().getSerializableExtra("position");
        Log.e("position degeri", String.valueOf(position));

        //Düzenleme tarihi eklenmesi
        binding.tvDetailOlusturmaZamani.setText(olusturma_zamani);

        HashMap<String, Object> map = new HashMap<>();
        map.put("notBaslik", binding.txtDetailTitle.getText().toString());
        map.put("notIcerigi", binding.txtDetailContent.getText().toString());
        map.put("notOlusturmaTarihi", binding.tvDetailOlusturmaZamani.getText().toString());
        map.put("color", notRengi);

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
                            position.setColor(notRengi);
                            binding.tvSonDuzenlemeZamani.setText(olusturma_zamani);

                            //TODO: Position nesnesi ile o pozisyona ait object alınıyor.
                            //adapter.notifyDataSetChanged(); //null dönüyor!!!

                            util.toastMessage(getApplicationContext(), "Notunuz güncellendi").show();
                            Intent intent = new Intent(NoteDetail.this, NotePage.class);
                            startActivity(intent);
                        }
                    }
                }).addOnFailureListener(e -> util.toastMessage(this, "Hata oluştu").show());

    }


    //Button Tasks
    public void buttonTasks() {
        //Button Back
        binding.btnDetailBack.setOnClickListener(v -> {
            //Eğer not aynı kaldıysa olusturma zamanını guncellemesin.
            if (!notBasligi.equals(binding.txtDetailTitle.getText().toString())) {
                olusturma_zamani = util.olusturmaZamaniGetir();
                notGuncelleme();
            } else if (!notIcerigi.equals(binding.txtDetailContent.getText().toString())) {
                olusturma_zamani = util.olusturmaZamaniGetir();
                notGuncelleme();
            } else {
                onBackPressed();
            }
        });

        //Buton seçimlerine göre renk degisimi
        binding.btnToolbarColorRed.setOnClickListener(v -> {
            notRengi = getColor(R.color.red_circle);
            binding.scrollView2.setBackgroundColor(getResources().getColor(R.color.red_circle));
        });

        binding.btnToolbarColorBlue.setOnClickListener(v -> {
            notRengi = getColor(R.color.blue_circle);
            binding.scrollView2.setBackgroundColor(getResources().getColor(R.color.blue_circle));
        });

        binding.btnToolbarColorGreen.setOnClickListener(v -> {
            notRengi = getColor(R.color.green_circle);
            binding.scrollView2.setBackgroundColor(getResources().getColor(R.color.green_circle));
        });

        //TODO: Light temada herhangi bir renk seçmediğimizde sorun yok, dark theme de seçince light moda dönüşte yazılar görünmüyor.
        binding.btnToolbarEmptyColor.setOnClickListener(v -> {
            notRengi = getColor(R.color.bg_color_light);
            binding.scrollView2.setBackgroundColor(getResources().getColor(R.color.bg_color_light));
        });

        //Share Toolbar Button
        BottomSheetDialog dialog = new BottomSheetDialog(NoteDetail.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.share_bottom_sheet_layout, null);
        dialog.setContentView(view);
        binding.btnToolbarShare.setOnClickListener(v -> {
            //show dialog
            dialog.show();
        });

        //Delete Note Bottom Sheet
        view.findViewById(R.id.bottom_sheet_delete_layout).setOnClickListener(v -> deleteNote());


        //GPT Toolbar Button
        BottomSheetDialog dialogGPT = new BottomSheetDialog(NoteDetail.this);
        View viewGPT = LayoutInflater.from(getApplicationContext()).inflate(R.layout.gpt_bottom_sheet_dialog, null);
        dialogGPT.setContentView(viewGPT);
        binding.shineToolbarGpt.setOnClickListener(v -> {
            //show
            dialogGPT.show();
        });

        //Custom layout for ask question
        viewGPT.findViewById(R.id.bottom_sheet_gpt_question_layout).setOnClickListener(this::showAlertDialogButtonClicked);
    }

    //Custom GPT Ask Question
    public void showAlertDialogButtonClicked(View view) {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_gpt_ask_layout, null);
        builder.setView(customLayout);

        //Custom layout öğelerine erişim
        EditText editText = (EditText) customLayout.findViewById(R.id.txtChatAlert);
        TextView tvResponse = (TextView) customLayout.findViewById(R.id.tvChatGPTAlert);
        ImageButton sendMessageGPTButton = (ImageButton) customLayout.findViewById(R.id.sendMessageGPTDialog);

        //Sorulan sorunun alınması ve methoda yerlestirilmesi
        String ifade = editText.getText().toString();
        sendMessageGPTButton.setOnClickListener(v -> {
            tvResponse.setHint("Edithor düşünüyor...");
            viewModel.sendMessageGPT(ifade, tvResponse, getApplicationContext());
        });

        // add a button
        builder.setPositiveButton("Kopyala", (dialog, which) -> {
            // send data from the AlertDialog to the Activity
            String gptResponse = tvResponse.getText().toString();
            viewModel.getCopiedObject(getApplicationContext(), gptResponse); //Kopyalama islemi
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //Delete Note
    private void deleteNote() {
        View v = binding.getRoot();
        notID = getIntent().getStringExtra("id");
        NoteModel pos = (NoteModel) getIntent().getSerializableExtra("position");
        //Remove reference
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
        removeRef = FirebaseDatabase.getInstance().getReference().child("Kullanicilar")
                .child(user_id).child("Notlarim");

        AlertDialog.Builder builder = new AlertDialog.Builder(NoteDetail.this);
        builder.setTitle("Emin misiniz?");
        builder.setMessage("Notu silmek istediğinizden emin misiniz?");
        builder.setPositiveButton("EVET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeRef.child(notID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Silinenler Referansı
                            removedReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar")
                                    .child(user_id).child("Silinen Notlarim");
                            //unique getKey()
                            NoteModel mNotes = new NoteModel(pos.getNoteID(), pos.getNotIcerigi(), pos.getNotBaslik(),
                                    pos.getNotOlusturmaTarihi(), false, pos.getColor());
                            removedReference.child(notID).setValue(mNotes);

                            //Intent
                            Intent intent = new Intent(NoteDetail.this, NotePage.class);
                            startActivity(intent);
                        }

                        //Snackbar Effect (Throws Exception)
                        int sure = 3000;
                        Snackbar snackbar = Snackbar.make(v, "Notunuz silindi", sure).setAction("GERİ AL", view -> {
                            //Geri al dedikten sonra silinenlerden silinip yine eklenen notlara gecmesi
                            removedReference.child(notID).removeValue();

                            mAuth = FirebaseAuth.getInstance();
                            //Veritabanına Canlı Kayıt Etme (Realtime Database)
                            String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
                            mUser = mAuth.getCurrentUser();
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                                    .child("Kullanicilar").child(user_id).child("Notlarim");

                            //yuklenen fotorafin storage adresi
                            final String image = pos.getImageUri() != null ? pos.getImageUri() : null;
                            //model
                            if (image != null) {
                                //unique getKey()
                                NoteModel mNotes = new NoteModel(pos.getNoteID(), pos.getNotIcerigi(), pos.getNotBaslik(),
                                        pos.getNotOlusturmaTarihi(), image, false, pos.getColor());
                                mDatabase.child(notID).setValue(mNotes);
                            } else {
                                //unique getKey()
                                NoteModel mNotes = new NoteModel(pos.getNoteID(), pos.getNotIcerigi(), pos.getNotBaslik(),
                                        pos.getNotOlusturmaTarihi(), false, pos.getColor());
                                mDatabase.child(notID).setValue(mNotes);
                            }


                            //TODO: 20 adet deneme yapıp en başarılı modeli bulup entegre edilecek.
                            //TODO: Modele sonra karar verilecek.


                        });
                        snackbar.setActionTextColor(getResources().getColor(R.color.button_active_color));
                        snackbar.show();
                    }
                }).addOnFailureListener(e -> util.toastMessage(getApplicationContext(), "Vazgeçildi").show());

            }
        });
        builder.setNegativeButton("HAYIR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                util.toastMessage(getApplicationContext(), "Vazgeçildi");
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.button_active_color));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.button_active_color));
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
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


}