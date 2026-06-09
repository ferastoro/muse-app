package com.example.muse.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "muse_database.db";
    // TINGKATKAN VERSI DATABASE UNTUK MEMICU onUpgrade DAN MENGHAPUS FAVORIT LAMA
    private static final int DATABASE_VERSION = 4; 
    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE favorites (" +
                "id INTEGER PRIMARY KEY," +
                "title TEXT NOT NULL," +
                "artist TEXT," +
                "date_display TEXT," +
                "medium TEXT," +
                "image_url TEXT," +
                "description TEXT," +
                "saved_at INTEGER" +
                ")";
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Hapus tabel favorit yang sudah ada dan buat ulang untuk membersihkan data lama
        db.execSQL("DROP TABLE IF EXISTS favorites");
        onCreate(db);
    }
}
