package com.divingpixel.popularmovies.datamodel;

import java.util.List;

public class TheMovieDbPage {
        int page;
        int total_results;
        int total_pages;
        private List<TheMovieDBMovie> results;

    public List<TheMovieDBMovie> getResults() {
        return results;
    }

}
