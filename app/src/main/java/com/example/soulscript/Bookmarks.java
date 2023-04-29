package com.example.soulscript;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class Bookmarks extends AppCompatActivity {
    private static final String TAG = "Bookmarks";
    private RecyclerView bookmarksRecyclerView;
    private BookmarksAdapter bookmarksAdapter;
    private DatabaseReference bookmarkRef;
    private ValueEventListener valueEventListener;
    private BookmarksDbHelper bookmarkDbHelper;
    private NetworkStatusReceiver networkStatusReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String databaseUrl = "https://soulscript-7fb99-default-rtdb.europe-west1.firebasedatabase.app//";
        bookmarkRef = FirebaseDatabase.getInstance(databaseUrl).getReference().child("bookmarks").child(user.getUid());

        bookmarksRecyclerView = findViewById(R.id.bookmarks_recycler_view);
        bookmarksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookmarksAdapter = new BookmarksAdapter(this, new ArrayList<>());
        bookmarksRecyclerView.setAdapter(bookmarksAdapter);

        bookmarkDbHelper = new BookmarksDbHelper(this);
        loadLocalBookmarks();

        // Listen for changes to the bookmarks in the Firebase Realtime Database
        // and update the bookmarks list accordingly
        valueEventListener = bookmarkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<BibleVerse> bibleVerses = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BibleVerse bibleVerse = snapshot.getValue(BibleVerse.class);
                    bibleVerses.add(bibleVerse);
                }
                bookmarksAdapter = new BookmarksAdapter(Bookmarks.this, bibleVerses);
                bookmarksRecyclerView.setAdapter(bookmarksAdapter);
                Log.d(TAG, "Bookmark data changed. Total bookmarks: " + bibleVerses.size());

                bookmarksAdapter = new BookmarksAdapter(Bookmarks.this, bibleVerses);
                bookmarksRecyclerView.setAdapter(bookmarksAdapter);
                Log.d(TAG, "Bookmark data changed. Total bookmarks: " + bibleVerses.size());

                // Update local bookmarks
                updateLocalBookmarks(bibleVerses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Bookmark data retrieval cancelled.", databaseError.toException());
            }
        });

        networkStatusReceiver = new NetworkStatusReceiver(new NetworkStatusReceiver.NetworkStatusListener() {
            @Override
            public void onNetworkStatusChanged(boolean isConnected) {
                if (isConnected) {
                    Log.d(TAG, "Internet connected. Loading bookmarks from the server.");
                } else {
                    Log.d(TAG, "Internet disconnected. Loading bookmarks from local storage.");
                    loadLocalBookmarks();
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(networkStatusReceiver, new IntentFilter("NETWORK_STATUS"));

    }



    private void loadLocalBookmarks() {
        ArrayList<BibleVerse> localBookmarks = new ArrayList<>();
        Cursor cursor = bookmarkDbHelper.getAllBookmarks();

        if (cursor.moveToFirst()) {
            do {
                String verse = cursor.getString(cursor.getColumnIndexOrThrow(bookmarkDbHelper.COLUMN_VERSE));
                String text = cursor.getString(cursor.getColumnIndexOrThrow(bookmarkDbHelper.COLUMN_TEXT));
                String explanation = cursor.getString(cursor.getColumnIndexOrThrow(bookmarkDbHelper.COLUMN_EXPLANATION));
                localBookmarks.add(new BibleVerse(verse, text, explanation));
            } while (cursor.moveToNext());
        }

        cursor.close();
        bookmarksAdapter.setBookmarks(localBookmarks);
        Log.d(TAG, "loadLocalBookmarks() method called.");
    }

    private void updateLocalBookmarks(ArrayList<BibleVerse> bibleVerses) {
        // Delete all rows from the local bookmarks table
        SQLiteDatabase db = bookmarkDbHelper.getWritableDatabase();
        db.delete(BookmarksContract.BookmarksEntry.TABLE_NAME, null, null);

        // Insert the new bookmarks into the local bookmarks table
        for (BibleVerse bibleVerse : bibleVerses) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(BookmarksContract.BookmarksEntry.COLUMN_VERSE, bibleVerse.getVerse());
            contentValues.put(BookmarksContract.BookmarksEntry.COLUMN_TEXT, bibleVerse.getText());
            contentValues.put(BookmarksContract.BookmarksEntry.COLUMN_EXPLANATION, bibleVerse.getExplanation());
            db.insert(BookmarksContract.BookmarksEntry.TABLE_NAME, null, contentValues);
        }

        Log.d(TAG, "Local bookmarks updated.");
    }






    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkStatusReceiver);
        bookmarkRef.removeEventListener(valueEventListener);
        Log.d(TAG, "Bookmark activity destroyed.");
    }
}
