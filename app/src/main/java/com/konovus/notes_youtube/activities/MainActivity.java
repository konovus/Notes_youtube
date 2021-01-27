package com.konovus.notes_youtube.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.konovus.notes_youtube.R;
import com.konovus.notes_youtube.adapters.NoteAdapter;
import com.konovus.notes_youtube.database.NoteDatabase;
import com.konovus.notes_youtube.databinding.ActivityMainBinding;
import com.konovus.notes_youtube.models.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements NoteAdapter.NoteListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    private ActivityMainBinding binding;
    private List<Note> noteList;
    private List<Note> search_notes = new ArrayList<>();
    private boolean isSearch;
    private String searchWord = "";
    private NoteAdapter adapter;
    private Timer timer;

    private int noteClickedPos = -1;


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

        getNotes(REQUEST_CODE_SHOW_NOTES, false);

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (timer != null)
                    timer.cancel();
            }

            @Override
            public void afterTextChanged(Editable s) {
                isSearch = true;
                searchWord = s.toString();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(searchWord.trim().isEmpty()) {
                            noteList.clear();
                            search_notes.clear();
                            isSearch = false;
                            getNotes(REQUEST_CODE_SHOW_NOTES, false);
                        } else {
                            search_notes.clear();
                            adapter.setNotes(searchNotes(searchWord));
                            new Handler(Looper.getMainLooper()).post(() -> adapter.notifyDataSetChanged());
                            binding.recyclerView.smoothScrollToPosition(0);
                        }
                    }
                },500);
            }
        });

    }


    private void getNotes(final int request_code, final boolean isNoteDeleted){
        noteList = new ArrayList<>();
        CompositeDisposable compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notes -> {
                    noteList.addAll(notes);

                    if(request_code == REQUEST_CODE_SHOW_NOTES) {
                        adapter = new NoteAdapter(noteList, this, this);
                        binding.recyclerView.setAdapter(adapter);
                        binding.recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                    } else if(request_code == REQUEST_CODE_ADD_NOTE){
                        adapter.setNotes(noteList);
                        adapter.notifyItemInserted( 0);
                        binding.recyclerView.smoothScrollToPosition(0);
                    } else if(request_code == REQUEST_CODE_UPDATE_NOTE){
                        if(isNoteDeleted) {
                            adapter.setNotes(noteList);
                            adapter.notifyItemRemoved(noteClickedPos);
                        }
                        else {
                            adapter.setNotes(noteList);
                            adapter.notifyItemChanged(noteClickedPos);
                        }
                    }
                    compositeDisposable.dispose();
                }));
    }

    private List<Note> searchNotes(String searchWord){
        search_notes.clear();
        for(Note note : noteList)
            if(note.getTitle().toLowerCase().contains(searchWord) ||
                    note.getSubtitle().toLowerCase().contains(searchWord) ||
                    note.getNoteText().toLowerCase().contains(searchWord))
                search_notes.add(note);
        return search_notes;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK)
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK)
            if(data != null && !isSearch)
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            else if(data != null && isSearch){
                CompositeDisposable compositeDisposable = new CompositeDisposable();
                compositeDisposable.add(NoteDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(notes -> {
                            noteList.clear();
                            noteList.addAll(notes);
                            adapter.setNotes(searchNotes(searchWord));
                            if(data.getBooleanExtra("isNoteDeleted", false))
                                new Handler(Looper.getMainLooper()).post(() -> adapter.notifyItemRemoved(noteClickedPos));
                            else
                                new Handler(Looper.getMainLooper()).post(() -> adapter.notifyItemChanged(noteClickedPos));
                            compositeDisposable.dispose();
                        }));

            }
    }

    @Override
    public void OnNoteClicked(Note note, int position) {
        noteClickedPos = position;
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

}