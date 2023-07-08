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
                String prompt = String.format("I asked someone how they're feeling and they said:\n\n'%s'\n\nFind me a RANDOM bible verse to help with this " +
                        "and provide an explanation.\n\nSend the response in json format with the fields: verse, text, explanation. Use single quotation marks for speech marks, reserve double quotation marks for json formatting to avoid syntax errors. example output: {\n" +
                        "    \"verse\": \"John 14:18\",\n" +
                        "    \"text\": \"I will not leave you as orphans; I will come to you.\",\n" +
                        "    \"explanation\": \"This verse is a reminder that even though our earthly fathers may pass away, we are never truly alone. God is always with us and will never leave us. He is our heavenly Father and He will always be there to comfort us in our time of need.\"\n" +
                        "}", message);

                callApi(prompt);
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
                        String prompt = "Please recommend a verse that has the same themes as these verses, with an explanation,: " + verses.toString() + "Send the response in json format with the fields: verse, text, explanation. Use single quotation marks for speech marks, reserve double quotation marks for json formatting to avoid syntax errors. example output: {\\n\" +\n" +
                                "                \"    \\\"verse\\\": \\\"John 14:18\\\",\\n\" +\n" +
                                "                \"    \\\"text\\\": \\\"I will not leave you as orphans; I will come to you.\\\",\\n\" +\n" +
                                "                \"    \\\"explanation\\\": \\\"This verse is a reminder that even though our earthly fathers may pass away, we are never truly alone. God is always with us and will never leave us. He is our heavenly Father and He will always be there to comfort us in our time of need.\\\"\\n\" +\n" +
                                "                \"}\"";

                        // Make an API call using the message with the most recent bookmarked verses
                        callApi(prompt);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Home.this, error, Toast.LENGTH_SHORT).show();
                hideProgressBarAndEnableButtons();
            }
        });
    }

    void postResult(String output) {
        Log.d("Home", "postResult called with output: " + output);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), Results.class);
                intent.putExtra("resultText", output);
                startActivity(intent);
                hideProgressBarAndEnableButtons();
            }
        });
    }


    // Send request to chatgpt api using okhttp
    void callApi(String prompt) {
        Log.d("Home", "callApi called with prompt: " + prompt);
        JSONObject jsonBody = new JSONObject();
        try {
            // Create  completion: https://platform.openai.com/docs/api-reference/completions/create
            jsonBody.put("model", "text-davinci-003");
            jsonBody.put("prompt", prompt);
            jsonBody.put("max_tokens", 2048);
            // Temperature at one so that the output is a bit more random
            jsonBody.put("temperature", 1);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "{ENTER API KEY")
                .post(body)
                .build();

        // Post to openai server using okhttp: https://square.github.io/okhttp/#post-to-a-server
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Home", "API request failed: " + e.getMessage());
                errorMessage("Failed to load response due to "+e.getMessage());
            }

            // Parse response from openai server and send to postResult function
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.peekBody(Long.MAX_VALUE).string());
                        // Log raw response from the API
                        Log.d("Home", "API response: " + jsonObject.toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");

                        // Send result to postResult function
                        postResult(result);

                    } catch (JSONException e) {
                        Log.d("Home","Failed to load response due to "+response.code());
                        throw new RuntimeException(e);

                    }
                    Log.d("Home", "API request successful");

                } else {
                    Log.d("Home", "Failed to get a response from the server. Response code: " + response.code() + ", message: " + response.message());
                    errorMessage("Failed to load response due to "+response.body().toString());
                }
            }
        });
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
            // calendar.add(Calendar.SECOND, 15); // For testing purposes, set the alarm to go off 30 seconds after scheduling

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

