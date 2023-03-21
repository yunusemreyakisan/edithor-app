package com.app.edithormobile.viewmodel;

import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.app.edithormobile.adapters.NoteAdapter;
import com.app.edithormobile.databinding.ActivityNotePageBinding;
import com.app.edithormobile.model.NoteModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotePageViewModel extends ViewModel {

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
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                NoteModel model = dataSnapshot.getValue(NoteModel.class);
                notes.add(model);
                noteAdapter.notifyItemInserted(notes.size());
                noteAdapter.notifyDataSetChanged();
                bosKontrolu(binding, noteAdapter, mDatabaseReference);
                Log.d("note size", String.valueOf(notes.size()));
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                //Update
                bosKontrolu(binding, noteAdapter, mDatabaseReference);
                noteAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //Intent
                bosKontrolu(binding, noteAdapter, mDatabaseReference);
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
                Log.e("Veritabanı Hatası:", "Veritabanı hatası! " + databaseError.getMessage());
            }
        });
    }

}
