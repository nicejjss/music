package com.example.music.model;


import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommentModel {
    private String commentId, content, userId, username;
    private int likeCount;
    private String  songId, avatarUrl;private Timestamp timestamp;
    private boolean liked;

    public CommentModel(){

    }

    public CommentModel(String commentId, String content, String userId, String username, int likeCount, Timestamp timestamp, String songId, String avatarUrl, boolean liked) {
        this.commentId = commentId;
        this.content = content;
        this.userId = userId;
        this.username = username;
        this.likeCount = likeCount;
        this.timestamp = timestamp;
        this.songId = songId;
        this.avatarUrl = avatarUrl;
        this.liked = liked;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getTimestamp() {
        Date date = timestamp.toDate();

        // Định dạng ngày tháng năm
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        return formattedDate;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
