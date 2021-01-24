package com.konovus.notes_youtube.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.konovus.notes_youtube.R;
import com.konovus.notes_youtube.database.NoteDatabase;
import com.konovus.notes_youtube.databinding.ActivityCreateNoteBinding;
import com.konovus.notes_youtube.databinding.LayoutAddUrlBinding;
import com.konovus.notes_youtube.models.Note;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private ActivityCreateNoteBinding binding;
    private AlertDialog alertDialog;
    private String selectedColor;
    private String selectedImagePath;
    private Note oldNote;

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_IMAGE_PERMISSION = 2;

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
        selectedImagePath = "";

        if(getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            oldNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        binding.deleteWebImg.setOnClickListener(v -> {
            binding.textWebURL.setText(null);
            binding.layoutWebURL.setVisibility(View.GONE);
        });
        binding.imageDeleteImg.setOnClickListener(v -> {
            binding.imageNote.setImageBitmap(null);
            binding.imageNote.setVisibility(View.GONE);
            binding.imageDeleteImg.setVisibility(View.GONE);
            selectedImagePath = "";
        });

        initMiscell();
        setSubtitleIndicatorColor();
    }

    private void setViewOrUpdateNote(){
        binding.inputNoteTitle.setText(oldNote.getTitle());
        binding.inputNoteSubtitle.setText(oldNote.getSubtitle());
        binding.inputNote.setText(oldNote.getNoteText());
        binding.textDateTime.setText(oldNote.getDateTime());

        if(oldNote.getImagePath() != null && !oldNote.getImagePath().trim().isEmpty()){
            binding.imageNote.setImageBitmap(BitmapFactory.decodeFile(oldNote.getImagePath()));
            binding.imageNote.setVisibility(View.VISIBLE);
            binding.imageDeleteImg.setVisibility(View.VISIBLE);
            selectedImagePath = oldNote.getImagePath();
        }

        if(oldNote.getWebLink() != null && !oldNote.getWebLink().trim().isEmpty()){
            binding.textWebURL.setText(oldNote.getWebLink());
            binding.layoutWebURL.setVisibility(View.VISIBLE);
        }
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
        note.setImagePath(selectedImagePath);
        if(binding.layoutWebURL.getVisibility() == View.VISIBLE)
            note.setWebLink(binding.textWebURL.getText().toString());

        if(oldNote != null)
            note.setId(oldNote.getId());

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

        binding.layoutMiscell.layoutAddImage.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            } else selectImage();
        });

        if(oldNote != null && oldNote.getColor() != null && !oldNote.getColor().trim().isEmpty()){
            selectedColor = oldNote.getColor();
            for(ImageView color: colors)
                if(color.getTag().toString().equals(oldNote.getColor()))
                    color.setImageResource(R.drawable.ic_check);
                else color.setImageResource(0);
            setSubtitleIndicatorColor();
        }

        binding.layoutMiscell.layoutAddURL.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            showAddUrlDialog();
        });

    }

    private void showAddUrlDialog(){
        if(alertDialog == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url,
                     findViewById(R.id.layoutAddUrlContainer));
            builder.setView(view);
            LayoutAddUrlBinding addUrlBinding = DataBindingUtil.bind(view);

            alertDialog = builder.create();
            if(alertDialog.getWindow() != null)
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

            addUrlBinding.inputURL.requestFocus();
            addUrlBinding.textAdd.setOnClickListener(v -> {
                if(addUrlBinding.inputURL.getText().toString().trim().isEmpty())
                    Toast.makeText(this, "URL cannot be empty!", Toast.LENGTH_SHORT).show();
                else if(!Patterns.WEB_URL.matcher(addUrlBinding.inputURL.getText().toString()).matches())
                    Toast.makeText(this, "Enter valid URL!", Toast.LENGTH_SHORT).show();
                else {
                    binding.textWebURL.setText(addUrlBinding.inputURL.getText().toString());
                    binding.layoutWebURL.setVisibility(View.VISIBLE);
                    alertDialog.dismiss();
                }
            });
            addUrlBinding.cancelButton.setOnClickListener(v -> alertDialog.dismiss());
        }
        alertDialog.show();
    }

    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PERMISSION);
    }

    private String getPathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if(cursor == null)
            filePath = contentUri.getPath();
        else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0)
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                selectImage();
            else Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_IMAGE_PERMISSION && resultCode == RESULT_OK)
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.imageNote.setImageBitmap(bitmap);
                        binding.imageNote.setVisibility(View.VISIBLE);
                        binding.imageDeleteImg.setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) binding.viewSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedColor));
    }
}