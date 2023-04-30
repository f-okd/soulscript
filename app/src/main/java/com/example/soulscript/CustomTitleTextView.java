package com.example.soulscript;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

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