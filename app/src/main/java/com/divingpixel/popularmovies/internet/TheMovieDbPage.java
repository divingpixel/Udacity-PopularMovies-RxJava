package com.divingpixel.popularmovies.internet;

import java.util.List;

class TheMovieDbPage {
        int page;
        int total_results;
        int total_pages;
        private List<TheMovieDBMovie> results;

    List<TheMovieDBMovie> getResults() {
        return results;
    }

}
