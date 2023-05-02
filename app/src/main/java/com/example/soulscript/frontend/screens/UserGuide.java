package com.example.soulscript.frontend.screens;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

import com.example.soulscript.R;

public class UserGuide extends AppCompatActivity {
    private WebView webViewUserGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        webViewUserGuide = findViewById(R.id.webViewUserGuide);

        // Enable JavaScript
        webViewUserGuide.getSettings().setJavaScriptEnabled(true);

        // Load the user guide from the assets folder
        webViewUserGuide.loadUrl("file:///android_asset/user_guide/index.html");
    }
}

