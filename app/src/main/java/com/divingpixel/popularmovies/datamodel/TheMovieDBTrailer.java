package com.divingpixel.popularmovies.datamodel;

public class TheMovieDBTrailer {

    private String id, iso_639_1, iso_3166_1, key, name, site;
    private int size;
    private String type;

    public TheMovieDBTrailer(String name, String key, String site){
        this.name = name;
        this.key = key;
        this.site = site;
    }

    public String getTitle() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
