package com.example.music.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.R;
import com.example.music.SongActivity;
import com.example.music.model.SongModel;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final Context context;
    private final ArrayList<SongModel> songList;

    // Constructor
    public SongAdapter(Context context, ArrayList<SongModel> songList) {
        this.context = context;
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        SongModel song = songList.get(position);

        holder.titleText.setText(song.getTitle());
        holder.subtitleText.setText(song.getSubtitle());
        Glide.with(context)
                .load(song.getCoverUrl())
                .placeholder(R.drawable.logo)
                .into(holder.coverImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SongActivity.class);
            // Truyền songList dưới dạng ArrayList
            intent.putParcelableArrayListExtra("songList", songList);
            intent.putExtra("currentPosition", position);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {

        TextView titleText, subtitleText;
        ImageView coverImage;

        public SongViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.song_title);
            subtitleText = itemView.findViewById(R.id.song_subtitle);
            coverImage = itemView.findViewById(R.id.cover_image);
        }
    }
}
