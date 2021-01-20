package com.konovus.notes_youtube.dao;

import com.konovus.notes_youtube.models.Note;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.reactivex.Completable;
import io.reactivex.Flowable;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    Flowable<List<Note>> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertNote(Note note);

    @Delete
    Completable deleteNote(Note note);

    @Query("SELECT * FROM notes WHERE id = :noteId")
    Flowable<Note> getNoteById(String noteId);
}
