package com.divingpixel.popularmovies.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;

@Dao
public interface MyMovieDAO {

    @Query("SELECT * from movies WHERE mFavorite='true' ORDER BY mDate")
    Observable<List<MyMovieEntry>> loadFavoriteMovies();

    @Query("SELECT * from movies WHERE mPopIndex>0 ORDER BY mPopIndex")
    Observable<List<MyMovieEntry>> loadPopularMovies();

    @Query("SELECT * from movies WHERE mTopIndex>0 ORDER BY mTopIndex")
    Observable<List<MyMovieEntry>> loadTopRatedMovies();

    @Insert
    void insertMovie(MyMovieEntry movieEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(MyMovieEntry movieEntry);

    @Delete
    void deleteMovie(MyMovieEntry movieEntry);

    @Query("DELETE from movies WHERE mPopIndex>0 AND mTopIndex=0 AND mFavorite='false'")
    void deletePopularMovies();

    @Query("DELETE from movies WHERE mTopIndex>0 AND mPopIndex=0 AND mFavorite='false'")
    void deleteTopRatedMovies();

    @Query("SELECT * from movies WHERE mId= :id")
    Single<MyMovieEntry> getMovieById(int id);
}
