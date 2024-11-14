package com.example.music.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SongModel implements Parcelable {

    private String id;
    private String title;
    private String subtitle;
    private String coverUrl;
    private String url;
    private String userId;

    // Constructor
    public SongModel() {
        // Empty constructor required for Firestore
    }

    // Constructor with parameters
    public SongModel(String id, String title, String subtitle, String coverUrl, String url, String userId) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.coverUrl = coverUrl;
        this.url = url;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Parcelable implementation
    protected SongModel(Parcel in) {
        id = in.readString();
        title = in.readString();
        subtitle = in.readString();
        coverUrl = in.readString();
        url = in.readString();
        userId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(coverUrl);
        dest.writeString(url);
        dest.writeString(userId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SongModel> CREATOR = new Creator<SongModel>() {
        @Override
        public SongModel createFromParcel(Parcel in) {
            return new SongModel(in);
        }

        @Override
        public SongModel[] newArray(int size) {
            return new SongModel[size];
        }
    };
}
