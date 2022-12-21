package com.app.edithormobile.layouts.upload;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.app.edithormobile.databinding.ActivityUploadFileBinding;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class UploadFile extends AppCompatActivity {

    private Uri imageUri = null;

    //Requests Code
    static final int REQUEST_IMAGE_CODE = 100;
    static final int STORAGE_REQUEST_CODE = 101;

    ActivityUploadFileBinding binding;

    //Text Recognizer
    private TextRecognizer recognizer;

    //Permissions
    private String[] cameraPermission;
    private String[] storagePermission;

    //Progress Dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadFileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

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
        binding.ocrCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputImageDialog();
            }
        });

        //recognize text
        binding.ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri == null){
                    Toast.makeText(UploadFile.this, "Lütfen resim seçiniz", Toast.LENGTH_SHORT).show();
                }else{
                    recognizeTextFromImage();
                }
            }
        });

    }


    //recognize text
    private void recognizeTextFromImage() {
        progressDialog.setMessage("Resim hazırlanıyor...");
        progressDialog.show();

        try {
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);
            progressDialog.setMessage("Resim çözülüyor...");

            Task<Text> textTaskResult = recognizer.process(inputImage).addOnSuccessListener(text -> {
                progressDialog.dismiss();

                String recognized = text.getText();
                binding.textData.setText(recognized);
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(UploadFile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            });
        }catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed prepairing image.."+ e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    //Popup Menu ile seceneklerin sorulması
    private void showInputImageDialog() {
        PopupMenu popupMenu = new PopupMenu(this, binding.ocrCaptureButton);

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
                binding.image.setImageURI(imageUri);
            } else {
                Toast.makeText(UploadFile.this, "Cancelled...", Toast.LENGTH_SHORT).show();
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
                binding.image.setImageURI(imageUri);
            } else {
                Toast.makeText(UploadFile.this, "Cancelled...", Toast.LENGTH_SHORT).show();
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

                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){
                        pickImageGallery();
                    }else{
                        Toast.makeText(this, "Depolama izni gerekli", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        }

    }
}
