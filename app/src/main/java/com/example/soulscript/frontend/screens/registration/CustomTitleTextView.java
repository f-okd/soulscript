package com.example.soulscript.frontend.screens.registration;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.example.soulscript.R;

// This class is used to create a custom title textview for the registration activity,
// ...to distinguish it from the login activity.
public class CustomTitleTextView extends AppCompatTextView {
    public CustomTitleTextView(Context context) {
        super(context);
        init();
    }

    public CustomTitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomTitleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.custom_title_background);
    }
}
