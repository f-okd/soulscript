package com.example.soulscript;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*
* This class extends RecyclerView.ViewHolder and is responsible for holding and displaying individual bookmarked Bible verses within the RecyclerView.
* It contains a single TextView for displaying the verse.
* The create method is responsible for inflating the layout for each individual bookmark item and creating an instance of BookmarkViewHolder.
* The bind method is responsible for binding the data (i.e., the BibleVerse object) to the TextView in the view holder.
* */
public class BookmarkViewHolder extends RecyclerView.ViewHolder {
    public TextView verseText;

    public BookmarkViewHolder(@NonNull View itemView) {
        super(itemView);
        verseText = itemView.findViewById(R.id.verse_text);
    }

    // This method is used to inflate the layout for each individual bookmark item and create an instance of BookmarkViewHolder.
    public static BookmarkViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bookmark, parent, false);
        return new BookmarkViewHolder(view);
    }
    // This method is used to bind the data (i.e., the BibleVerse object) to the TextView in the view holder.
    public void bind(BibleVerse bibleVerse) {
        verseText.setText(bibleVerse.getVerse());
        Log.d("BookmarkViewHolder", "Binding data with verse: " + bibleVerse.getVerse());
    }

}
