package com.example.soulscript;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Request;



public class Home extends AppCompatActivity {

    FirebaseAuth auth;
    TextView textViewUserDetails;
    TextInputEditText userInput;
    FirebaseUser user;
    ImageButton buttonSettings;
    Button buttonSearch, buttonBookmarks;
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    private NetworkChangeReceiver networkChangeReceiver;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        buttonSettings = findViewById(R.id.settings_button);
        buttonSearch = findViewById(R.id.search_button);
        buttonBookmarks = findViewById(R.id.bookmarksButton);
        userInput = findViewById(R.id.user_input);

        /* If user not logged in, open login activity and close main activity/home page*/
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else {
            //
        }

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = String.valueOf(userInput.getText());
                callApi(message);
                Log.d("Home", "Search button clicked");

            }
        });

        buttonBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Bookmarks.class);
                startActivity(intent);
            }
        });
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                startActivity(intent);
            }
        });

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                runOnUiThread(() -> Toast.makeText(Home.this, "The app won't work without an internet connection!", Toast.LENGTH_LONG).show());
            }
        };

        networkChangeReceiver = new NetworkChangeReceiver(networkCallback);

        // Schedule daily verse notification
        scheduleDailyVerseNotification();
    }

    void errorMessage(String error) {
        Toast.makeText(Home.this, error, Toast.LENGTH_SHORT).show();
    }
    void PostResult(String output) {
        Log.d("Home", "PostResult called with output: " + output);
        Intent intent = new Intent(getApplicationContext(), Results.class);
        intent.putExtra("resultText", output);
        startActivity(intent);
    }

    // Send request to chatgpt api using okhttp
    void callApi(String problemDescription) {
        // https://square.github.io/okhttp/ - get implementation for gradle
        // format prompt with problem description
        String prompt = String.format("Find me a RANDOM bible verse to help with this problem description " +
                "and provide an explanation:\n\n%s\n\nSend the response in json format with the fields: verse, text, explanation", problemDescription);

        JSONObject jsonBody = new JSONObject();
        try {
            // Create  completion: https://platform.openai.com/docs/api-reference/completions/create
            jsonBody.put("model", "text-davinci-003");
            jsonBody.put("prompt", prompt);
            jsonBody.put("max_tokens", 2000);
            jsonBody.put("temperature", 0);
        } catch(JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        // Post to openai server using okhttp: https://square.github.io/okhttp/#post-to-a-server
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Home", "API request failed: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        errorMessage("Failed to load response due to "+e.getMessage());
                    }
                });

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(response.peekBody(Long.MAX_VALUE).string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0).getString("text");
                        PostResult(result);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorMessage("Failed to load response due to "+response.body().toString());
                        }
                    });

                }
            }
        });
    }

    // Schedule daily verse notification using AlarmManager
    private void scheduleDailyVerseNotification() {
        Intent serviceIntent = new Intent(Home.this, DailyVerseService.class);
        PendingIntent pendingIntent = PendingIntent.getService(Home.this, 0, serviceIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8); // Set the desired time, e.g., 8 AM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1); // If it's past the desired time, schedule for the next day
        }

        // Repeat the alarm every day
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerNetworkChangeReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkChangeReceiver();
    }

    private void registerNetworkChangeReceiver() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        }
    }

    private void unregisterNetworkChangeReceiver() {
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}

