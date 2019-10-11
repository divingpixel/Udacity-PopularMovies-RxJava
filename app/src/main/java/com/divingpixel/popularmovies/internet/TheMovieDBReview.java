package com.divingpixel.popularmovies.internet;

public class TheMovieDBReview {

    private String author, content, id, url;

    public TheMovieDBReview(String reviewer, String text, String reviewId, String reviewUrl){
        author = reviewer;
        content = text;
        id = reviewId;
        url = reviewUrl;
    }

    public String getReviewer() {
        return author;
    }

    public String getText() {
        return content;
    }
}
