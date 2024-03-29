package com.app.edithormobile.viewmodel.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.app.edithormobile.R;
import com.app.edithormobile.adapter.NoteAdapter;
import com.app.edithormobile.databinding.ActivityNotePageBinding;
import com.app.edithormobile.model.NoteModel;
import com.app.edithormobile.util.Util;
import com.app.edithormobile.view.gpt.AskGPT;
import com.app.edithormobile.view.create.AddNote;
import com.app.edithormobile.view.home.NotePage;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class NotePageViewModel extends ViewModel {
    Boolean isAllFabsVisible;
    Util util = new Util();

    //bos kontrolu
    public void bosKontrolu(ActivityNotePageBinding binding, NoteAdapter noteAdapter, DatabaseReference mDatabaseReference) {
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
    public void search(ActivityNotePageBinding binding, NoteAdapter noteAdapter, ArrayList<NoteModel> notes) {
        //arama islemi
        binding.searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query, notes, noteAdapter);
                noteAdapter.notifyDataSetChanged();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText, notes, noteAdapter);
                noteAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }

    //TODO: Issue -> Filtreleme sırasında notu bulduktan sonra bir önceki liste pozisyonunu alıyor.
    public void filter(String text, ArrayList<NoteModel> notes, NoteAdapter noteAdapter) {
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

    //Degisiklik izleme
    public void notesEventChangeListener(ActivityNotePageBinding binding, NoteAdapter noteAdapter, DatabaseReference mDatabaseReference, ArrayList<NoteModel> notes) {
        //Child Listener
        bosKontrolu(binding, noteAdapter, mDatabaseReference);
        binding.progressBar.setVisibility(View.VISIBLE);
        Collections.sort(notes);
        Set<NoteModel> pinnedNotes = new ArraySet<>();;

        notes.addAll(0, pinnedNotes);
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                NoteModel model = dataSnapshot.getValue(NoteModel.class);
                notes.add(model);
                noteAdapter.notifyItemInserted(notes.size());
                Comparator<NoteModel> tersSiralama = Collections.reverseOrder();
                Collections.sort(notes, tersSiralama);
                noteAdapter.notifyDataSetChanged();
                bosKontrolu(binding, noteAdapter, mDatabaseReference);
                Log.d("note size", String.valueOf(notes.size()));
                Log.e("Notes: ", noteAdapter.getNotes().toString());
                //Pinlenen mesajları alma ve pinlenen listesine ekleme
                for(NoteModel models :  noteAdapter.getNotes()){
                    if(models.isPinned()){
                        pinnedNotes.add(models);
                    }
                }
                Log.e("Pinned notes: ", pinnedNotes.toString());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                //Update
                bosKontrolu(binding, noteAdapter, mDatabaseReference);
                Comparator<NoteModel> tersSiralama = Collections.reverseOrder();
                Collections.sort(notes, tersSiralama);
                noteAdapter.notifyDataSetChanged();
                Log.e("Pinned notes: ", pinnedNotes.toString());

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //Intent
                bosKontrolu(binding, noteAdapter, mDatabaseReference);
                noteAdapter.notifyItemRemoved(notes.size());
                Comparator<NoteModel> tersSiralama = Collections.reverseOrder();
                Collections.sort(notes, tersSiralama);
                noteAdapter.notifyDataSetChanged();
                Log.d("note size removed", String.valueOf(notes.size()));
                Log.e("Pinned notes: ", pinnedNotes.toString());

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                //Updated
                Comparator<NoteModel> tersSiralama = Collections.reverseOrder();
                Collections.sort(notes, tersSiralama);
                noteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Veritabanı Hatası:", "Veritabanı hatası! " + databaseError.getMessage());
            }
        });
    }


    //FAB Control
    @SuppressLint("ClickableViewAccessibility")
    public void fabControl(ActivityNotePageBinding binding, Context context) {
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
                // binding.notePageFullScreen.setForeground(getResources().getDrawable(R.drawable.custom_foreground));

                //TODO: FAB açıldığında arkaplanın solması gerekiyor.

                isAllFabsVisible = true;
            } else {
                binding.addNote.hide();
                binding.addFile.hide();
                binding.addNoteTv.setVisibility(View.GONE);
                binding.addFileTv.setVisibility(View.GONE);
                //binding.notePageFullScreen.setForeground(null);
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
            Intent intent = new Intent(context, AddNote.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        //ChatGPT
        binding.addFile.setOnClickListener(view -> {
            Intent intent = new Intent(context, AskGPT.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });


    }

    //Swipe Listener Func.
    public void swipeListener(ActivityNotePageBinding binding, NoteAdapter noteAdapter, DatabaseReference mDatabaseReference, ArrayList<NoteModel> notes) {
        //TODO: Swipe yapıldığında liste kapanıp progress bar çıkacak ve liste yenilenecek.
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            notes.clear();
            notesEventChangeListener(binding, noteAdapter, mDatabaseReference, notes);
        });
    }

    //Filter color note
    public void filterColor(ActivityNotePageBinding binding, BottomSheetDialog filterDialog){
        binding.btnFilterColorNote.setOnClickListener(v -> filterDialog.show());
    }


    //TODO: Renklere göre not filtreleme işleminin gerçekleştirilmesi
    public void filterColorActions(View filterView, Context context){
        //Blue Filter Button
        filterView.findViewById(R.id.btn_filter_color_blue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Maviye tıklanıldı
                Toast.makeText(context, "Maviye tıklanıldı", Toast.LENGTH_SHORT).show();
            }
        });

        //Green Filter Button
        filterView.findViewById(R.id.btn_filter_color_green).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Yeşile tıklanıldı
                Toast.makeText(context, "Yeşile tıklanıldı", Toast.LENGTH_SHORT).show();
            }
        });

        //Red Filter Button
        filterView.findViewById(R.id.btn_filter_color_red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Kırmızıya tıklanıldı
                Toast.makeText(context, "Kırmızıya tıklanıldı", Toast.LENGTH_SHORT).show();
            }
        });


    }



}
