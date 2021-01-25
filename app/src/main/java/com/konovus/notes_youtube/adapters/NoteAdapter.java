package com.konovus.notes_youtube.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.konovus.notes_youtube.R;
import com.konovus.notes_youtube.databinding.NoteItemBinding;
import com.konovus.notes_youtube.models.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private LayoutInflater layoutInflater;
    private NoteListener noteListener;
    private Timer timer;
    private List<Note> notesSource;
    Context context;

    public NoteAdapter(List<Note> notes, NoteListener noteListener, Context context) {
        this.notes = notes;
        this.noteListener = noteListener;
        this.context = context;
        notesSource = notes;
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
            else binding.textSubtitle.setVisibility(View.VISIBLE);

            GradientDrawable gradientDrawable = (GradientDrawable) binding.layoutNote.getBackground();
            if(note.getColor() != null)
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            else gradientDrawable.setColor(Color.parseColor("#333333"));

            if(note.getImagePath() != null){
                binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                binding.imageNote.setVisibility(View.VISIBLE);
            } else binding.imageNote.setVisibility(View.GONE);

//            if(note.getWebLink() != null){
//                binding.
//            }


            binding.executePendingBindings();
            binding.layoutNote.setOnClickListener(v -> noteListener.OnNoteClicked(note, getAdapterPosition()));
        }
    }

    public interface NoteListener{
        void OnNoteClicked(Note note, int position);
    }

    public void setNotes(List<Note> new_notes){
        this.notes = new_notes;
    }

    public void searchNotes(final String searchWord){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchWord.trim().isEmpty())
                    notes = notesSource;
                else {
                    List<Note> temp = new ArrayList<>();
                    for(Note note : notesSource)
                        if(note.getTitle().toLowerCase().contains(searchWord) ||
                           note.getSubtitle().toLowerCase().contains(searchWord) ||
                           note.getNoteText().toLowerCase().contains(searchWord))
                                temp.add(note);

                        notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        },500);
    }

    public void cancelTimer(){
        if(timer != null)
            timer.cancel();
    }
}
