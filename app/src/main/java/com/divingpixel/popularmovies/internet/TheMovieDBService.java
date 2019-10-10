package com.divingpixel.popularmovies.internet;

import com.divingpixel.popularmovies.PopularMovies;
import com.divingpixel.popularmovies.database.MyMovieEntry;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface TheMovieDBService {

    @GET("3/movie/{type}?api_key="+ PopularMovies.API_KEY)

    Observable<List<MyMovieEntry>> getMovies(@Path("type") String movieType);
}

