package com.example.soulscript;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;

import android.view.Menu;
import android.view.MenuItem;

public class Results extends AppCompatActivity {
    private static final String TAG = "Results";
    private boolean isVerseBookmarked = false;
    TextView resultBox1, resultBox2;
    Button buttonBookmark;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference bookmarkRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Log.d(TAG, "Results activity created.");

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Replace "your-database-url" with the actual URL of your Firebase Realtime Database
        String databaseUrl = "https://soulscript-7fb99-default-rtdb.europe-west1.firebasedatabase.app//";
        bookmarkRef = FirebaseDatabase.getInstance(databaseUrl).getReference().child("bookmarks").child(user.getUid());

        resultBox1 = findViewById(R.id.result_box_1);
        resultBox2 = findViewById(R.id.result_box_2);
        buttonBookmark = findViewById(R.id.bookmark);

        String resultText = getIntent().getStringExtra("resultText");
        if (resultText != null) {
            try {
                JSONObject resultObject = new JSONObject(resultText);
                String verse = resultObject.getString("verse");
                String text = resultObject.getString("text");
                String explanation = resultObject.getString("explanation");

                checkIfVerseBookmarked(verse);

                resultBox1.setText(verse + "\n\n" + text);
                resultBox2.setText(explanation);

                // add OnClickListener to bookmark button
                buttonBookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get a reference to the Firebase Realtime Database location for the user's bookmarks
                        DatabaseReference bookmarkRef = FirebaseDatabase.getInstance(databaseUrl).getReference().child("bookmarks").child(user.getUid());
                        // Create a new BibleVerse object with verse, text, and explanation
                        BibleVerse bibleVerse = new BibleVerse(verse, text, explanation);
                        // Save the verse to the user's bookmarks in Firebase Realtime Database
                        bookmarkRef.push().setValue(bibleVerse);

                        // Save the verse to the local SQLite database
                        insertBookmark(bibleVerse);
                        Toast.makeText(Results.this, "Bookmark saved", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Verse bookmarked.");
                        // Start the Bookmarks activity
                        Intent bookmarksIntent = new Intent(Results.this, Bookmarks.class);
                        startActivity(bookmarksIntent);
                    }


                });


            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error parsing JSON result text.", e);
            }
        } else {
            Log.e(TAG, "No result text received.");
        }
    }


    private void checkIfVerseBookmarked(String verse) {
        bookmarkRef.orderByChild("verse").equalTo(verse).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    isVerseBookmarked = true;
                    buttonBookmark.setEnabled(false);
                    buttonBookmark.setText("Bookmarked");
                } else {
                    isVerseBookmarked = false;
                    buttonBookmark.setEnabled(true);
                    buttonBookmark.setText("Bookmark");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error checking if verse is bookmarked.", databaseError.toException());
            }
        });
    }

    private void insertBookmark(BibleVerse bibleVerse) {
        SQLiteDatabase db = new BookmarksDbHelper(this).getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BookmarksContract.BookmarksEntry.COLUMN_VERSE, bibleVerse.getVerse());
        contentValues.put(BookmarksContract.BookmarksEntry.COLUMN_TEXT, bibleVerse.getText());
        contentValues.put(BookmarksContract.BookmarksEntry.COLUMN_EXPLANATION, bibleVerse.getExplanation());

        long newRowId = db.insert(BookmarksContract.BookmarksEntry.TABLE_NAME, null, contentValues);
        if (newRowId == -1) {
            Toast.makeText(this, "Error saving bookmark", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Bookmark saved with row id: " + newRowId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_results, menu);
        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, createShareText());
        shareActionProvider.setShareIntent(shareIntent);
        return true;
    }

    private String createShareText() {
        String verse = resultBox1.getText().toString();
        String explanation = resultBox2.getText().toString();
        return "Verse: " + verse + "\n\nExplanation: " + explanation;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        bookmarkRef = null;
        Log.d(TAG, "Results activity destroyed.");
    }
}
