package com.app.edithormobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.edithormobile.R;
import com.app.edithormobile.models.NoteModel;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {

    Context context;
    ArrayList<NoteModel> notes;

    //Constructor
    public NoteAdapter(Context context, ArrayList<NoteModel> notes) {
        this.context = context;
        this.notes = notes;
    }


    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_note_item, parent, false);
        return new NoteHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.NoteHolder holder, int position) {
        NoteModel mNote = notes.get(position);
        holder.tvTitle.setText(mNote.getNotBaslik());
        holder.tvNote.setText(mNote.getNotIcerigi());
        holder.tvOlusturmaTarihi.setText(mNote.getNotOlusturmaTarihi());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    //ViewHolder
    public static class NoteHolder extends RecyclerView.ViewHolder {

        TextView tvNote, tvTitle, tvOlusturmaTarihi;

        public NoteHolder(@NonNull View itemView) {
            super(itemView);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvOlusturmaTarihi = itemView.findViewById(R.id.tvOlusturmaTarihi);

        }

    }


}
