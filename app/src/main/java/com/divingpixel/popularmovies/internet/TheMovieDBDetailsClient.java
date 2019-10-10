package com.divingpixel.popularmovies.internet;
import androidx.annotation.NonNull;

import com.divingpixel.popularmovies.PopularMovies;
import com.divingpixel.popularmovies.database.MyMovieEntry;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

public class TheMovieDBDetailsClient {

    private static TheMovieDBDetailsClient instance;
    private TheMovieDBDetailsService movieDetailsService;

    private TheMovieDBDetailsClient() {
        final Gson gson =
                new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor())
                .build();
        final Retrofit retrofit = new Retrofit.Builder().baseUrl(PopularMovies.TheMovieDB_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        movieDetailsService = retrofit.create(TheMovieDBDetailsService.class);
    }

    public static TheMovieDBDetailsClient getInstance() {
        if (instance == null) {
            instance = new TheMovieDBDetailsClient();
        }
        return instance;
    }

    public Observable<List<MyMovieEntry>> getMovieDetails(@NonNull String movieId, @NonNull String detailsType) {
        return movieDetailsService.getDetails(movieId, detailsType);
    }
}
