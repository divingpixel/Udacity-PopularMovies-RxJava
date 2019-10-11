package com.divingpixel.popularmovies.internet;

import com.divingpixel.popularmovies.MovieReview;
import com.divingpixel.popularmovies.MovieTrailer;
import com.divingpixel.popularmovies.PopularMovies;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;
import io.reactivex.Single;

public interface TheMovieDBService {

    @GET("3/movie/{type}?api_key="+ PopularMovies.API_KEY)
    Single<TheMovieDbPage> getMovies(@Path("type") String movieType);

    @GET("3/movie/{id}/videos?api_key="+ PopularMovies.API_KEY)
    Single<List<MovieTrailer>> getTrailers(@Path("id") int movieId);

    @GET("3/movie/{id}/reviews?api_key="+ PopularMovies.API_KEY)
    Single<List<MovieReview>> getReviews(@Path("id") int movieId);

}

