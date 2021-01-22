package com.konovus.notes_youtube.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.konovus.notes_youtube.R;
import com.konovus.notes_youtube.database.NoteDatabase;
import com.konovus.notes_youtube.databinding.ActivityCreateNoteBinding;
import com.konovus.notes_youtube.models.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private ActivityCreateNoteBinding binding;
    private String selectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_note);

        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMMM yy HH:mm a", Locale.getDefault())
                .format(new Date()));

        binding.saveNote.setOnClickListener(v -> saveNote());

        selectedColor = "#333333";
        initMiscell();
        setSubtitleIndicatorColor();
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
        note.setColor(selectedColor);

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

    private void initMiscell(){
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(binding.layoutMiscell.layoutMiscell);
        binding.layoutMiscell.textMiscell.setOnClickListener(v -> {
            if(bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        List<ImageView> colors = new ArrayList<>();
        Collections.addAll(colors, binding.layoutMiscell.imageColor1, binding.layoutMiscell.imageColor2,
                binding.layoutMiscell.imageColor3, binding.layoutMiscell.imageColor4, binding.layoutMiscell.imageColor5);
        for(ImageView color : colors)
            color.setOnClickListener(v -> {
                selectedColor = color.getTag().toString();
                for(ImageView c : colors)
                    if(c.getTag().toString().equals(selectedColor))
                        c.setImageResource(R.drawable.ic_check);
                    else c.setImageResource(0);
                setSubtitleIndicatorColor();
            });

    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) binding.viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColor));
    }
}