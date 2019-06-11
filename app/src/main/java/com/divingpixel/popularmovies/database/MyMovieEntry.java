package com.divingpixel.popularmovies.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = "movies")
public class MyMovieEntry {

    @PrimaryKey
    private int mId;
    private int mPopIndex,mTopIndex;
    private String mTitle, mDate, mSynopsis, mPosterUrl;
    private float mRating;
    private boolean mFavorite;

    public MyMovieEntry(int id, int popIndex, int topIndex, String title, String date, String synopsis, String posterUrl, float rating, boolean favorite) {
        mId = id;
        mPopIndex = popIndex;
        mTopIndex = topIndex;
        mTitle = title;
        mDate = date;
        mSynopsis = synopsis;
        mPosterUrl = posterUrl;
        mRating = rating;
        mFavorite = favorite;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getPopIndex() {
        return mPopIndex;
    }

    public int getTopIndex() {
        return mTopIndex;
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

    public void setFavorite(boolean favorite) {
        mFavorite = favorite;
    }

    public boolean isFavorite() {
        return mFavorite;
    }
}
