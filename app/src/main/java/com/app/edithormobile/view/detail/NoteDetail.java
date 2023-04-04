package com.app.edithormobile.view.detail;

import static java.util.Objects.requireNonNull;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.app.edithormobile.R;
import com.app.edithormobile.databinding.ActivityNoteDetailBinding;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.service.APIService;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.create.AddNote;
import com.app.edithormobile.view.home.NotePage;
import com.app.edithormobile.viewmodel.detail.NoteDetailViewModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class NoteDetail extends AppCompatActivity {

    ActivityNoteDetailBinding binding;
    ArrayList<NoteModel> pinnedList;
    String notBasligi, notIcerigi, notOlusturmaZamani, notID, olusturma_zamani;
    String image;
    Boolean pin;
    String imageURL;
    private Uri imageUri;
    int notRengi;
    FirebaseAuth mAuth;
    private final int PICK_IMAGE_REQUEST = 71;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    ImageView ivOCR;
    Bitmap capturedImage;
    FirebaseUser mUser;
    FirebaseStorage storage;
    StorageReference storageReference, ref;
    DatabaseReference removeRef, removedReference, mDatabase, mDatabaseReference;
    //color picker
    int defaultColor;
    BottomSheetDialog dialog;
    NoteDetailViewModel viewModel;
    int sure = 3000;
    Util util = new Util();
    Snackbar snackbar;
    NoteModel position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Db ref
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Notlarim");

        //Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        ref = storageReference.child("harici/" + UUID.randomUUID().toString());

        //ViewModel Bağlama
        viewModel = ViewModelProviders.of(this).get(NoteDetailViewModel.class);
        System.out.println("URL: " + viewModel.getAPIUrl()); //ViewModel Test

        //init snackbar
        snackbar = Snackbar.make(binding.getRoot(), "Notunuz silindi", sure);
        //Window nesnesi alma
        if (Build.VERSION.SDK_INT >= 26) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.bg_color_light));
        }

        //get intent data
        getIntentData();
        viewModel.assignData(binding, notBasligi, notIcerigi, notOlusturmaZamani, notRengi);
        notID = getIntent().getStringExtra("id");
        position = (NoteModel) getIntent().getSerializableExtra("position");
        Log.e("position degeri", String.valueOf(position));


        //TODO: Textviewler edittexte donusturulup otomatik not kaydedilecek.
        //TODO: Edittext klavyenin üzerinde görünmeli.

        buttonTasks();
        //Get Adapter Position
        position = (NoteModel) getIntent().getSerializableExtra("position");
    }

    //onStart()
    @Override
    protected void onStart() {
        super.onStart();
        pin = position.isPinned();
        //Button tasks
        if (pin) {
            binding.btnDetailPin.setImageResource(R.drawable.ic_bookmark_true);
        } else {
            binding.btnDetailPin.setImageResource(R.drawable.ic_pin);
        }
        buttonTasks();
        //if image != null get data
        getNoteImage();
    }


    @Override
    public void onBackPressed() {
        //Eğer not aynı kaldıysa olusturma zamanını guncellemesin.
        if (!notBasligi.equals(binding.txtDetailTitle.getText().toString())) {
            olusturma_zamani = util.olusturmaZamaniGetir(getApplicationContext());
            viewModel.updateNote(binding, mDatabaseReference, position, notID, notRengi, olusturma_zamani, pin, util, getApplicationContext());
        } else if (!notIcerigi.equals(binding.txtDetailContent.getText().toString())) {
            olusturma_zamani = util.olusturmaZamaniGetir(getApplicationContext());
            viewModel.updateNote(binding, mDatabaseReference, position, notID, notRengi, olusturma_zamani, pin,  util, getApplicationContext());
        } else if (notRengi != 0) {
            olusturma_zamani = util.olusturmaZamaniGetir(getApplicationContext());
            viewModel.updateNote(binding, mDatabaseReference, position, notID, notRengi, olusturma_zamani, pin,  util, getApplicationContext());
        } else {
            Intent intent = new Intent(NoteDetail.this, NotePage.class);
            startActivity(intent);
        }
    }


    //if image != null get data
    public void getNoteImage() {
        mAuth = FirebaseAuth.getInstance();
        //Veritabanına Canlı Kayıt Etme (Realtime Database)
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Notlarim").child(position.getNoteID()).child("imageUri");

        if (image != null) {
            Log.e("Image degeri: ", image);
            binding.NoteDetailImage.setVisibility(View.VISIBLE);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String link = dataSnapshot.getValue(String.class);
                    CircularProgressDrawable drawable = new CircularProgressDrawable(getApplicationContext());
                    drawable.setCenterRadius(40f);
                    drawable.setStrokeWidth(8f);
                    drawable.start();
                    // Glide
                    Glide.with(getApplicationContext()).load(link).centerCrop().placeholder(drawable).into(binding.NoteDetailImage);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(NoteDetail.this, "Error Loading Image", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            binding.NoteDetailImage.setVisibility(View.GONE);
        }
    }


    //Button Tasks
    public void buttonTasks() {
        //Button Back
        binding.btnDetailBack.setOnClickListener(v -> {
            if (imageURL == null) {

                Toast.makeText(this, "Image url bos", Toast.LENGTH_SHORT).show();
            }

            //Eğer not aynı kaldıysa olusturma zamanını guncellemesin.
            if (!notBasligi.equals(binding.txtDetailTitle.getText().toString())) {
                olusturma_zamani = util.olusturmaZamaniGetir(getApplicationContext());
                viewModel.updateNote(binding, mDatabaseReference, position, notID, notRengi, olusturma_zamani, pin, util, getApplicationContext());
            } else if (!notIcerigi.equals(binding.txtDetailContent.getText().toString())) {
                olusturma_zamani = util.olusturmaZamaniGetir(getApplicationContext());
                viewModel.updateNote(binding, mDatabaseReference, position, notID, notRengi, olusturma_zamani, pin, util, getApplicationContext());
            } else if (notRengi != 1) {
                olusturma_zamani = util.olusturmaZamaniGetir(getApplicationContext());
                viewModel.updateNote(binding, mDatabaseReference, position, notID, notRengi, olusturma_zamani, pin, util, getApplicationContext());
            } else if (position.isPinned() != pin) {
                olusturma_zamani = util.olusturmaZamaniGetir(getApplicationContext());
                viewModel.updateNote(binding, mDatabaseReference, position, notID, notRengi, olusturma_zamani, pin,util, getApplicationContext());
            } else if (position.getImageUri() != null) {
                olusturma_zamani = util.olusturmaZamaniGetir(getApplicationContext());
                viewModel.updateNote(binding, mDatabaseReference, position, notID, notRengi, olusturma_zamani, pin,util, getApplicationContext());
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
        dialog = new BottomSheetDialog(NoteDetail.this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.share_bottom_sheet_layout, null);
        dialog.setContentView(view);
        binding.btnToolbarShare.setOnClickListener(v -> {
            //show dialog
            dialog.show();
        });

        //Delete Note Bottom Sheet
        view.findViewById(R.id.bottom_sheet_delete_layout).setOnClickListener(v -> deleteNote());
        view.findViewById(R.id.properties_copy_note).setOnClickListener(v -> viewModel.copyNote(getApplication(), binding));
        view.findViewById(R.id.shareNoteToolbar).setOnClickListener(v -> viewModel.shareNote(getApplication(), binding));


        //GPT Toolbar Button
        BottomSheetDialog dialogGPT = new BottomSheetDialog(NoteDetail.this);
        View viewGPT = LayoutInflater.from(getApplicationContext()).inflate(R.layout.gpt_bottom_sheet_dialog, null);
        dialogGPT.setContentView(viewGPT);
        binding.shineToolbarGpt.setOnClickListener(v -> dialogGPT.show());

        //Custom layout for ask question
        viewGPT.findViewById(R.id.bottom_sheet_gpt_question_layout).setOnClickListener(this::showAlertDialogButtonClicked);
        //Daily Quote
        viewGPT.findViewById(R.id.bottom_sheet_gunun_sozu_layout).setOnClickListener(this::showAlertDialogForDailyQuote);
        //Brainstorming
        viewGPT.findViewById(R.id.bottom_sheet_brain_storming_layout).setOnClickListener(this::showAlertDialogForBrainStorming);
        //Translate Turkish-English
        viewGPT.findViewById(R.id.bottom_sheet_translate_layout).setOnClickListener(this::showAlertDialogForTranslate);


        //Plus Extra Properties Bottom Sheet
        BottomSheetDialog dialogPlus = new BottomSheetDialog(NoteDetail.this);
        View viewPlus = LayoutInflater.from(getApplicationContext()).inflate(R.layout.extra_bottom_sheet_layout, null);
        dialogPlus.setContentView(viewPlus);
        binding.btnToolbarProperties.setOnClickListener(v -> dialogPlus.show());

        //viewPlus.findViewById(R.id.create_pdf_toolbar).setOnClickListener(v -> generatePDF());
        //Choose Photo
        viewPlus.findViewById(R.id.choosePhotoToolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Choose Photo", "Fotograf seçmeye tıklanıldı");
                chooseImage();
            }
        });



        //Pinned Toolbar Top
        binding.btnDetailPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pin) {
                    pin = false;
                    position.setPinned(pin);
                    //binding.btnDetailPin.setColorFilter(null);
                    binding.btnDetailPin.setImageResource(R.drawable.ic_pin);
                } else {
                    pin = true;
                    position.setPinned(pin);
                    //binding.btnDetailPin.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.button_active_color));
                    binding.btnDetailPin.setImageResource(R.drawable.ic_bookmark_true);
                }

                //TODO: Pinlenenler ayrı bir listeye eklenecek. Adapter üzerinden gösterim yapılacak.
               //Toast.makeText(NoteDetail.this, "Pinlendi " + pin, Toast.LENGTH_SHORT).show();

            }
        });

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
            util.getCopiedObject(getApplicationContext(), gptResponse); //Kopyalama islemi
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Translate
    private void showAlertDialogForTranslate(View view) {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_translate_dialog, null);
        //Custom layout öğelerine erişim
        EditText txtCevirilecekIfade = (EditText) customLayout.findViewById(R.id.txtChatTranslate);
        ImageButton sendMessageGPTButton = (ImageButton) customLayout.findViewById(R.id.sendMessageGPTDialogTranslate);

        TextView translateResponse = (TextView) customLayout.findViewById(R.id.tvChatGPTResponseTranslate);
        builder.setView(customLayout);

        //Sorulan sorunun alınması ve methoda yerlestirilmesi
        sendMessageGPTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateResponse.setHint(getString(R.string.edithor_ceviriyi_yapiyor));
                //İfadelerin alınması
                String ifade = txtCevirilecekIfade.getText().toString();
                String gonderilecekIfade = getString(R.string.ingilizceiseturkceye_turkceiseingilizceyecevir) + ifade;
                sendMessageGPT(gonderilecekIfade, translateResponse, getApplicationContext());

            }
        });


        // add a button
        builder.setPositiveButton(getString(R.string.kopyala), (dialog, which) -> {
            // send data from the AlertDialog to the Activity
            String gptResponse = translateResponse.getText().toString();
            util.getCopiedObject(getApplicationContext(), gptResponse); //Kopyalama islemi
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Brainstorming
    public void showAlertDialogForBrainStorming(View view) {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_get_gpt_response, null);
        //Custom layout öğelerine erişim
        TextView brainStormingResponse = (TextView) customLayout.findViewById(R.id.tvChatGPTResponse);
        TextView brainStormingTextView = (TextView) customLayout.findViewById(R.id.tvOzellikAdi);
        builder.setView(customLayout);

        //Sorulan sorunun alınması ve methoda yerlestirilmesi
        brainStormingTextView.setText(getString(R.string.fikir_makinesi));
        String ifade = getString(R.string.bana_hobi_fikri_ver);
        brainStormingResponse.setHint(getString(R.string.edithor_fikir_dusunuyor));
        sendMessageGPT(ifade, brainStormingResponse, getApplicationContext());

        // add a button
        builder.setPositiveButton(getString(R.string.kopyala), (dialog, which) -> {
            // send data from the AlertDialog to the Activity
            String gptResponse = brainStormingResponse.getText().toString();
            util.getCopiedObject(getApplicationContext(), gptResponse); //Kopyalama islemi
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Daily Quote
    public void showAlertDialogForDailyQuote(View view) {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_get_gpt_response, null);
        //Custom layout öğelerine erişim
        TextView gununSozuResponse = (TextView) customLayout.findViewById(R.id.tvChatGPTResponse);
        TextView gununSozuTextView = (TextView) customLayout.findViewById(R.id.tvOzellikAdi);
        builder.setView(customLayout);


        //Sorulan sorunun alınması ve methoda yerlestirilmesi
        gununSozuTextView.setText(getString(R.string.gunun_sozu));
        String ifade = getString(R.string.bana_gunun_sozunu_soyle);
        gununSozuResponse.setHint(getString(R.string.edithor_gunun_sozunu_dusunuyor));
        sendMessageGPT(ifade, gununSozuResponse, getApplicationContext());

        // add a button
        builder.setPositiveButton(getString(R.string.kopyala), (dialog, which) -> {
            // send data from the AlertDialog to the Activity
            String gptResponse = gununSozuResponse.getText().toString();
            util.getCopiedObject(getApplicationContext(), gptResponse); //Kopyalama islemi
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    //Send Message
    public void sendMessageGPT(String ifade, TextView text, Context context) {
        APIService service = new APIService();
        try {
            if (!Objects.equals(ifade, "")) {
                service.getResponse(ifade, text, context);
                Log.e("Girilen ifade: ", ifade);
            } else {
                Log.e("Girilen ifade: ", "İfade bos geliyor");

            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                //binding.NoteDetailImage.setImageBitmap(null);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.NoteDetailImage.setImageBitmap(bitmap);
                uploadImage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            capturedImage = (Bitmap) data.getExtras().get("data");
            ivOCR.setImageBitmap(capturedImage);
        }
    }

    //TODO: Storage üzerinden dowloadURL alıp firebase realtime db kayıt etmeli
    private void uploadImage() {
        if (imageUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Lütfen bekleyin...");
            progressDialog.show();

            ref.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            binding.NoteDetailImage.setVisibility(View.VISIBLE);
                            Toast.makeText(NoteDetail.this, "Yüklendi", Toast.LENGTH_SHORT).show();
                            // Log.e("Image URL", taskSnapshot.toString());
                            //Image Download link
                            ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    imageURL = task.getResult().toString();
                                    Log.i("URL", imageURL);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(NoteDetail.this, "Başarısız " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());

                            progressDialog.setMessage("Yükleniyor  " + (int) progress + "%");

                        }
                    });
        }
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
        removeRef = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Notlarim");

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
                            removedReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Silinen Notlarim");
                            //unique getKey()
                            NoteModel mNotes = new NoteModel(pos.getNoteID(), pos.getNotIcerigi(), pos.getNotBaslik(), pos.getNotOlusturmaTarihi(), false, pos.getColor(), util.getDateAnotherPattern(), pos.isPinned());
                            removedReference.child(notID).setValue(mNotes);
                            dialog.cancel();
                        }

                        //Snackbar Effect (Throws Exception)
                        //Set elevation 0f
                        util.configSnackbar(getApplicationContext(), snackbar);
                        snackbar.setAction("GERİ AL", view -> {
                            //Geri al dedikten sonra silinenlerden silinip yine eklenen notlara gecmesi
                            removedReference.child(notID).removeValue();

                            mAuth = FirebaseAuth.getInstance();
                            //Veritabanına Canlı Kayıt Etme (Realtime Database)
                            String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
                            mUser = mAuth.getCurrentUser();
                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Notlarim");

                            //yuklenen fotorafin storage adresi
                            final String image = pos.getImageUri() != null ? pos.getImageUri() : null;
                            //model
                            if (image != null) {
                                //unique getKey()
                                NoteModel mNotes = new NoteModel(pos.getNoteID(), pos.getNotIcerigi(), pos.getNotBaslik(), pos.getNotOlusturmaTarihi(), image, pos.isSelected(), pos.getColor(), util.getDateAnotherPattern(), pos.isPinned());
                                mDatabase.child(notID).setValue(mNotes);
                            } else {
                                //unique getKey()
                                NoteModel mNotes = new NoteModel(pos.getNoteID(), pos.getNotIcerigi(), pos.getNotBaslik(), pos.getNotOlusturmaTarihi(), pos.isSelected(), pos.getColor(), util.getDateAnotherPattern(), pos.isPinned());
                                mDatabase.child(notID).setValue(mNotes);
                            }


                            //TODO: 20 adet deneme yapıp en başarılı modeli bulup entegre edilecek.
                            //TODO: Modele sonra karar verilecek.


                        });
                        snackbar.setActionTextColor(getResources().getColor(R.color.button_active_color));
                        snackbar.show();

                        //Do something after 3500ms
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            Intent intent = new Intent(NoteDetail.this, NotePage.class);
                            startActivity(intent);
                        }, 3500);


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
        image = data.getStringExtra("image");
        pin = Boolean.valueOf(data.getStringExtra("pinned"));
    }


}