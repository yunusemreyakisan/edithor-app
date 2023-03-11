package com.app.edithormobile.layouts.detail;

import static java.util.Objects.requireNonNull;

import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.app.edithormobile.NotePage;
import com.app.edithormobile.R;
import com.app.edithormobile.databinding.ActivityNoteDetailBinding;
import com.app.edithormobile.models.NoteModel;
import com.app.edithormobile.utils.IToast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class NoteDetail extends AppCompatActivity implements IToast {

    ActivityNoteDetailBinding binding;
    String notBasligi, notIcerigi, notOlusturmaZamani, notID, olusturma_zamani;
    int notRengi;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference removeRef, removedReference;
    //color picker
    int defaultColor;

    String url = "https://api.openai.com/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Window nesnesi alma
        if (Build.VERSION.SDK_INT >= 26) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_color_light));
        }

        //get intent data
        getIntentData();
        assignData(notBasligi, notIcerigi, notOlusturmaZamani, notRengi);
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


                            Toast("Notunuz güncellendi");
                            Intent intent = new Intent(NoteDetail.this, NotePage.class);
                            startActivity(intent);
                        }
                    }
                }).addOnFailureListener(e -> Toast("Hata oluştu"));

    }


    //assign data
    public void assignData(String baslik, String icerik, String olusturmaZamani, int notRengi) {
        binding.txtDetailTitle.setText(baslik);
        binding.txtDetailContent.setText(icerik);
        binding.tvDetailOlusturmaZamani.setText(olusturmaZamani);
        binding.tvSonDuzenlemeZamani.setText(olusturmaZamani); //Toolbar üzerinde son düzenleme tarihinin gosterilmesi
        binding.scrollView2.setBackgroundColor(notRengi);
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
            sendMessageGPT(ifade, tvResponse);
        });

        // add a button
        builder.setPositiveButton("Kopyala", (dialog, which) -> {
            // send data from the AlertDialog to the Activity
            String gptResponse = tvResponse.getText().toString();

            // ClipboardManager nesnesini al
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            
            // Metin değerini kopyala
            ClipData clip = ClipData.newPlainText("label", gptResponse);
            clipboard.setPrimaryClip(clip);
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Custom dialog send message button listener
    public void sendMessageGPT(String ifade, TextView text) {
        try {
            getResponse(ifade, text);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }


    //Response (GPT-3.5-Turbo)
    private void getResponse(String question, TextView text) throws JSONException {
        RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());

        /** @author yunusemreyakisan
        {
        "model": "gpt-3.5-turbo",
        "messages": [{"role": "user", "content": "Hello!"}]
        }
         */

        //Parametrelere gore objelerin olusturulması
        //messageObject içerisine role ve content verilerini yerleştirdik.
        //Bu nesneyi daha sonra messageArray içerisine yerleştirdik.
        //Bu array'i genel jsonObject'e yerleştirdik.
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", "gpt-3.5-turbo");
        JSONArray messagesArray = new JSONArray();
        JSONObject messageObject = new JSONObject();
        messageObject.put("role", "user");
        messageObject.put("content", question);
        messagesArray.put(messageObject);
        jsonObject.put("messages", messagesArray);


        //Post istegi
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String resMessage = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
                    text.setText(resMessage);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, error -> {
            Toast.makeText(getApplicationContext(), "Failed response", Toast.LENGTH_SHORT)
                    .show();
        }) {
            @NotNull
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer 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");
                return params;
            }
        };
        postRequest.setRetryPolicy((RetryPolicy) (new RetryPolicy() {
            public int getCurrentTimeout() {
                return 50000;
            }

            public int getCurrentRetryCount() {
                return 50000;
            }

            public void retry(@Nullable VolleyError error) {
                runOnUiThread((Runnable) (new Runnable() {
                    public final void run() {
                        Toast toast = Toast.makeText(getApplicationContext(), (CharSequence) "API Hatası", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }));
            }
        }));
        queue.add(postRequest);
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
                }).addOnFailureListener(e -> Toast("Vazgeçildi."));

            }
        });
        builder.setNegativeButton("HAYIR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast("Vazgeçildi");
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.button_active_color));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.button_active_color));
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
    }

    //Color picker dialog
   /* private void openColorPicker() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                //binding.addNoteBG.setBackgroundColor(getResources().getColor(R.color.bg_color_light));
                binding.btnToolbarColorPicker.setColorFilter(getResources().getColor(R.color.bg_color_dark));
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

    */

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