package com.divingpixel.popularmovies;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.datamodel.MyMovieEntry;
import com.divingpixel.popularmovies.datamodel.TheMovieDBMovie;
import com.divingpixel.popularmovies.internet.TheMovieDBClient;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.divingpixel.popularmovies.PopularMovies.CATEGORY_FAVORITES;
import static com.divingpixel.popularmovies.PopularMovies.CATEGORY_POPULAR;
import static com.divingpixel.popularmovies.PopularMovies.CATEGORY_TOP_RATED;

public class MainViewModel extends AndroidViewModel {

    private static final String LOG_TAG = MainViewModel.class.getSimpleName();
    private Observable<List<MyMovieEntry>> popular, topRated, favorites;
    private MoviesDatabase moviesDB;
    private CompositeDisposable disposable = new CompositeDisposable();

    public MainViewModel(@NonNull Application application) {
        super(application);
        moviesDB = MoviesDatabase.getInstance(application.getBaseContext());
        favorites = moviesDB.myMovieDAO().loadFavoriteMovies();
        popular = moviesDB.myMovieDAO().loadPopularMovies();
        topRated = moviesDB.myMovieDAO().loadTopRatedMovies();
    }

    private void processMovieList(String category, List<TheMovieDBMovie> movieEntries) {
        int index = 0;
        for (TheMovieDBMovie dbMovie : movieEntries) {
            index++;
            int finalIndex = index;
            moviesDB.myMovieDAO().getMovieById(dbMovie.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(new DisposableSingleObserver<MyMovieEntry>() {
                        @Override
                        public void onError(Throwable e) {
                            int popIndex = 0, topIndex = 0;
                            if (category.equals(PopularMovies.CATEGORY_POPULAR))
                                popIndex = finalIndex;
                            else
                                topIndex = finalIndex;

                            MyMovieEntry selectedMovie =
                                    new MyMovieEntry(dbMovie.getId(),
                                            popIndex,
                                            topIndex,
                                            dbMovie.getTitle(),
                                            dbMovie.getRelease_date(),
                                            dbMovie.getOverview(),
                                            dbMovie.getPoster_path(),
                                            dbMovie.getVote_average(),
                                            false);

                            disposable.add(moviesDB.myMovieDAO().insertMovie(selectedMovie)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.io())
                                    .subscribeWith(new DisposableCompletableObserver() {
                                        @Override
                                        public void onComplete() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e(LOG_TAG, "ERROR UPDATING DATABASE ITEM", e);
                                        }
                                    }));
                            Log.i(LOG_TAG, "MOVIE " + dbMovie.getTitle() + " IS NEW. ADDED TO DATABASE.");
                        }

                        @Override
                        public void onSuccess(MyMovieEntry movie) {
                            int popIndex, topIndex;
                            if (category.equals(PopularMovies.CATEGORY_POPULAR)) {
                                popIndex = finalIndex;
                                topIndex = movie.getTopIndex();
                            } else {
                                topIndex = finalIndex;
                                popIndex = movie.getPopIndex();
                            }
                            MyMovieEntry selectedMovie = new MyMovieEntry(
                                    movie.getId(),
                                    popIndex,
                                    topIndex,
                                    movie.getTitle(),
                                    movie.getDate(),
                                    movie.getSynopsis(),
                                    movie.getPosterUrl(),
                                    movie.getRating(),
                                    movie.isFavorite());

                            disposable.add (moviesDB.myMovieDAO().updateMovie(selectedMovie)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(Schedulers.io())
                                    .subscribeWith(new DisposableCompletableObserver() {
                                        @Override
                                        public void onComplete() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e(LOG_TAG, "ERROR UPDATING DATABASE ITEM", e);
                                        }
                                    }));
                            Log.i(LOG_TAG, "FOUND " + movie.getTitle() + " IN DATABASE. UPDATED.");
                        }
                    });
        }
    }

    void fillDatabase(final String category) {
        disposable.add(TheMovieDBClient.getInstance()
                .getSelectedMovies(category)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribeWith(new DisposableSingleObserver<List<TheMovieDBMovie>>() {
                    @Override
                    public void onSuccess(List<TheMovieDBMovie> theMovieDBMovies) {
                        if (category.equals(CATEGORY_POPULAR))
                            moviesDB.myMovieDAO().deletePopularMovies();
                        else moviesDB.myMovieDAO().deleteTopRatedMovies();
                        processMovieList(category, theMovieDBMovies);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }));

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