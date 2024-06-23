package com.example.notepad;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

public class NoteDatabase {
    private static final String DATABASE_NAME = "note_database";
    private static final int DATABASE_VERSION = 1;

    private static NoteDatabase instance;
    public SQLiteDatabase database;

    private NoteDatabase(Context context) {
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
    }

    public static synchronized NoteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new NoteDatabase(context);
        }
        return instance;
    }

    public void saveNote(Note note) {
        ContentValues values = new ContentValues();
        values.put("title", note.getTitle());
        values.put("time", note.getTime());
        values.put("content", note.getContent());
        values.put("imagePaths", String.join(",", note.getImagePaths()));

        database.insert("notes", null, values);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, time TEXT, content TEXT, imagePaths TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 处理数据库版本升级
        }
    }
    public SQLiteDatabase getDatabase() {
        return database;
    }
}