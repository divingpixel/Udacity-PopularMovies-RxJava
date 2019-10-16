package com.divingpixel.popularmovies.internet;

import androidx.annotation.NonNull;

import com.divingpixel.popularmovies.datamodel.TheMovieDBMovie;
import com.divingpixel.popularmovies.datamodel.TheMovieDBReview;
import com.divingpixel.popularmovies.datamodel.TheMovieDBReviews;
import com.divingpixel.popularmovies.datamodel.TheMovieDBTrailer;
import com.divingpixel.popularmovies.datamodel.TheMovieDBTrailers;
import com.divingpixel.popularmovies.datamodel.TheMovieDbPage;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.divingpixel.popularmovies.internet.TheMovieDBService.TheMovieDB_BASE_URL;


public class TheMovieDBClient {

    private static TheMovieDBClient instance;
    private TheMovieDBService movieService;

    private TheMovieDBClient() {
        final Gson gson =
                new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TheMovieDB_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        movieService = retrofit.create(TheMovieDBService.class);
    }

    public static TheMovieDBClient getInstance() {
        if (instance == null) {
            instance = new TheMovieDBClient();
        }
        return instance;
    }

    public Single<List<TheMovieDBMovie>> getSelectedMovies(@NonNull String movieType) {
        return movieService.getMovies(movieType).map(TheMovieDbPage::getResults);
    }

    public Single<List<TheMovieDBTrailer>> getMovieTrailers(int movieId) {
        return movieService.getTrailers(movieId).map(TheMovieDBTrailers::getResults);
    }

    public Single<List<TheMovieDBReview>> getMovieReviews(int movieId) {
        return movieService.getReviews(movieId).map(TheMovieDBReviews::getResults);
    }

}
