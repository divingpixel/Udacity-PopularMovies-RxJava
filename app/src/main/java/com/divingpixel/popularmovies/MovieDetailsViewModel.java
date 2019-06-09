package com.divingpixel.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;

public class MovieDetailsViewModel extends ViewModel {

    private LiveData<MyMovieEntry> movie;

    public MovieDetailsViewModel(MoviesDatabase database, int movieId) {
        movie = database.myMovieDAO().loadMovieById(movieId);
    }

    public LiveData<MyMovieEntry> getMovie() {
        return movie;
    }
}
