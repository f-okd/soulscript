package com.example.soulscript;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BookmarkViewHolder extends RecyclerView.ViewHolder {
    public TextView verseText;

    public BookmarkViewHolder(@NonNull View itemView) {
        super(itemView);
        verseText = itemView.findViewById(R.id.verse_text);
    }

    public static BookmarkViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }

    public void bind(BibleVerse bibleVerse) {
        verseText.setText(bibleVerse.getVerse());
        Log.d("BookmarkViewHolder", "Binding data with verse: " + bibleVerse.getVerse());
    }

}
