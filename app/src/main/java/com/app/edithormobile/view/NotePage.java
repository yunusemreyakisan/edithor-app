package com.app.edithormobile.view;

import static java.util.Objects.requireNonNull;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.edithormobile.R;
import com.app.edithormobile.adapters.NoteAdapter;
import com.app.edithormobile.databinding.ActivityNotePageBinding;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.util.ISnackbar;
import com.app.edithormobile.util.IToast;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.chat_gpt.AskGPT;
import com.app.edithormobile.view.crud.AddNote;
import com.app.edithormobile.view.detail.NoteDetail;
import com.app.edithormobile.view.login.SignIn;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
    DatabaseReference removeRef, removedReference;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Boolean isAllFabsVisible;
    private GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount account;

    NoteAdapter.ClickListener clickListener;

    ActivityNotePageBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);

        binding = ActivityNotePageBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        binding.noData.setVisibility(View.INVISIBLE);
        binding.notFound.setVisibility(View.INVISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        //Methods
        databaseRef();
        fabControl();

        //TODO: Dialogplus kullanarak fotograf ve galeri seçimini yaptır.


    }

    @Override
    protected void onStart() {
        super.onStart();
        //onStart modunda yenilensin.
        notesEventChangeListener();
        search();
        notesViewRV();

        //TODO: Swipe yapıldığında liste kapanıp progress bar çıkacak ve liste yenilenecek.
        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.rvNotes.setAdapter(noteAdapter);
            }
        });

        binding.toolbarSecilenler.setOnClickListener(v1 -> {
            //todo: verileri siliyoruz, fakat adapter listesi güncellenmiyor.
            for (NoteModel model : selectedNotes) {
                String id = model.getNoteID();
                removeRef.child(id).removeValue();
            }

            selectedNotes.removeAll(selectedNotes);
            binding.toolbarTopNotePage.setVisibility(View.VISIBLE);
            binding.toolbarSecilenler.setVisibility(View.GONE);
            Log.e("secilenler listesi", selectedNotes.toString());

        });
        noteAdapter.listeyiGuncelle(notes);

    }

    @SuppressLint("ClickableViewAccessibility")
    private void fabControl() {
        binding.addNote.setVisibility(View.GONE);
        binding.addNoteTv.setVisibility(View.GONE);
        binding.addFile.setVisibility(View.GONE);
        binding.addFileTv.setVisibility(View.GONE);
        binding.notePageFullScreen.setAlpha(1f);

        isAllFabsVisible = false;
        //Baslarken kucuk gosterir.
        binding.extendedFab.shrink();
        //extendable click listener
        binding.extendedFab.setOnClickListener(view -> {
            if (!isAllFabsVisible) {
                binding.addNoteTv.bringToFront();
                binding.addFileTv.bringToFront();
                //fab icerigi goster.
                binding.addNote.show();
                binding.addFile.show();
                binding.addNoteTv.setVisibility(View.VISIBLE);
                binding.addFileTv.setVisibility(View.VISIBLE);
                //fab genislesin
                binding.extendedFab.extend();

                //TODO: FAB açıldığında arkaplanın solması gerekiyor.

                isAllFabsVisible = true;
            } else {
                binding.addNote.hide();
                binding.addFile.hide();
                binding.addNoteTv.setVisibility(View.GONE);
                binding.addFileTv.setVisibility(View.GONE);

                //fab kucultme
                binding.extendedFab.shrink();

                isAllFabsVisible = false;
            }
        });

        //fab shrink anywhere in screen
        binding.rvNotes.setOnTouchListener((view, motionEvent) -> {
            binding.addNote.setVisibility(View.GONE);
            binding.addNoteTv.setVisibility(View.GONE);
            binding.addFile.setVisibility(View.GONE);
            binding.addFileTv.setVisibility(View.GONE);

            isAllFabsVisible = false;
            binding.extendedFab.shrink();
            return false;
        });


        //Not ekleme FAB
        binding.addNote.setOnClickListener(view -> {
            Intent intent = new Intent(NotePage.this, AddNote.class);
            startActivity(intent);
        });

        //ChatGPT
        binding.addFile.setOnClickListener(view -> {
            Intent intent = new Intent(NotePage.this, AskGPT.class);
            startActivity(intent);
        });


    }


    //DB Reference
    private void databaseRef() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mUser).getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Notlarim");
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
                //TODO: Bunun için bir selectedNotes() arraylisti yapıp içerisine id'li bir şekilde atacağız, liste sonradan silinecek.
                isSelectedMode = true;
                if (selectedNotes.contains(pos)) {
                    v.setForeground(null);
                    selectedNotes.remove(pos);
                } else {
                    selectedNotes.add(pos);
                    v.setForeground(getResources().getDrawable(R.color.button_active_color));
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
        binding.rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
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


    //Degisiklik izleme
    private void notesEventChangeListener() {
        //Child Listener
        bosKontrolu();
        binding.progressBar.setVisibility(View.VISIBLE);
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                NoteModel model = dataSnapshot.getValue(NoteModel.class);
                notes.add(model);
                noteAdapter.notifyItemInserted(notes.size());
                noteAdapter.notifyDataSetChanged();
                bosKontrolu();
                Log.d("note size", String.valueOf(notes.size()));
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                //Update
                bosKontrolu();
                noteAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //Intent
                bosKontrolu();
                noteAdapter.notifyItemRemoved(notes.size());
                noteAdapter.notifyDataSetChanged();
                Log.d("note size removed", String.valueOf(notes.size()));

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                //Updated

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast("Veritabanı hatası!");
            }
        });
    }


    //bos kontrolu
    private void bosKontrolu() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        String user_id = requireNonNull(mUser).getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(user_id).child("Notlarim");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get NoteModel object
                NoteModel model = dataSnapshot.getValue(NoteModel.class);

                if (model != null) {
                    binding.progressBar.setVisibility(View.GONE);
                    noteAdapter.notifyDataSetChanged();
                } else {
                    binding.noData.setVisibility(View.VISIBLE);
                    binding.notFound.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Handle-Error
            }

        };
        mDatabaseReference.addValueEventListener(postListener);

    }


    //Menu (Search)
    private void search() {
        //arama islemi
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query);
                noteAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                noteAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    //TODO: Issue -> Filtreleme sırasında notu bulduktan sonra bir önceki liste pozisyonunu alıyor.
    private void filter(String text) {
        ArrayList<NoteModel> filteredlist = new ArrayList<>();

        for (NoteModel item : notes) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.getNotBaslik().toLowerCase().contains(text.toLowerCase())
                    || item.getNotIcerigi().toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(item);
            }
        }
        if (filteredlist.isEmpty()) {
            //Toast("Eşleşen not yok");
        } else {
            noteAdapter.filterList(filteredlist);
            noteAdapter.notifyDataSetChanged();
        }
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