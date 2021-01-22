package com.konovus.notes_youtube.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.konovus.notes_youtube.R;
import com.konovus.notes_youtube.databinding.NoteItemBinding;
import com.konovus.notes_youtube.models.Note;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private LayoutInflater layoutInflater;
    private NoteListener noteListener;
    Context context;

    public NoteAdapter(List<Note> notes, NoteListener noteListener, Context context) {
        this.notes = notes;
        this.noteListener = noteListener;
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(layoutInflater == null)
            layoutInflater = LayoutInflater.from(parent.getContext());
        NoteItemBinding binding = DataBindingUtil.inflate(
                layoutInflater, R.layout.note_item, parent, false
        );
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bindNote(notes.get(position));
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{

        private NoteItemBinding binding;

        public NoteViewHolder(NoteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bindNote(Note note){
            binding.setNote(note);
            if(note.getSubtitle().isEmpty())
                binding.textSubtitle.setVisibility(View.GONE);
            GradientDrawable gradientDrawable = (GradientDrawable) binding.layoutNote.getBackground();
            if(note.getColor() != null)
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            else gradientDrawable.setColor(Color.parseColor("#333333"));

            binding.executePendingBindings();
            binding.layoutNote.setOnClickListener(v -> noteListener.OnNoteClicked(note, getAdapterPosition()));
        }
    }

    public interface NoteListener{
        void OnNoteClicked(Note note, int position);
    }

    public void setNotes(List<Note> new_notes, Integer pos){
        int currentSize = notes.size();
        this.notes = new_notes;
        if(pos != null){
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, getItemCount());
        } else {
            //tell the recycler view that all the old items are gone
            notifyItemRangeRemoved(0, currentSize);
            //tell the recycler view how many new items we added
            notifyItemRangeInserted(0, new_notes.size());
        }
    }
}
