package com.example.soulscript.frontend.screens;

// Import necessary classes:
// The code imports several classes from the Android SDK and Firebase Authentication libraries.
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.soulscript.backend.DailyVerseService;
import com.example.soulscript.R;
import com.example.soulscript.frontend.screens.bookmarks.Bookmarks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;

import android.view.GestureDetector;
import android.view.MotionEvent;



public class Home extends AppCompatActivity {
    // Declare variables:
    FirebaseAuth auth;
    TextView textViewUserDetails;
    TextInputEditText userInput;
    FirebaseUser user;
    ImageButton buttonSettings;
    Button buttonSearch, buttonBookmarks, buttonRecommend;
    GestureDetector gestureDetector;
    ProgressBar progressBar;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    // Instantiate OkHttpClient to make API calls
    OkHttpClient client = new OkHttpClient();
    private DatabaseReference bookmarkRef;
    private String databaseUrl;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d("Home", "Activity created");

        // Initialize Firebase authentication objects:
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Initialize UI elements:
        buttonSettings = findViewById(R.id.settings_button);
        buttonSearch = findViewById(R.id.search_button);
        buttonRecommend = findViewById(R.id.recommendButton);
        buttonBookmarks = findViewById(R.id.bookmarksButton);
        userInput = findViewById(R.id.user_input);
        progressBar = findViewById(R.id.progress_bar);

        // Get a reference to the Firebase Realtime Database location for the user's bookmarks
        databaseUrl = "https://soulscript-7fb99-default-rtdb.europe-west1.firebasedatabase.app//";
        bookmarkRef = FirebaseDatabase.getInstance(databaseUrl).getReference().child("bookmarks").child(user.getUid());

        // Initialize gesture detector:
        // The code initializes a gesture detector to detect double taps on the home page.
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Intent intent = new Intent(getApplicationContext(), Bookmarks.class);
                startActivity(intent);
                return true;
            }
        });

        // Check if user is logged in:
        // The code checks if the user is logged in. If not, the user is redirected to the login page.
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            //
        }

        // The code sets an onclick listener for the search button.
        // When the button is clicked, the app calls the chatgpt api.
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBarAndDisableButtons();
                String message = String.valueOf(userInput.getText());
                callApi(message);
                Log.d("Home", "Search button clicked");

            }
        });

        // The code sets an onclick listener for the bookmarks button.
        // When the button is clicked, the app opens the bookmarks activity.
        buttonBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Bookmarks.class);
                startActivity(intent);
            }
        });

        // Set an onClick listener for the recommend button
        buttonRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBarAndDisableButtons();
                // Fetch the 10 most recent bookmarked verses from the Firebase Realtime Database
                // orderByChild("timestamp") sorts the verses by their timestamp
                // limitToLast(10) limits the fetched data to the 10 most recent verses
                bookmarkRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Create a StringBuilder to store the verses
                        StringBuilder verses = new StringBuilder();

                        // Iterate through each DataSnapshot (bookmarked verse) and get the verse text
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String verse = snapshot.child("verse").getValue(String.class);
                            if (verse != null) {
                                // Append the verse and a comma to the StringBuilder
                                verses.append(verse).append(", ");
                            }
                        }

                        // Remove the trailing comma and space from the StringBuilder
                        if (verses.length() > 0) {
                            verses.setLength(verses.length() - 2);
                        }

                        // Create a message containing the most recent bookmarked verses
                        String message = "Ignore what was said previously. Please recommend a verse that has the same themes as these verses: " + verses.toString();

                        // Make an API call using the message with the most recent bookmarked verses
                        callApi(message);
                        Log.d("Home", "Recommend button clicked");
                    }

                    // Handle onCancelled event if the Firebase Realtime Database operation fails
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.w("Home", "Failed to read value.", databaseError.toException());
                        errorMessage("Failed to load bookmarked verses.");
                    }
                });
            }
        });


        // The code sets an onclick listener for the settings image button.
        // When the button is clicked, the app opens the settings activity.
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
            }
        });


        // Listen for network changes:
        // The code listens for network changes and disables the search and recommend buttons if the network is unavailable.
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                Intent intent = new Intent("NETWORK_STATUS");
                intent.putExtra("status", true);
                LocalBroadcastManager.getInstance(Home.this).sendBroadcast(intent);

                runOnUiThread(() -> {
                    buttonSearch.setEnabled(true);
                    buttonRecommend.setEnabled(true);
                });
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                Intent intent = new Intent("NETWORK_STATUS");
                intent.putExtra("status", false);
                LocalBroadcastManager.getInstance(Home.this).sendBroadcast(intent);

                runOnUiThread(() -> {
                    buttonSearch.setEnabled(false);
                    buttonRecommend.setEnabled(false);
                    Toast.makeText(Home.this, "The app won't work completely without an internet connection!", Toast.LENGTH_LONG).show();
                });
            }
        };

        // Schedule daily verse notification
        scheduleDailyVerseNotification();
    }

    // Method to show the progress bar and disable buttons
    private void showProgressBarAndDisableButtons() {
        progressBar.setVisibility(View.VISIBLE);
        buttonSearch.setEnabled(false);
        buttonRecommend.setEnabled(false);
        buttonBookmarks.setEnabled(false);
        buttonSettings.setEnabled(false);
    }

    // Method to hide the progress bar and enable buttons
    private void hideProgressBarAndEnableButtons() {
        progressBar.setVisibility(View.GONE);
        buttonSearch.setEnabled(true);
        buttonRecommend.setEnabled(true);
        buttonBookmarks.setEnabled(true);
        buttonSettings.setEnabled(true);
    }

    void errorMessage(String error) {
        Toast.makeText(Home.this, error, Toast.LENGTH_SHORT).show();
        hideProgressBarAndEnableButtons();
    }

    // Used to open results activity with output from chatgpt api
    void PostResult(String output) {
        Log.d("Home", "PostResult called with output: " + output);
        Intent intent = new Intent(getApplicationContext(), Results.class);
        intent.putExtra("resultText", output);
        startActivity(intent);
        hideProgressBarAndEnableButtons();
    }

    // Send request to chatgpt api using okhttp
    void callApi(String problemDescription) {
        showProgressBarAndDisableButtons();
        new CallApiTask().execute(problemDescription);
    }

    private class CallApiTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... problemDescriptions) {
            String problemDescription = problemDescriptions[0];

            // Check if any fields are empty
            if (TextUtils.isEmpty(problemDescription)) {
                runOnUiThread(() -> {
                    Toast.makeText(Home.this, "Please type something first", Toast.LENGTH_SHORT).show();
                    hideProgressBarAndEnableButtons();
                });
                return null;
            }

            // The rest of the code in the callApi method
            String prompt = String.format("Find me a RANDOM bible verse to help with this problem description, " +
                    "and provide an explanation, referencing the problem:\n\n%s\n\nSend the response in json format with the fields: verse, text, explanation", problemDescription);

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("model", "text-davinci-003");
                jsonBody.put("prompt", prompt);
                jsonBody.put("max_tokens", 2000);
                jsonBody.put("temperature", 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/completions")
                    .header("Authorization", "Bearer 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")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = jsonObject.getJSONArray("choices");
                    return jsonArray.getJSONObject(0).getString("text");
                } else {
                    runOnUiThread(() -> {
                        errorMessage("Failed to load response due to " + response.body().toString());
                    });
                    return null;
                }

            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    errorMessage("Failed to load response due to " + e.getMessage());
                });
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                PostResult(result);
            } else {
                hideProgressBarAndEnableButtons();
            }
        }
    }

    // Schedule daily verse notification using AlarmManager
    private void scheduleDailyVerseNotification() {
        SharedPreferences sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        boolean dailyNotificationsEnabled = sharedPreferences.getBoolean("daily_notifications_enabled", true);

        if (dailyNotificationsEnabled) {
            Intent serviceIntent = new Intent(Home.this, DailyVerseService.class);
            PendingIntent pendingIntent = PendingIntent.getService(Home.this, 0, serviceIntent, 0);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 8); // Set the desired time, 8 AM
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            //calendar.add(Calendar.SECOND, 30); // For testing purposes, set the alarm to go off 30 seconds after scheduling

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DATE, 1); // If it's past the desired time, schedule for the next day
            }

            // Repeat the alarm every day
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            Log.d("Home", "Daily verse notification scheduled for " + calendar.getTime().toString());
        }
    }


    // Register network change receiver when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkChangeReceiver();
        Log.d("Home", "Activity resumed");
    }

    // Unregister network change receiver when activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkChangeReceiver();
        Log.d("Home", "Activity paused");
    }

    // Check if network is connected
    private void registerNetworkChangeReceiver() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        }
    }

    // Unregister network change receiver
    private void unregisterNetworkChangeReceiver() {
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

}

