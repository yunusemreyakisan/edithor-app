package com.app.edithormobile.view.home;

import static java.util.Objects.requireNonNull;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.edithormobile.R;
import com.app.edithormobile.adapter.NoteAdapter;
import com.app.edithormobile.databinding.ActivityNotePageBinding;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.util.ISnackbar;
import com.app.edithormobile.util.IToast;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.detail.NoteDetail;
import com.app.edithormobile.view.login.SignIn;
import com.app.edithormobile.viewmodel.home.NotePageViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

/**
 * @author yunusemreyakisan
 */

public class NotePage extends AppCompatActivity implements IToast, ISnackbar {

    ArrayList<NoteModel> selectedNotes = new ArrayList<>();
    Util util = new Util();
    boolean isSelectedMode = false;
    ArrayList<NoteModel> notes;
    NoteAdapter noteAdapter;
    DatabaseReference mDatabaseReference;
    DatabaseReference removeRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;

    ActivityNotePageBinding binding;
    Bitmap bmp; // store the image in your bitmap
    NotePageViewModel viewModel;
    View filterView;
    BottomSheetDialog filterDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        //ViewBinding
        binding = ActivityNotePageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //DB Ref
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mUser).getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Notlarim");

        //Todo: Aşağıdaki methodları viewmodele taşı.
        //ViewModel Binding
        viewModel = ViewModelProviders.of(this).get(NotePageViewModel.class);

        binding.noData.setVisibility(View.INVISIBLE);
        binding.notFound.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        //Methods
        viewModel.fabControl(binding, getApplicationContext());

        //Filter Color
        filterDialog = new BottomSheetDialog(NotePage.this);
        filterView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_color_filter_layout, null);
        filterDialog.setContentView(filterView);

    }


    @Override
    protected void onStart() {
        super.onStart();
        //onStart modunda yenilensin.
        notesViewRV();
        degisikligiBildir();
        //Search Func.
        viewModel.search(binding, noteAdapter, notes);
        //Swipe Listener Func.
        viewModel.swipeListener(binding, noteAdapter, mDatabaseReference, notes);
        //Selected Notes Toolbar Func.
        selectedNotesRemove();
        //Filter Color
        viewModel.filterColor(binding, filterDialog);
        //Filter Color Actions
        viewModel.filterColorActions(filterView, getApplicationContext());
        //Sorting
        Collections.sort(notes);
        noteAdapter.notifyDataSetChanged();
    }


    //Selected Notes Toolbar Func.
    public void selectedNotesRemove(){
        binding.toolbarSecilenler.setNavigationOnClickListener(v1 -> {
            binding.toolbarTopNotePage.setVisibility(View.VISIBLE);
            binding.toolbarSecilenler.setVisibility(View.GONE);
            selectedNotes.clear();
            notes.clear();
            degisikligiBildir();
        });

        binding.btnDeleteToolbarNotepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: verileri siliyoruz, fakat adapter listesi güncellenmiyor.
                for (NoteModel model : selectedNotes) {
                    String id = model.getNoteID();
                    removeRef.child(id).removeValue();
                }

                selectedNotes.removeAll(selectedNotes);
                binding.toolbarTopNotePage.setVisibility(View.VISIBLE);
                binding.toolbarSecilenler.setVisibility(View.GONE);
                Log.e("secilenler listesi", selectedNotes.toString());
                notes.clear();
                degisikligiBildir();
            }
        });
    }

    //Degisiklik izleme
    public void degisikligiBildir() {
        viewModel.notesEventChangeListener(binding, noteAdapter, mDatabaseReference, notes);
        noteAdapter.notifyDataSetChanged();
    }

    //Recyclerview
    private void notesViewRV() {
        //Remove reference
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
        removeRef = FirebaseDatabase.getInstance().getReference().child("Kullanicilar")
                .child(user_id).child("Notlarim");

        notes = new ArrayList<>();
        noteAdapter = new NoteAdapter(NotePage.this, notes, new NoteAdapter.ClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // Toast.makeText(NotePage.this, "Kısa basıldı", Toast.LENGTH_SHORT).show();
                NoteModel model = notes.get(position);
                Intent intent = new Intent(NotePage.this, NoteDetail.class);
                intent.putExtra("id", notes.get(position).getNoteID());
                intent.putExtra("baslik", notes.get(position).getNotBaslik());
                intent.putExtra("icerik", notes.get(position).getNotIcerigi());
                intent.putExtra("olusturma_zamani", notes.get(position).getNotOlusturmaTarihi());
                intent.putExtra("color", notes.get(position).getColor());
                intent.putExtra("position", model);
                if(notes.get(position).getImageUri() != null){
                    intent.putExtra("image", notes.get(position).getImageUri());
                    Toast.makeText(NotePage.this, "Gonderilen image: " + notes.get(position).getImageUri(), Toast.LENGTH_SHORT).show();
                }
                intent.putExtra("pinned", notes.get(position).isPinned());
                startActivity(intent);
            }


            @Override
            public void onItemLongClick(View v, int position) {
                String id = notes.get(position).getNoteID();
                NoteModel pos = notes.get(position);
                mAuth = FirebaseAuth.getInstance();
                mUser = mAuth.getCurrentUser();
                String user_id = requireNonNull(mAuth.getCurrentUser()).getUid();
                removeRef = FirebaseDatabase.getInstance().getReference().child("Kullanicilar")
                        .child(user_id).child("Notlarim");



                //TODO: Toolbar inflate olsun ve seçilen not sayısını yazsın.
                binding.toolbarTopNotePage.setVisibility(View.GONE);
                binding.toolbarSecilenler.setVisibility(View.VISIBLE);


                //TODO:Seçilim
                isSelectedMode = true;
                if (selectedNotes.contains(pos)) {
                    v.setForeground(null);
                    selectedNotes.remove(pos);
                } else {
                    selectedNotes.add(pos);
                    v.setForeground(getResources().getDrawable(R.drawable.custom_foreground));
                    binding.tvToolbarListSize.setText(String.valueOf("Seçilen not sayısı: " + selectedNotes.size()));
                }

                if (selectedNotes.size() == 0) {
                    isSelectedMode = false;
                    binding.toolbarTopNotePage.setVisibility(View.VISIBLE);
                    binding.toolbarSecilenler.setVisibility(View.GONE);
                }

                binding.tvToolbarListSize.setText(String.valueOf(selectedNotes.size()));
                Log.e("secilenler listesi", selectedNotes.toString());

            }
        });
        binding.rvNotes.setHasFixedSize(true);
        StaggeredGridLayoutManager sgm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        binding.rvNotes.setLayoutManager(sgm);
        binding.rvNotes.setAdapter(noteAdapter);
    }

    //Back Pressed
    @Override
    public void onBackPressed() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            mGoogleSignInClient.signOut();
        }
        //Toast.makeText(MainActivity.this, "Back pressed", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(NotePage.this);
        // Pencere Baslik Tanımı
        builder.setTitle("Emin misiniz?");
        // Pencere Mesaj Tanımı
        builder.setMessage("Çıkış yapmak istediğinizden emin misiniz?");

        class AlertDialogClickListener implements DialogInterface.OnClickListener {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialog.BUTTON_NEGATIVE) {
                    Toast("İşlem iptal edildi");
                } else if (which == AlertDialog.BUTTON_POSITIVE) { // veya else
                    Toast("Çıkış başarıyla gerçekleştirildi");
                    NotePage.this.finish(); // Activity’nin sonlandırılması

                    //Giriş ekranı için Pref. Kontrolü
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("Remember", "false");
                    editor.apply();
                    finish();

                    //TODO: Google Sign-out blogu yazılmalı.

                    //Giriş aktivitesine dönülmesi
                    Intent intent = new Intent(NotePage.this, SignIn.class);
                    startActivity(intent);
                }
            }
        }

        // AlertDialog Builder
        AlertDialogClickListener alertDialogClickListener = new AlertDialogClickListener();
        builder.setPositiveButton("EVET", alertDialogClickListener);
        builder.setNegativeButton("HAYIR", alertDialogClickListener);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.button_active_color));
        alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.button_active_color));
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);
    }


    @Override
    public void Toast(String message) {
        android.widget.Toast.makeText(getApplicationContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void Snackbar(View v, String message) {
        Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show();
    }





}