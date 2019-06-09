package com.divingpixel.popularmovies;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.divingpixel.popularmovies.database.MoviesDatabase;

public class MovieDetailsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final MoviesDatabase mDb;
    private final int movieId;

    public MovieDetailsViewModelFactory(MoviesDatabase database, int taskId) {
        mDb = database;
        movieId = taskId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MovieDetailsViewModel(mDb, movieId);
    }
}
