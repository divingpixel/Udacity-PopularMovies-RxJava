package com.divingpixel.popularmovies.internet;

import com.divingpixel.popularmovies.PopularMovies;
import com.divingpixel.popularmovies.database.MyMovieEntry;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface TheMovieDBDetailsService {

    @GET("3/movie/{id}/{type}?api_key="+ PopularMovies.API_KEY)

    Observable<List<MyMovieEntry>> getDetails(@Path("id") String movieId, @Path("type") String detailType);
}

