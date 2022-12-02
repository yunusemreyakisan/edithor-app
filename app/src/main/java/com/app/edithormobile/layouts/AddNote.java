package com.app.edithormobile.layouts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.app.edithormobile.NotePage;
import com.app.edithormobile.databinding.ActivityAddNoteBinding;
import com.app.edithormobile.models.NoteModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.UUID;

public class AddNote extends AppCompatActivity {

    Button btnNotuKaydet, btnBack;
    Button btnBold, btnItalic, btnUnderline, btnCopy, btnColor, btnUploadImage;
    EditText note, title;
    CharacterStyle styleBold, styleItalic, styleNormal, underLine;
    boolean bold, underline, italic = false;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private StorageReference storageRef;
    private Uri imageUri;
    ImageView imageNote;


    // request code
    private final int PICK_IMAGE_REQUEST = 22;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;

    ActivityAddNoteBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
       // view binding
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        notKaydetmeIslevi();
        islemdenVazgec();
        optionsbarIslevi();
        btnUploadImageIslevi();


        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //deger alma (update first step)
        binding.txtTitle.setText(getIntent().getStringExtra("baslik"));
        binding.txtNote.setText(getIntent().getStringExtra("icerik"));
        Glide.with(this)
                .load(getIntent().getStringExtra("image"))
                .into(binding.imageNote);

        //Styles
        styleBold = new StyleSpan(Typeface.BOLD);
        styleNormal = new StyleSpan(Typeface.NORMAL);
        styleItalic = new StyleSpan(Typeface.ITALIC);
        underLine = new UnderlineSpan();

    }


    private void islemdenVazgec() {
        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AddNote.this, NotePage.class);
            startActivity(intent);
        });
    }

    //not kaydet
    private void notKaydetmeIslevi() {
        mAuth = FirebaseAuth.getInstance();
        binding.btnNotuKaydet.setOnClickListener(view -> {
            //Veritabanına Canlı Kayıt Etme (Realtime Database)
            String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            mUser = mAuth.getCurrentUser();
            mDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("Kullanicilar").child(user_id).child("Notlarim");

            //Alan Tanımları
            String notIcerigi = binding.txtNote.getText().toString();
            String notBaslik = binding.txtTitle.getText().toString();

            if (TextUtils.isEmpty(notIcerigi)) {
                Toast.makeText(AddNote.this, "Not içeriği giriniz.", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(notBaslik)) {
                Toast.makeText(AddNote.this, "Başlık boş bırakılamaz.", Toast.LENGTH_SHORT).show();
            } else {
                //Olusturma zamanini al.
                Calendar calendar = new GregorianCalendar();
                int month = calendar.get(Calendar.MONTH) + 1; //0 ile basladigi icin 1 eklendi.
                int hours = calendar.get(Calendar.HOUR);
                int minutes = calendar.get(Calendar.MINUTE);
                String time = String.format("%02d:%02d", hours, minutes);
                String notOlusturmaTarihi = calendar.get(Calendar.DAY_OF_MONTH) + "/" + month
                        + " " + time;


                String uri = imageUri.toString();

                //unique getKey()
                String id = mDatabase.push().getKey();
                NoteModel mNotes = new NoteModel(id, notIcerigi, notBaslik, notOlusturmaTarihi, uri, false);
                assert id != null;
                mDatabase.child(id).setValue(mNotes);

                Intent intent = new Intent(AddNote.this, NotePage.class);
                startActivity(intent);
                Toast.makeText(AddNote.this, "Not başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    //resim yukle
    private void btnUploadImageIslevi() {
        binding.btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
    }


    //resim sec
    private void SelectImage() {
        //Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
        uploadImage();
    }

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            imageUri = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                imageUri);
                binding.imageNote.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }


    // UploadImage method
    private void uploadImage() {
        if (imageUri != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(imageUri)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(AddNote.this,
                                                    "Fotograf yüklendi",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(AddNote.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            });
        }
    }



    private void optionsbarIslevi() {
        //bold yapar
        binding.btnbold.setOnClickListener(v -> {
            // Toast.makeText(AddNote.this, "Tıklandı Bold", Toast.LENGTH_SHORT).show();

            bold = !bold;
            String wholeText = binding.txtNote.getText().toString();
            int start = binding.txtNote.getSelectionStart();
            int end = binding.txtNote.getSelectionEnd();

            CharacterStyle passedStyle;
            SpannableStringBuilder sb = new SpannableStringBuilder(wholeText);

            if (bold) {
                passedStyle = styleNormal;

            } else {
                passedStyle = styleBold;
            }
            sb.setSpan(passedStyle, start, end, 0);
            binding.txtNote.setText(sb);
        });


        //italic yapar
        binding.btnitalic.setOnClickListener(v -> {

            italic = !italic;
            String wholeText = binding.txtNote.getText().toString();
            int start = binding.txtNote.getSelectionStart();
            int end = binding.txtNote.getSelectionEnd();

            CharacterStyle passedStyle;
            SpannableStringBuilder sb = new SpannableStringBuilder(wholeText);
            if (italic) {
                passedStyle = styleNormal;

            } else {
                passedStyle = styleItalic;

            }
            sb.setSpan(passedStyle, start, end, 0);
            binding.txtNote.setText(sb);

        });

        //altini cizer
        binding.btnunderline.setOnClickListener(v -> {
            underline = !underline;
            String wholeText = binding.txtNote.getText().toString();
            int start = binding.txtNote.getSelectionStart();
            int end = binding.txtNote.getSelectionEnd();

            CharacterStyle passedStyle;
            SpannableStringBuilder sb = new SpannableStringBuilder(wholeText);
            if (underline) {
                passedStyle = styleNormal;

            } else {
                passedStyle = underLine;

            }
            sb.setSpan(passedStyle, start, end, 0);
            binding.txtNote.setText(sb);

        });

        //texti kopyalar
        binding.btncopy.setOnClickListener(v -> note.getText().toString());
    }
    /*
        //renk degistirir
        btnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note.setTypeface(note.getTypeface(), Typeface.BOLD);

            }
        });
        //resim ekler
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note.setTypeface(note.getTypeface(), Typeface.BOLD);
            }
        });

    }

     */



}
