package com.divingpixel.popularmovies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String LOG_TAG = MainViewModel.class.getSimpleName();
    private LiveData<List<MyMovieEntry>> movies, favorites;
    private MoviesDatabase moviesDB;

    public MainViewModel(@NonNull Application application) {
        super(application);
        moviesDB = MoviesDatabase.getInstance(this.getApplication());
        favorites = moviesDB.myMovieDAO().loadFavoriteMovies();
    }

   public LiveData<List<MyMovieEntry>> getMovies() {
        if (PopularMovies.showFavorites) {
            Log.d(LOG_TAG, "Returning FAVORITES from ViewModel");
            return favorites;
        } else if (PopularMovies.category.equalsIgnoreCase(Utils.CATEGORY_POPULAR)){
            movies = moviesDB.myMovieDAO().loadPopularMovies();
            Log.d(LOG_TAG, "Returning MOVIES CATEGORY " + PopularMovies.category.toUpperCase() + " from ViewModel");
            return movies;
        } else {
            movies = moviesDB.myMovieDAO().loadTopRatedMovies();
            Log.d(LOG_TAG, "Returning MOVIES CATEGORY " + PopularMovies.category.toUpperCase() + " from ViewModel");
            return movies;
        }
    }
}
