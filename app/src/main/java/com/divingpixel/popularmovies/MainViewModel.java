package com.divingpixel.popularmovies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;
import com.divingpixel.popularmovies.internet.TheMovieDB;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String LOG_TAG = MainViewModel.class.getSimpleName();
    private LiveData<List<MyMovieEntry>> popular, topRated, favorites;

    public MainViewModel(@NonNull Application application) {
        super(application);
        MoviesDatabase moviesDB = MoviesDatabase.getInstance(application.getBaseContext());
        favorites = moviesDB.myMovieDAO().loadFavoriteMovies();
        popular = moviesDB.myMovieDAO().loadPopularMovies();
        topRated = moviesDB.myMovieDAO().loadTopRatedMovies();
    }

    void updateMovieDatabase(MoviesDatabase moviesDB, Context context) {
        TheMovieDB.getMovieList(Utils.CATEGORY_TOP_RATED, moviesDB, context);
        TheMovieDB.getMovieList(Utils.CATEGORY_POPULAR, moviesDB, context);
    }

    LiveData<List<MyMovieEntry>> getMovies(String category) {
        switch (category) {
            default:
                Log.i(LOG_TAG, "RETURNING " + category.toUpperCase() + " MOVIES");
            case Utils.CATEGORY_FAVORITES: {
                return favorites;
            }
            case Utils.CATEGORY_POPULAR: {
                return popular;
            }
            case Utils.CATEGORY_TOP_RATED: {
                return topRated;
            }
        }
    }
}
