package com.example.soulscript;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class Bookmarks extends AppCompatActivity {
    private static final String TAG = "Bookmarks";
    private RecyclerView bookmarksRecyclerView;
    private BookmarksAdapter bookmarksAdapter;
    private DatabaseReference bookmarkRef;
    private ValueEventListener valueEventListener;

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Bookmark data retrieval cancelled.", databaseError.toException());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bookmarkRef.removeEventListener(valueEventListener);
        Log.d(TAG, "Bookmark activity destroyed.");
    }
}
