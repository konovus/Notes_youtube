package com.konovus.notes_youtube.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.konovus.notes_youtube.R;
import com.konovus.notes_youtube.database.NoteDatabase;
import com.konovus.notes_youtube.databinding.ActivityCreateNoteBinding;
import com.konovus.notes_youtube.databinding.ActivityMainBinding;
import com.konovus.notes_youtube.models.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private ActivityCreateNoteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_note);

        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMMM yy HH:mm a", Locale.getDefault())
                .format(new Date()));

        binding.saveNote.setOnClickListener(v -> saveNote());
    }

    private void saveNote(){
        if(binding.inputNoteTitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note title can't be empty.", Toast.LENGTH_SHORT).show();
            return;
        } else if(binding.inputNote.getText().toString().trim().isEmpty()
                && binding.inputNoteSubtitle.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "Note text can't be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        final Note note = new Note();
        note.setTitle(binding.inputNoteTitle.getText().toString());
        note.setSubtitle(binding.inputNoteSubtitle.getText().toString());
        note.setDateTime(binding.textDateTime.getText().toString());
        note.setNoteText(binding.inputNote.getText().toString());

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(NoteDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            setResult(RESULT_OK, new Intent());
                            Toast.makeText(this, "Note saved.", Toast.LENGTH_SHORT).show();
                            finish();
                            compositeDisposable.dispose();
                        }));

    }
}