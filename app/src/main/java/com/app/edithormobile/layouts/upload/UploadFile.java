package com.app.edithormobile.layouts.upload;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.edithormobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

public class UploadFile extends AppCompatActivity {

    Button upload;
    ProgressBar bar;
    ImageView image;
    private DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private StorageReference storageRef;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);
        //Methods
        initComponents();
        //dbRef();
        //fotografYukle();

        //Default
        bar.setVisibility(View.INVISIBLE);

    }
    //init
    private void initComponents() {
        upload = findViewById(R.id.upload);
        bar = findViewById(R.id.bar);
        image = findViewById(R.id.image);
    }

/*
    private void dbRef() {
        mAuth = FirebaseAuth.getInstance();
            //Veritabanına Canlı Kayıt Etme (Realtime Database)
            NoteModel model = new NoteModel();
            mUser = mAuth.getCurrentUser();
            String user_id = Objects.requireNonNull(mUser).getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("Kullanicilar").child(user_id).child("Notlarim").child(model.getNoteID()).child("images");

            //Storage
            storageRef= FirebaseStorage.getInstance().getReference();

    }
/*
    private void fotografYukle() {
        upload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            Intent chooser = Intent.createChooser(intent, "Choose a Picture");
            startActivityForResult(chooser, 1);

            //Storage
            if(imageUri!=null){
                uploadFirebase(imageUri);
            }else{
                Toast.makeText(this, "Lutfen fotograf seciniz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!= null){
            imageUri=data.getData();
            image.setImageURI(imageUri);

        }
    }

 */

    /*
    //upload Firebase
    private void uploadFirebase(Uri uri){
        StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            NoteModel model = new NoteModel(uri.toString());
                            String modelID= mDatabase.push().getKey();
                            assert modelID != null;
                            mDatabase.child(modelID).setValue(model);
                            Intent image = new Intent(UploadFile.this, AddNote.class);
                            image.putExtra("image",modelID );
                            startActivity(image);

                            bar.setVisibility(View.INVISIBLE);

                            Toast.makeText(UploadFile.this, "Yukleme basarili", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            bar.setVisibility(View.VISIBLE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                bar.setVisibility(View.INVISIBLE);
                Toast.makeText(UploadFile.this, "Yukleme basarisiz oldu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileExtension(Uri mUri){
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(mUri));
    }


     */



}
