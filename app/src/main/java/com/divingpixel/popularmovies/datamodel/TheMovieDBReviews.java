package com.divingpixel.popularmovies.datamodel;

import java.util.List;

public class TheMovieDBReviews {
    int id;
    int page;
    private List<TheMovieDBReview> results;
    int total_pages;
    int total_results;

    public List<TheMovieDBReview> getResults() {
        return results;
    }
}
