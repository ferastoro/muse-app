package com.example.muse.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.muse.model.Favorite;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {
    private final DatabaseHelper dbHelper;

    public FavoriteDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void insertFavorite(Favorite favorite) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", favorite.getId());
        values.put("title", favorite.getTitle());
        values.put("artist", favorite.getArtist());
        values.put("date_display", favorite.getDateDisplay());
        values.put("medium", favorite.getMedium());
        values.put("image_url", favorite.getImageUrl());
        values.put("description", favorite.getDescription());
        values.put("saved_at", favorite.getSavedAt());

        db.insertWithOnConflict("favorites", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void deleteFavorite(int artworkId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("favorites", "id = ?", new String[]{String.valueOf(artworkId)});
    }

    public boolean isFavorite(int artworkId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM favorites WHERE id = ?", new String[]{String.valueOf(artworkId)});
        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;
        }
        cursor.close();
        return exists;
    }

    public List<Favorite> getAllFavorites() {
        List<Favorite> favorites = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("favorites", null, null, null, null, null, "saved_at DESC");

        if (cursor.moveToFirst()) {
            do {
                Favorite favorite = new Favorite();
                favorite.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                favorite.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                favorite.setArtist(cursor.getString(cursor.getColumnIndexOrThrow("artist")));
                favorite.setDateDisplay(cursor.getString(cursor.getColumnIndexOrThrow("date_display")));
                favorite.setMedium(cursor.getString(cursor.getColumnIndexOrThrow("medium")));
                favorite.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                favorite.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                favorite.setSavedAt(cursor.getLong(cursor.getColumnIndexOrThrow("saved_at")));
                favorites.add(favorite);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return favorites;
    }

    public int getFavoritesCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM favorites", null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
