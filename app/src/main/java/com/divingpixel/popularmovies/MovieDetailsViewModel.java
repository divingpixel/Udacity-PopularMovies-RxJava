package com.divingpixel.popularmovies;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.divingpixel.popularmovies.database.MoviesDatabase;
import com.divingpixel.popularmovies.datamodel.MyMovieEntry;
import com.divingpixel.popularmovies.datamodel.TheMovieDBReview;
import com.divingpixel.popularmovies.datamodel.TheMovieDBTrailer;
import com.divingpixel.popularmovies.internet.TheMovieDBClient;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

class MovieDetailsViewModel extends ViewModel {

    private static final String LOG_TAG = MovieDetailsViewModel.class.getSimpleName();
    private Single<MyMovieEntry> movie;
    private int movieId;
    private Single<List<TheMovieDBReview>> reviews;
    private Single<List<TheMovieDBTrailer>> trailers;

    MovieDetailsViewModel(MoviesDatabase database, int movieId) {
        movie = database.myMovieDAO().getMovieById(movieId);
        this.movieId = movieId;
        Log.i(LOG_TAG,"GETTING TRAILERS AND REVIEWS");
        getMovieDBReviews();
        getMovieDBTrailers();
    }

    Single<MyMovieEntry> getMovie() {
        return movie;
    }

    Single<List<TheMovieDBReview>> getReviews() {
        return reviews;
    }

    Single<List<TheMovieDBTrailer>> getTrailers() {
        return trailers;
    }

    private void getMovieDBReviews() {
        reviews = TheMovieDBClient.getInstance()
                .getMovieReviews(movieId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .cache();
    }

    private void getMovieDBTrailers() {
        trailers = TheMovieDBClient.getInstance()
                .getMovieTrailers(movieId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .cache();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
