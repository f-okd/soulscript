package com.example.soulscript.frontend.screens.bookmarks;

// Import necessary classes:
// The code imports several classes from the Android SDK and Firebase Authentication libraries.
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

import com.example.soulscript.frontend.BibleVerse;
import com.example.soulscript.backend.database.BookmarksContract;
import com.example.soulscript.backend.database.BookmarksDbHelper;
import com.example.soulscript.backend.NetworkStatusReceiver;
import com.example.soulscript.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/*
* This class represents the Bookmarks screen.
* It handles displaying a list of bookmarked Bible verses using a RecyclerView.
* This class is responsible for managing and interacting with Firebase Realtime Database and a local SQLite database to store and retrieve the bookmarked verses.
* It also listens to network status changes to switch between online and offline data sources.
 */
public class Bookmarks extends AppCompatActivity {
    // Declare variables:
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

        // Initialize Firebase authentication objects:
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String databaseUrl = "https://soulscript-7fb99-default-rtdb.europe-west1.firebasedatabase.app//";
        bookmarkRef = FirebaseDatabase.getInstance(databaseUrl).getReference().child("bookmarks").child(user.getUid());

        // Initialize the RecyclerView and its adapter
        bookmarksRecyclerView = findViewById(R.id.bookmarks_recycler_view);
        bookmarksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookmarksAdapter = new BookmarksAdapter(this, new ArrayList<>(), new BookmarksAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(BibleVerse bibleVerse) {
                deleteVerseFromFirebase(bibleVerse);
            }
        });
        bookmarksRecyclerView.setAdapter(bookmarksAdapter);

        // Initialize the local database
        bookmarkDbHelper = new BookmarksDbHelper(this);
        loadLocalBookmarks();

        // Listen for changes to the bookmarks in the Firebase Realtime Database and update the bookmarks list accordingly
        valueEventListener = bookmarkRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange called.");
                ArrayList<BibleVerse> bibleVerses = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BibleVerse bibleVerse = snapshot.getValue(BibleVerse.class);
                    bibleVerses.add(bibleVerse);
                }

                bookmarksAdapter.setBookmarks(bibleVerses); // Update the adapter with the new list of verses

                // Update local bookmarks
                updateLocalBookmarks(bibleVerses);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Bookmark data retrieval cancelled.", databaseError.toException());
            }
        });

        // Listen for changes to the network status and load bookmarks from local storage if the device is offline
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


    // Load bookmarks from the local database into the bookmarks list
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

    // Delete a bookmark from the Firebase Realtime Database using the verse name as the key
    private void deleteVerseFromFirebase(BibleVerse bibleVerse) {
        // Query the Firebase Realtime Database to find the verse with the matching name value
        Query query = bookmarkRef.orderByChild("verse").equalTo(bibleVerse.getVerse());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Remove the verse with the matching name value
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to delete the Bible verse.", databaseError.toException());
            }
        });
    }



    // Update the local bookmarks table with the latest bookmarks from the Firebase Realtime Database
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
    // Unregister the network status receiver and remove the Firebase Realtime Database listener
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(networkStatusReceiver);
        bookmarkRef.removeEventListener(valueEventListener);
        Log.d(TAG, "Bookmark activity destroyed.");
    }
}
