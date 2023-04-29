package com.example.soulscript;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BookmarksDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Bookmarks.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "bookmarks";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_VERSE = "verse";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_EXPLANATION = "explanation";

    public BookmarksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_VERSE + " TEXT, " +
                COLUMN_TEXT + " TEXT, " +
                COLUMN_EXPLANATION + " TEXT" +
                ")";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getAllBookmarks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_VERSE, COLUMN_TEXT, COLUMN_EXPLANATION},
                null, null, null, null, null);
    }
}
