package com.example.soulscript;

import android.provider.BaseColumns;

public final class BookmarksContract {
    private BookmarksContract() {}

    public static class BookmarksEntry implements BaseColumns {
        public static final String TABLE_NAME = "bookmarks";
        public static final String COLUMN_VERSE = "verse";
        public static final String COLUMN_TEXT = "text";
        public static final String COLUMN_EXPLANATION = "explanation";
    }
}
