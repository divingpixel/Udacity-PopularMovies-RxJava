package com.divingpixel.popularmovies;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.divingpixel.popularmovies.database.MoviesDatabase;

import org.jetbrains.annotations.NotNull;

public class MovieDetailsViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final MoviesDatabase mDb;
    private final int movieId;

    MovieDetailsViewModelFactory(MoviesDatabase database, int taskId) {
        mDb = database;
        movieId = taskId;
    }

    @NotNull
    @Override
    public <T extends ViewModel> T create(@NotNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MovieDetailsViewModel(mDb, movieId);
    }
}
