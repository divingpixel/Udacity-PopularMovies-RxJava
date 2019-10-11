package com.divingpixel.popularmovies.internet;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TheMovieDBService {

    String TheMovieDB_BASE_URL = "https://api.themoviedb.org/";
    String API_KEY = "32a2be514060aa29a632774e0649f353";
    String POSTER_PATH = "https://image.tmdb.org/t/p/";
    String POSTER_SMALL = "w185/";
    String POSTER_BIG = "w500/";

    @GET("3/movie/{type}?api_key="+ API_KEY)
    Single<TheMovieDbPage> getMovies(@Path("type") String movieType);

    @GET("3/movie/{id}/videos?api_key="+ API_KEY)
    Single<TheMovieDBTrailers> getTrailers(@Path("id") int movieId);

    @GET("3/movie/{id}/reviews?api_key="+ API_KEY)
    Single<TheMovieDBReviews> getReviews(@Path("id") int movieId);

}

