package com.divingpixel.popularmovies;

import androidx.lifecycle.ViewModel;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;

import io.reactivex.Single;

class MovieDetailsViewModel extends ViewModel {

    private Single<MyMovieEntry> movie;

    MovieDetailsViewModel(MoviesDatabase database, int movieId) {
        movie = database.myMovieDAO().getMovieById(movieId);
    }

    Single<MyMovieEntry> getMovie() {
        return movie;
    }
}
