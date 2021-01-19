package com.konovus.notes_youtube.database;

import android.content.Context;

import com.konovus.notes_youtube.dao.NoteDao;
import com.konovus.notes_youtube.models.Note;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase{

    private static NoteDatabase noteDatabase;

    public static synchronized NoteDatabase getDatabase(Context context){
        if(noteDatabase == null){
            noteDatabase = Room.databaseBuilder(
                    context, NoteDatabase.class, "notes_db"
            ).build();
        }

        return noteDatabase;
    }

    public abstract NoteDao noteDao();
}
