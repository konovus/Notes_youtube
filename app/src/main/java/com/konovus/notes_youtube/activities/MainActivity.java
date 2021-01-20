package com.konovus.notes_youtube.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.konovus.notes_youtube.R;
import com.konovus.notes_youtube.database.NoteDatabase;
import com.konovus.notes_youtube.databinding.ActivityMainBinding;
import com.konovus.notes_youtube.models.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    private ActivityMainBinding binding;
    private List<Note> notesMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.addNoteMain.setOnClickListener(v -> {
            startActivityForResult(
                    new Intent(getApplicationContext(), CreateNoteActivity.class),
                    REQUEST_CODE_ADD_NOTE
            );
        });

        getNotes();
    }

    private void getNotes(){
        notesMain = new ArrayList<>();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notes -> {
                    notesMain.addAll(notes);
                    Log.d("my_notes", notesMain.toString());
                    compositeDisposable.dispose();
                }));
    }
}