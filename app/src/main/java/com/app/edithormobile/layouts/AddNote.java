package com.app.edithormobile.layouts;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.edithormobile.NotePage;
import com.app.edithormobile.R;
import com.app.edithormobile.databinding.ActivityAddNoteBinding;
import com.app.edithormobile.models.NoteModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.UUID;

public class AddNote extends AppCompatActivity {

    EditText note, title;
    CharacterStyle styleBold, styleItalic, styleNormal, underLine;
    boolean bold, underline, italic = false;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private StorageReference storageRef;
    private Uri imageUri;
    ImageView imageNote;

    //Requests Code
    static final int REQUEST_IMAGE_CODE = 100;
    static final int STORAGE_REQUEST_CODE = 101;

    //Progress Dialog
    private ProgressDialog progressDialog;

    //Text Recognizer
    private TextRecognizer recognizer;

    //Permissions
    private String[] cameraPermission;
    private String[] storagePermission;


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

        //methods
        notKaydetmeIslevi();
        islemdenVazgec();
        optionsbarIslevi();


        //Permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //progress
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Lütfen bekleyin");
        progressDialog.setCanceledOnTouchOutside(false);

        //init TextRecognizer
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //Handle Click
        binding.imageNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputImageDialog();
            }
        });


        //share text
        binding.btnUploadImage.setOnClickListener(v -> shareNotes());

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

        //TODO: imageNote özelinde uzun basıldığında resmi önizleme özelliği olmalı.

    }
    
    
    //share notes
    private void shareNotes(){
        //Alan Tanımları
        String notIcerigi = binding.txtNote.getText().toString();
        String notBaslik = binding.txtTitle.getText().toString();
        /*Create an ACTION_SEND Intent*/
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        /*Applying information Subject and Body.*/
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, notBaslik);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, notIcerigi);
        /*Fire!*/
        startActivity(Intent.createChooser(intent, "Paylaş"));
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
                //yuklenen fotorafin storage adresi
                final String image = imageUri != null ? imageUri.toString() : null;

                //model
                if(image != null){
                    //unique getKey()
                    String id = mDatabase.push().getKey();
                    assert id != null;
                    NoteModel mNotes = new NoteModel( id, notIcerigi, notBaslik, notOlusturmaTarihi, image, false);
                    mDatabase.child(id).setValue(mNotes);
                }else{
                    //unique getKey()
                    String id = mDatabase.push().getKey();
                    assert id != null;
                    NoteModel mNotes = new NoteModel( id, notIcerigi, notBaslik, notOlusturmaTarihi, false);
                    mDatabase.child(id).setValue(mNotes);
                }

                Intent intent = new Intent(AddNote.this, NotePage.class);
                startActivity(intent);
                Toast.makeText(AddNote.this, "Not başarıyla oluşturuldu.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    //recognize text
    private void recognizeTextFromImage() {
        progressDialog.setMessage("Resim hazırlanıyor...");
        progressDialog.show();

        if(imageUri == null){
            Toast.makeText(AddNote.this, "Lütfen resim seçiniz", Toast.LENGTH_SHORT).show();
        }else{

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Not bulundu");
            builder.setMessage("Seçtiğiniz fotoğraf üzerinde not bulundu, eklemek ister misiniz?");
            builder.setNegativeButton("Hayır", (dialog, which) ->   progressDialog.dismiss());
            builder.setPositiveButton("Evet", (dialogInterface, i) -> {
                try {
                    InputImage inputImage = InputImage.fromFilePath(this, imageUri);
                    progressDialog.setMessage("Resim çözülüyor...");

                    Task<Text> textTaskResult = recognizer.process(inputImage).addOnSuccessListener(text -> {
                        progressDialog.dismiss();

                        String recognized = text.getText();
                        binding.txtNote.setText(recognized);
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(AddNote.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    });
                }catch (IOException e) {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed prepairing image.."+ e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.button_active_color));
            alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.button_active_color));



        }


    }

    //Popup Menu ile seceneklerin sorulması
    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.imageNote);

        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Fotoğraf çek");
        popupMenu.getMenu().add(Menu.NONE, 2,2, "Galeriden seç");

        popupMenu.show();

        //listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if(id ==1 ){
                if(checkCameraPermission()){
                    pickImageCamera();
                }else {
                    requestCameraPermission();
                }
            }else if(id == 2){
                if(checkStoragePermission()){
                    pickImageGallery();
                }else{
                    requestStoragePermission();
                }
            }
            return false;
        });

    }

    //Galeriden fotograf secmek
    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                imageUri = result.getData().getData();
                //set img view
                binding.imageNote.setImageURI(imageUri);
                recognizeTextFromImage();
            } else {
                Toast.makeText(AddNote.this, "Cancelled...", Toast.LENGTH_SHORT).show();
            }
        }
    });

    //Kameradan fotograf cekimi
    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Ornek baslik");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Ornek aciklama");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                binding.imageNote.setImageURI(imageUri);
                recognizeTextFromImage();
            } else {
                Toast.makeText(AddNote.this, "Cancelled...", Toast.LENGTH_SHORT).show();
            }
        }
    });

    //Galeri Erisim İzni Kontrolü
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    //Galeri Erisim İzni İsteği
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    //Kamera Erisim İzni Kontrolü
    private boolean checkCameraPermission() {
        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return cameraResult == storageResult;
    }

    //Kamera Erisim İzni İsteği
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, REQUEST_IMAGE_CODE);
    }


    //İzin istegi sonucunun kontrolu
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_IMAGE_CODE: {

                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        pickImageCamera();
                    } else {
                        Toast.makeText(this, "Kamera ve Depolama izni gerekli.", Toast.LENGTH_SHORT).show();
                    }

                }

            }
            case STORAGE_REQUEST_CODE: {

                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted) {
                        pickImageGallery();
                    } else {
                        Toast.makeText(this, "Depolama izni gerekli", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }

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
                            taskSnapshot -> Toast.makeText(AddNote.this, "Fotograf yukleme basarılı", Toast.LENGTH_SHORT).show())

                    .addOnFailureListener(e -> Toast.makeText(AddNote.this, "Failed", Toast.LENGTH_SHORT).show());
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
                passedStyle = styleBold;

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
                passedStyle = styleItalic;

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
                passedStyle = underLine;

            } else {
                passedStyle = underLine;

            }
            sb.setSpan(passedStyle, start, end, 0);
            binding.txtNote.setText(sb);

        });

        //texti kopyalar
        binding.btncopy.setOnClickListener(v -> binding.txtNote.getText().toString());
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
