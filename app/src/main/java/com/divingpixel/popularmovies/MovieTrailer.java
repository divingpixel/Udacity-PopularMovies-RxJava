package com.divingpixel.popularmovies;

public class MovieTrailer {

    private int mMovieId;
    private String mTitle, mKey;

    public MovieTrailer (int movieID, String name, String key){
        mMovieId = movieID;
        mTitle = name;
        mKey = key;
    }

    public int getMovieId() {
        return mMovieId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getKey() {
        return mKey;
    }
}
