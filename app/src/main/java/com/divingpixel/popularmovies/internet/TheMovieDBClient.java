package com.divingpixel.popularmovies.internet;

import androidx.annotation.NonNull;

import com.divingpixel.popularmovies.MovieReview;
import com.divingpixel.popularmovies.MovieTrailer;
import com.divingpixel.popularmovies.PopularMovies;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.reactivex.Single;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


public class TheMovieDBClient {

    private static TheMovieDBClient instance;
    private TheMovieDBService movieService;

    private TheMovieDBClient() {
        final Gson gson =
                new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PopularMovies.TheMovieDB_BASE_URL)
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

    public Single<List<MovieTrailer>> getMovieTrailers(@NonNull int movieId) {
        return movieService.getTrailers(movieId);
    }

    public Single<List<MovieReview>> getMovieReviews(@NonNull int movieId) {
        return movieService.getReviews(movieId);
    }


}
