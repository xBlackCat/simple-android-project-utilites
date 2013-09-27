package org.xblackcat.android.service.image;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 06.09.12 15:03
 *
 * @author xBlackCat
 */
class ImageCacheDB extends SQLiteOpenHelper {
    private static int DB_VERSION = 1;
    private static String DB_NAME = "image_cache.db";

    public ImageCacheDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE imagemetadata (url TEXT NOT NULL PRIMARY KEY ASC, filename TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized String getImageFileName(String url) {
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.query(
                    "imagemetadata",
                    new String[]{"filename"},
                    "url = ?",
                    new String[]{url},
                    null,
                    null,
                    null
            );

            try {
                if (cursor.moveToNext()) {
                    return cursor.getString(0);
                } else {
                    return null;
                }
            } finally {
                cursor.close();
            }
        } finally {
            db.close();
        }
    }

    public synchronized void storeInCache(String url, String fileName) {
        ContentValues values = new ContentValues();
        values.put("url", url);
        values.put("filename", fileName);
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.insert("imagemetadata", null, values);
        } finally {
            db.close();
        }
    }

    public void removeImage(String url) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            int cursor = db.delete(
                    "imagemetadata",
                    "url = ?",
                    new String[]{url}
            );
        } finally {
            db.close();
        }
    }
}
