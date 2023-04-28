package com.example.soulscript;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarkViewHolder> {
    private ArrayList<BibleVerse> bibleVerses;
    private Context context;

    public BookmarksAdapter(Context context, ArrayList<BibleVerse> bibleVerses) {
        this.context = context;
        this.bibleVerses = bibleVerses;
    }

    @NonNull
    @Override
    public BookmarkViewHolder onCreateViewHolder(@NonNull
                                                     ViewGroup parent, int viewType) {
        return BookmarkViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkViewHolder holder, int position) {
        BibleVerse bibleVerse = bibleVerses.get(position);
        holder.bind(bibleVerse);
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


    @Override
    public int getItemCount() {
        return bibleVerses.size();
    }
}