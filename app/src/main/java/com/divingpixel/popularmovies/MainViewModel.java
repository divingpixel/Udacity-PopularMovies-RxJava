package com.divingpixel.popularmovies;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.database.MyMovieEntry;
import com.divingpixel.popularmovies.internet.TheMovieDBMovie;
import com.divingpixel.popularmovies.internet.TheMovieDBClient;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.divingpixel.popularmovies.PopularMovies.CATEGORY_FAVORITES;
import static com.divingpixel.popularmovies.PopularMovies.CATEGORY_POPULAR;
import static com.divingpixel.popularmovies.PopularMovies.CATEGORY_TOP_RATED;

public class MainViewModel extends AndroidViewModel {

    private static final String LOG_TAG = MainViewModel.class.getSimpleName();
    private Observable<List<MyMovieEntry>> popular, topRated, favorites;
    private MoviesDatabase moviesDB;
    private Disposable disposable;

    public MainViewModel(@NonNull Application application) {
        super(application);
        moviesDB = MoviesDatabase.getInstance(application.getBaseContext());
        favorites = moviesDB.myMovieDAO().loadFavoriteMovies();
        popular = moviesDB.myMovieDAO().loadPopularMovies();
        topRated = moviesDB.myMovieDAO().loadTopRatedMovies();
    }

    private void fillDatabase(final String category) {
        disposable = TheMovieDBClient.getInstance()
                .getSelectedMovies(category)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(movieEntries -> {
                    MyMovieEntry newMovie;
                    if (category.equals(CATEGORY_POPULAR))
                        moviesDB.myMovieDAO().deletePopularMovies();
                    else moviesDB.myMovieDAO().deleteTopRatedMovies();
                    int popIndex = 0, topIndex = 0, index = 0;
                    boolean favorite = false;
                    for (TheMovieDBMovie movie : movieEntries) {
                        popIndex=topIndex=index++;
                        final MyMovieEntry[] dbMovie = {null};
                        moviesDB.myMovieDAO().getMovieById(movie.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(Schedulers.io())
                                .subscribe(new DisposableSingleObserver<MyMovieEntry>() {
                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onSuccess(MyMovieEntry movie) {
                                        dbMovie[0] = movie;
                                    }
                                });
                        if (dbMovie[0] != null) {
                            favorite = dbMovie[0].isFavorite();
                            if (category.equals(PopularMovies.CATEGORY_POPULAR)) {
                                topIndex = dbMovie[0].getTopIndex();
                            } else {
                                popIndex = dbMovie[0].getPopIndex();
                            }
                        }

                        newMovie = new MyMovieEntry(movie.getId(), popIndex, topIndex,
                                movie.getTitle(),
                                movie.getRelease_date(),
                                movie.getOverview(),
                                movie.getPoster_path(),
                                movie.getVote_average(),
                                favorite);
                        //moviesDB.myMovieDAO().insertMovie(newMovie);
                        Log.i(LOG_TAG,"Downloaded movie " + movie.getTitle());
                    }
                });
    }

    void updateMovieDatabase() {
        fillDatabase(CATEGORY_POPULAR);
        fillDatabase(CATEGORY_TOP_RATED);
    }

    Observable<List<MyMovieEntry>> getMovies(String category) {
        switch (category) {
            default:
                Log.i(LOG_TAG, "RETURNING " + category.toUpperCase() + " MOVIES");
            case CATEGORY_FAVORITES: {
                return favorites;
            }
            case CATEGORY_POPULAR: {
                return popular;
            }
            case CATEGORY_TOP_RATED: {
                return topRated;
            }
        }
    }

    @Override
    protected void onCleared() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onCleared();
    }
}