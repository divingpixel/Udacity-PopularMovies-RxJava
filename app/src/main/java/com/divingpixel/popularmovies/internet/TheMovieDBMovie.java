package com.divingpixel.popularmovies.internet;

public class TheMovieDBMovie {
    private float popularity;
    private int vote_count;
    private String video;
    private String poster_path;
    private int id;
    private String adult;
    private String backdrop_path;
    private String original_language;
    private String original_title;
    private int[] genre_ids;
    private String title;
    private float vote_average;
    private String overview;
    private String release_date;

    public int getId() {
        return id;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public String getTitle() {
        return title;
    }

    public float getVote_average() {
        return vote_average;
    }

    public String getOverview() {
        return overview;
    }

    public String getRelease_date() {
        return release_date;
    }
}
