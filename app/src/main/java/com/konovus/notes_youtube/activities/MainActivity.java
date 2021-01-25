package com.konovus.notes_youtube.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity implements NoteAdapter.NoteListener {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    private ActivityMainBinding binding;
    private List<Note> noteList;
    private NoteAdapter adapter;

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
                adapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!noteList.isEmpty())
                    adapter.searchNotes(s.toString());
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
                        binding.recyclerView.setLayoutManager(new WrapStaggeredLayout(2, StaggeredGridLayoutManager.VERTICAL));
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
                            noteList.remove(noteClickedPos);
                            noteList.add(noteClickedPos, notes.get(noteClickedPos));
                            adapter.setNotes(noteList);
                            adapter.notifyItemChanged(noteClickedPos);
                        }
                    }
                    compositeDisposable.dispose();
                }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK)
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK)
            if(data != null)
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
    }

    @Override
    public void OnNoteClicked(Note note, int position) {
        noteClickedPos = position;
        Intent intent = new Intent(this, CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private class WrapStaggeredLayout extends StaggeredGridLayoutManager{
        public WrapStaggeredLayout(int spanCount, int orientation) {
            super(spanCount, orientation);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("TAG", "IndexOutOfBoundsException in RecyclerView");
            }
        }
    }
}