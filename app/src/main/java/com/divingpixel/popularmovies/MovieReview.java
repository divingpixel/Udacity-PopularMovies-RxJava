package com.divingpixel.popularmovies;

public class MovieReview {

    private int mMovieId;
    private String mReviewer, mText;

    public MovieReview (int movieID, String reviewer, String text){
        mMovieId = movieID;
        mReviewer = reviewer;
        mText = text;
    }

    public int getMovieId() {
        return mMovieId;
    }

    public String getReviewer() {
        return mReviewer;
    }

    public String getText() {
        return mText;
    }
}
