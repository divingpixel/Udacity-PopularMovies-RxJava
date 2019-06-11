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
        MoviesDatabase moviesDB = MoviesDatabase.getInstance(application);
        favorites = moviesDB.myMovieDAO().loadFavoriteMovies();
        popular = moviesDB.myMovieDAO().loadPopularMovies();
        topRated = moviesDB.myMovieDAO().loadTopRatedMovies();
    }

    void updateMovieDatabase(MoviesDatabase moviesDB, Context context) {
        TheMovieDB.getMovieList(Utils.CATEGORY_TOP_RATED, moviesDB, context);
        TheMovieDB.getMovieList(Utils.CATEGORY_POPULAR, moviesDB, context);
    }

    public LiveData<List<MyMovieEntry>> getFavorites() {
        Log.e(LOG_TAG, "RETURNING FAVORITE MOVIES");
        return favorites;
    }

    public LiveData<List<MyMovieEntry>> getPopular() {
        Log.e(LOG_TAG, "RETURNING POPULAR MOVIES");
        return popular;
    }

    public LiveData<List<MyMovieEntry>> getTopRated() {
        Log.e(LOG_TAG, "RETURNING TOP RATED MOVIES");
        return topRated;
    }

}
