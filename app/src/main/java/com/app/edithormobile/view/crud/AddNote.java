package com.app.edithormobile.view.crud;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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

import com.app.edithormobile.R;
import com.app.edithormobile.adapters.NoteAdapter;
import com.app.edithormobile.databinding.ActivityAddNoteBinding;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.util.IToast;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.NotePage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.UUID;

import yuku.ambilwarna.AmbilWarnaDialog;

public class AddNote extends AppCompatActivity implements IToast {

    //TODO: Fotograf seçtirirken Galeri intenti açılıyor. Bu çözülecek.
    //TODO: Fotograf eklerken kaydetmede hata alınıyor.
    Util util = new Util();
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private StorageReference storageRef;
    private Uri imageUri;
    NoteAdapter adapter;
    ArrayList<NoteModel> notes;

    //color picker
    int defaultColor;

    //Requests Code
    static final int REQUEST_IMAGE_CODE = 100;
    static final int STORAGE_REQUEST_CODE = 101;


    //Text Recognizer
    private TextRecognizer recognizer;

    //Permissions
    private String[] cameraPermission;
    private String[] storagePermission;

    String notBasligi, notIcerigi, notOlusturmaZamani, notID, olusturma_zamani;
    int notRengi;


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
        notKaydet();

        //open color picker
        binding.btnColor.setOnClickListener(v -> openColorPicker());


        //TODO: Nota tıklandığında Güncelle butonu ortaya çıksın ve işlev yürütülsün.


        //Permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //color picker
        defaultColor = ContextCompat.getColor(AddNote.this, R.color.cardview_color);

        //init TextRecognizer
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        //Handle Click
        binding.imageNote.setOnClickListener(v -> showInputImageDialog());
        //share text
        binding.btnUploadImage.setOnClickListener(v -> shareNotes());
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        //TODO: imageNote özelinde uzun basıldığında resmi önizleme özelliği olmalı.

        //Get data
        getIntentData();
        assignData(notBasligi, notIcerigi);

    }//eof onCreate

    @Override
    protected void onStart() {
        super.onStart();
        //Not kaydetme işlemi
        //notKaydet();
        //notGuncelleme();
        buttonTask();
        //TODO: Not guncelleme NoteDetail sayfasına alınacak.
        //TODO: UI screen bilgisayarda dokumanlarda.

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


    //assign data
    public void assignData(String baslik, String icerik) {
        binding.txtTitle.setText(baslik);
        binding.txtNote.setText(icerik);
    }


    //Color picker dialog
    private void openColorPicker() {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                binding.addNoteBG.setBackgroundColor(getResources().getColor(R.color.bg_color_light));
                binding.btnColor.setIconTint(ColorStateList.valueOf(getResources().getColor(R.color.bg_color_light)));
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                defaultColor = color;
                binding.btnColor.setIconTint(ColorStateList.valueOf(color));
            }
        });
        dialog.show();
        dialog.getDialog().getButton(dialog.getDialog().BUTTON_NEGATIVE).setTextColor(getColor(R.color.button_active_color));
        dialog.getDialog().getButton(dialog.getDialog().BUTTON_POSITIVE).setTextColor(getColor(R.color.button_active_color));
    }

    //TODO: Güncelleme işlemi yapıyor fakat buton değiştirilmeli.
    // Aynı buton olduğundan intent tarafından gelen değer null geliyor not eklemek istediğimizde.


    /* public void notGuncelleme() {
        //deger alma (update first step)
        binding.txtTitle.setText(getIntent().getStringExtra("baslik"));
        binding.txtNote.setText(getIntent().getStringExtra("icerik"));
        Glide.with(this)
                .load(getIntent().getStringExtra("image"))
                .into(binding.imageNote);

        String noteID = getIntent().getStringExtra("id");
        NoteModel position = (NoteModel) getIntent().getSerializableExtra("position");
        //Log.e("position degeri", position);

        binding.btnNotuKaydet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("notBaslik", binding.txtTitle.getText().toString());
                map.put("notIcerigi", binding.txtNote.getText().toString());

                //db ref
                mAuth = FirebaseAuth.getInstance();
                mUser = mAuth.getCurrentUser();
                String user_id = requireNonNull(mUser).getUid();
                FirebaseDatabase.getInstance()
                        .getReference().child("Kullanicilar").child(user_id).child("Notlarim").child(noteID).updateChildren(map)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    String notOlusturmaZamani = olusturmaZamaniGetir();

                                    position.setNotBaslik(binding.txtTitle.getText().toString());
                                    position.setNotIcerigi(binding.txtTitle.getText().toString());
                                    position.setNotOlusturmaTarihi(notOlusturmaZamani);
                                    //TODO: Position nesnesi ile o pozisyona ait object alınıyor.
                                    //adapter.notifyDataSetChanged(); //null dönüyor!!!

                                    Toast("Notunuz güncellendi");
                                    Intent intent = new Intent(AddNote.this, NotePage.class);
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
        });


    }

     */

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

    //share notes
    private void shareNotes() {
        //Alan Tanımları
        String notIcerigi = binding.txtNote.getText().toString();
        String notBaslik = binding.txtTitle.getText().toString();
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, notBaslik);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, notIcerigi);

        startActivity(Intent.createChooser(intent, "Paylaş"));
    }

    public void buttonTask() {
        //Buton seçimlerine göre renk degisimi
        binding.btnToolbarColorRedAddNote.setOnClickListener(v -> {
            notRengi = getColor(R.color.red_circle);
            binding.addNoteBG.setBackgroundColor(getResources().getColor(R.color.red_circle));
        });

        binding.btnToolbarColorBlueAddNote.setOnClickListener(v -> {
            notRengi = getColor(R.color.blue_circle);
            binding.addNoteBG.setBackgroundColor(getResources().getColor(R.color.blue_circle));
        });

        binding.btnToolbarColorGreenAddNote.setOnClickListener(v -> {
            notRengi = getColor(R.color.green_circle);
            binding.addNoteBG.setBackgroundColor(getResources().getColor(R.color.green_circle));
        });

        //TODO: Light temada herhangi bir renk seçmediğimizde sorun yok, dark theme de seçince light moda dönüşte yazılar görünmüyor.
        binding.btnToolbarColorEmptyAddNote.setOnClickListener(v -> {
            notRengi = getColor(R.color.bg_color_light);
            binding.addNoteBG.setBackgroundColor(getResources().getColor(R.color.bg_color_light));
        });
    }


    private void notKaydet() {
        binding.btnBack.setOnClickListener(v -> {
            //Eğer not aynı kaldıysa olusturma zamanını guncellemesin.
            olusturma_zamani = olusturmaZamaniGetir();
            notKaydetmeIslevi();

        });
    }


    //not kaydet
    public void notKaydetmeIslevi() {
        mAuth = FirebaseAuth.getInstance();
        //Veritabanına Canlı Kayıt Etme (Realtime Database)
        String user_id = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Kullanicilar").child(user_id).child("Notlarim");

        //Alan Tanımları
        String notIcerigi = binding.txtNote.getText().toString();
        String notBaslik = binding.txtTitle.getText().toString();

        if (TextUtils.isEmpty(notBaslik) && TextUtils.isEmpty(notIcerigi)) {
            Intent intent = new Intent(AddNote.this, NotePage.class);
            startActivity(intent);
        } else {

            String notOlusturmaTarihi = olusturmaZamaniGetir();
            //yuklenen fotorafin storage adresi
            final String image = imageUri != null ? imageUri.toString() : null;

            //model
            if (image != null) {
                //unique getKey()
                String id = mDatabase.push().getKey();
                assert id != null;
                NoteModel mNotes = new NoteModel(id, notIcerigi, notBaslik, notOlusturmaTarihi, image, false, defaultColor);
                mDatabase.child(id).setValue(mNotes);

                //intent
                Intent intent = new Intent(AddNote.this, NotePage.class);
                intent.putExtra("id", id);
                startActivity(intent);
                util.toastMessage(getApplicationContext(), "Not başarıyla oluşturuldu");


            } else {
                //unique getKey()
                String id = mDatabase.push().getKey();
                assert id != null;
                NoteModel mNotes = new NoteModel(id, notIcerigi, notBaslik, notOlusturmaTarihi, false, defaultColor);
                mDatabase.child(id).setValue(mNotes);


                //intent
                Intent intent = new Intent(AddNote.this, NotePage.class);
                intent.putExtra("id", id);
                startActivity(intent);
                util.toastMessage(getApplicationContext(), "Not başarıyla oluşturuldu");
            }
        }
    }


    //Recognize text
    private void recognizeTextFromImage() {
        //Eger uri degeri bos degilse:
        if (imageUri == null) {
            Toast("Lütfen resim seçiniz");
            util.toastMessage(getApplicationContext(), "Lütfen resim seçiniz");
        } else {
            InputImage inputImage = null;
            try {
                inputImage = InputImage.fromFilePath(this, imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert inputImage != null;
            Task<Text> result = recognizer.process(inputImage);
            result.addOnSuccessListener(text -> {
                String recognized = text.getText();
                if (!recognized.equals("")) {
                    //Alert ile uyarı belirt.
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNote.this);
                    builder.setTitle("Not Taraması");
                    builder.setMessage("Fotoğrafta not bulundu, notunuza eklensin mi?");
                    builder.setNegativeButton("Hayır", (dialog, which) -> Toast("İçerik alınmadı"));
                    builder.setPositiveButton("Evet", (dialogInterface, i) -> {
                        binding.txtNote.setText(recognized);
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.button_active_color));
                    alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.button_active_color));
                    alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
                }

            });
        }
    }
    //TODO: Recognize islemi ve depolamalar yeniden yazılacak.
    //TODO: Nota tıklanıldığında okumak için detay sayfasına gidilmeli.


    //Popup Menu ile seceneklerin sorulması
    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.imageNote);
        popupMenu.getMenu().add(Menu.NONE, 1, 1, "Fotoğraf çek");
        popupMenu.getMenu().add(Menu.NONE, 2, 2, "Galeriden seç");

        popupMenu.show();

        //listener
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == 1) {
                if (checkCameraPermission()) {
                    pickImageCamera();
                } else {
                    requestCameraPermission();
                }
            } else if (id == 2) {
                if (checkStoragePermission()) {
                    pickImageGallery();
                } else {
                    requestStoragePermission();
                }
            }
            return false;
        });

    }
    //TODO: Kamera ve depolama isteği istemeden izin gerekli diyor. İstek atmalı.

    //Galeri Erisim İzni İsteği
    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    //Kamera Erisim İzni İsteği
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermission, REQUEST_IMAGE_CODE);
    }

    //Galeri Erisim İzni Kontrolü
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    //Kamera Erisim İzni Kontrolü
    private boolean checkCameraPermission() {
        boolean cameraResult = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        return cameraResult == storageResult;
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
                        Toast("Kamera ve Depolama izni gerekli");
                    }

                }

            }
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickImageGallery();
                    } else {
                        Toast("Depolama izni gerekli");
                    }
                }

            }
        }

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
                assert result.getData() != null;
                imageUri = result.getData().getData();
                //set img view
                binding.imageNote.setImageURI(imageUri);
                uploadImage();
                recognizeTextFromImage();
            } else {
                Toast("Vazgeçildi");
            }
        }
    });

    //Kameradan fotograf cekimi
    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "");
        values.put(MediaStore.Images.Media.DESCRIPTION, "");

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
                uploadImage();
                recognizeTextFromImage();
            } else {
                Toast("Vazgecildi");

            }
        }
    });


    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        int PICK_IMAGE_REQUEST = 22;
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                imageUri);
                binding.imageNote.setImageBitmap(bitmap);
            } catch (IOException e) {
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
            ref.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String url = String.valueOf(ref.getDownloadUrl());
                    Log.d("Upload_Success", "Fotograf basarıyla yuklendi");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Upload_Failed", "Fotograf yüklemesi basarisiz");
                }
            });
        }
    }

    @Override
    public void Toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

