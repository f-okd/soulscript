package com.example.soulscript.frontend.screens.bookmarks;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.soulscript.frontend.BibleVerse;
import com.example.soulscript.frontend.screens.Results;

import java.util.ArrayList;


/*
* This class extends RecyclerView.Adapter<BookmarkViewHolder> and is responsible for providing the RecyclerView with the data it needs to display the bookmarked verses.
* It takes an ArrayList of BibleVerse objects and a Context as input.
* The adapter is responsible for creating view holders using the onCreateViewHolder method and binding the data to each view holder using the onBindViewHolder method.
* It also handles click events on each bookmark item, which opens the results activity to display the verse details and explanation.
* */
public class BookmarksAdapter extends RecyclerView.Adapter<BookmarkViewHolder> {
    private ArrayList<BibleVerse> bibleVerses;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener;
    public interface OnDeleteClickListener {
        void onDeleteClick(BibleVerse bibleVerse);
    }


    public BookmarksAdapter(Context context, ArrayList<BibleVerse> bibleVerses, OnDeleteClickListener onDeleteClickListener) {
        this.context = context;
        this.bibleVerses = bibleVerses;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull
                                                     ViewGroup parent, int viewType) {
        return BookmarkViewHolder.create(parent);
    }

    // This method is used to bind the data to the view holder.
    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        BibleVerse bibleVerse = bibleVerses.get(position);
        holder.bind(bibleVerse, onDeleteClickListener);
        Log.d("BookmarksAdapter", "Binding data for position: " + position + " with verse: " + bibleVerse.getVerse());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Results.class);
                intent.putExtra("resultText", bibleVerse.toJsonString());
                context.startActivity(intent);
            }
        });
    }

    // This method is used to update the bookmarks list when a new bookmark is added or removed.
    public void setBookmarks(ArrayList<BibleVerse> newBibleVerses) {
        this.bibleVerses = newBibleVerses;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return bibleVerses.size();
    }
}