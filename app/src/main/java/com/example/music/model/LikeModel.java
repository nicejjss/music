package com.example.music.model;

public class LikeModel {
    private int songId;
    private int userId;

    public LikeModel() {
        // Default constructor required for calls to DataSnapshot.getValue(LikeModel.class)
    }

    public LikeModel(int songId, int userId) {
        this.songId = songId;
        this.userId = userId;
    }

    public int getSongId() {
        return songId;
    }

    public int getUserId() {
        return userId;
    }
}