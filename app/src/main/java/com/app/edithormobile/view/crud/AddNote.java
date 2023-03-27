package com.app.edithormobile.view.crud;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;

import com.app.edithormobile.R;
import com.app.edithormobile.adapters.NoteAdapter;
import com.app.edithormobile.databinding.ActivityAddNoteBinding;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.util.IToast;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.NotePage;
import com.app.edithormobile.viewmodel.NoteDetailViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class AddNote extends AppCompatActivity implements IToast {

    //TODO: Fotograf seçtirirken Galeri intenti açılıyor. Bu çözülecek.
    //TODO: Fotograf eklerken kaydetmede hata alınıyor.
    Util util = new Util();
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri imageUri;
    String imageURL;
    private final int PICK_IMAGE_REQUEST = 71;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    NoteAdapter adapter;
    ArrayList<NoteModel> notes;
    String notBasligi, notIcerigi, notOlusturmaZamani, notID, olusturma_zamani;
    int notRengi;
    StorageReference ref;
    String currentPhotoPath;
    ActivityAddNoteBinding binding;
    NoteDetailViewModel noteDetailViewModel;
    ImageView ivOCR;
    TextView tvResponse;
    Bitmap capturedImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        // view binding
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //Viewmodel binding
        noteDetailViewModel = ViewModelProviders.of(this).get(NoteDetailViewModel.class);

        //methods
        notKaydet();
        //Get data
        getIntentData();
        assignData(notBasligi, notIcerigi);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        ref = storageReference.child("harici/" + UUID.randomUUID().toString());


    }//eof onCreate

    @Override
    protected void onStart() {
        super.onStart();
        buttonTask();
        olusturma_zamani = util.olusturmaZamaniGetir();
        binding.tvSonDuzenlemeZamaniToolbarAddNote.setText(olusturma_zamani);


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

        //Plus Extra Properties Bottom Sheet
        BottomSheetDialog dialogPlus = new BottomSheetDialog(AddNote.this);
        View viewPlus = LayoutInflater.from(getApplicationContext()).inflate(R.layout.extra_bottom_sheet_layout, null);
        dialogPlus.setContentView(viewPlus);
        binding.btnToolbarPropertiesAddNote.setOnClickListener(v -> {
            //show
            dialogPlus.show();
        });

        //Take Photo
        viewPlus.findViewById(R.id.takePhotoToolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Fotograf çekme ve algılama işlemi
                Toast.makeText(AddNote.this, "Fotograf sec tıklanıldı", Toast.LENGTH_SHORT).show();
            }
        });

        //Choose Photo
        viewPlus.findViewById(R.id.choosePhotoToolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Choose Photo", "Fotograf seçmeye tıklanıldı");
                chooseImage();
            }
        });

        //OCR
        viewPlus.findViewById(R.id.create_ocr_toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOCRDialog();
                //TODO: OCR için ayrıca hocaya sor. Tasarımı ve işleyişi ile alakalı.
            }
        });

        //GPT Toolbar Button
        BottomSheetDialog dialogGPT = new BottomSheetDialog(AddNote.this);
        View viewGPT = LayoutInflater.from(getApplicationContext()).inflate(R.layout.gpt_bottom_sheet_dialog, null);
        dialogGPT.setContentView(viewGPT);
        binding.shineToolbarGpt.setOnClickListener(v -> dialogGPT.show());

        //Custom layout for ask question
        viewGPT.findViewById(R.id.bottom_sheet_gpt_question_layout).setOnClickListener(this::showAlertDialogButtonClicked);

    }

    public void showOCRDialog() {
        // Create an alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the custom layout
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_ocr_dialog, null);
        builder.setView(customLayout);

        //Custom layout öğelerine erişim
        tvResponse = (TextView) customLayout.findViewById(R.id.tvOCRResponse);
        ivOCR = (ImageView) customLayout.findViewById(R.id.ivOCRToolbar);

        ivOCR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // İzin isteği gösterme
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        // İzinler zaten verilmiş
                        Log.e("Permission", "Kameraya izin verildi");
                        openCamera();
                    }
                } else {
                    // Android 6.0'dan önceki sürümlerde izinler zaten verilmiştir
                    openCamera();
                }
            }
        });

        // add a button
        builder.setPositiveButton("Kopyala", (dialog, which) -> {
            // send data from the AlertDialog to the Activity
            String gptResponse = "Merhaba";
            util.getCopiedObject(getApplicationContext(), gptResponse); //Kopyalama islemi
        });
        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
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
            noteDetailViewModel.sendMessageGPT(ifade, tvResponse, getApplicationContext());
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

    //Recognizer
    private void recognizeText() {
        InputImage image = InputImage.fromBitmap(capturedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                StringBuilder result = new StringBuilder();
                for (Text.TextBlock block : text.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        tvResponse.setText(blockText);
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast("Fotoğraftan okuma başarısız oldu");
                Log.e("Detect Image", "Fotoğraftan okuma başarısız oldu" + e.getMessage());
            }
        });
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                binding.addNoteToolbarImage.setImageBitmap(bitmap);
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
                            binding.addNoteToolbarImage.setVisibility(View.VISIBLE);
                            Toast.makeText(AddNote.this, "Yüklendi", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddNote.this, "Başarısız " + e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void notKaydet() {
        binding.btnBack.setOnClickListener(v -> {
            //Eğer not aynı kaldıysa olusturma zamanını guncellemesin.
            olusturma_zamani = util.olusturmaZamaniGetir();
            notKaydetmeIslevi();
        });
    }


    //Save new note
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

            String notOlusturmaTarihi = util.olusturmaZamaniGetir();
            //yuklenen fotorafin storage adresi
            //final String image = imageUri != null ? imageUri.toString() : null;

            //model
            if (imageURL != null) {
                //unique getKey()
                String id = mDatabase.push().getKey();
                assert id != null;
                NoteModel mNotes = new NoteModel(id, notIcerigi, notBaslik, notOlusturmaTarihi, imageURL, false, notRengi, util.getDateAnotherPattern());
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
                NoteModel mNotes = new NoteModel(id, notIcerigi, notBaslik, notOlusturmaTarihi, false, notRengi, util.getDateAnotherPattern());
                mDatabase.child(id).setValue(mNotes);


                //intent
                Intent intent = new Intent(AddNote.this, NotePage.class);
                intent.putExtra("id", id);
                startActivity(intent);
                util.toastMessage(getApplicationContext(), "Not başarıyla oluşturuldu");
            }
        }
    }

    //TODO: Galeri veya fotoğraf secimi yaptırılacak. OCR entegre edilecek.


    @Override
    public void Toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

