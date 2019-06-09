package com.divingpixel.popularmovies.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = "movies")
public class MyMovieEntry {

    @PrimaryKey
    private int mId;
    private int mIndex;
    private String mTitle, mDate, mSynopsis, mPosterUrl, mUpDate, mCategory;
    private float mRating;
    private boolean mFavorite;

    public MyMovieEntry(int id, int index, String upDate, String title, String date, String synopsis, String posterUrl, float rating, boolean favorite, String category) {
        mId = id;
        mIndex = index;
        mTitle = title;
        mDate = date;
        mSynopsis = synopsis;
        mPosterUrl = posterUrl;
        mRating = rating;
        mFavorite = favorite;
        mUpDate = upDate;
        mCategory = category;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUpDate() {
        return mUpDate;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getDate() {
        return mDate;
    }

    public float getRating() {
        return mRating;
    }

    public String getPosterUrl() {
        return mPosterUrl;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setFavorite(boolean favorite) {
        mFavorite = favorite;
    }

    public boolean isFavorite() {
        return mFavorite;
    }
}
