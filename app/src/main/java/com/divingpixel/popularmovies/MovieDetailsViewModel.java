package com.divingpixel.popularmovies;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;

class MovieDetailsViewModel extends ViewModel {

    private LiveData<MyMovieEntry> movie;

    MovieDetailsViewModel(MoviesDatabase database, int movieId) {
        movie = database.myMovieDAO().loadMovieById(movieId);
    }

    LiveData<MyMovieEntry> getMovie() {
        return movie;
    }
}
