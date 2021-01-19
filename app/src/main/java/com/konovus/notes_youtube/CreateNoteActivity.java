package com.konovus.notes_youtube;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.konovus.notes_youtube.databinding.ActivityCreateNoteBinding;
import com.konovus.notes_youtube.databinding.ActivityMainBinding;

public class CreateNoteActivity extends AppCompatActivity {

    private ActivityCreateNoteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_note);

        binding.imgBack.setOnClickListener(v -> onBackPressed());
    }
}