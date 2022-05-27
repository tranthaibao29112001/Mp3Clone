package com.example.mp3clone;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class Song implements Parcelable {
    private String singer;
    private String thumbnail;
    private String title;
    private String url;

    public Song() {
    }

    protected Song(Parcel in) {
        singer = in.readString();
        thumbnail = in.readString();
        title = in.readString();
        url = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Song(String singer, String thumbnail, String title, String url) {
        this.singer = singer;
        this.thumbnail = thumbnail;
        this.title = title;
        this.url = url;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Song temp = (Song)obj;
        return temp.getUrl().equals(this.getUrl());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(singer);
        dest.writeString(thumbnail);
        dest.writeString(title);
        dest.writeString(url);
    }
}
