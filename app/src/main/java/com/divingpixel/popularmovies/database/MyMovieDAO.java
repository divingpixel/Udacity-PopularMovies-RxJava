package com.divingpixel.popularmovies.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface MyMovieDAO {

    @Query("SELECT * from movies WHERE mFavorite='true' ORDER BY mDate")
    LiveData<List<MyMovieEntry>> loadFavoriteMovies();

    @Query("SELECT * from movies WHERE mPopIndex>0 ORDER BY mPopIndex")
    LiveData<List<MyMovieEntry>> loadPopularMovies ();

    @Query("SELECT * from movies WHERE mTopIndex>0 ORDER BY mTopIndex")
    LiveData<List<MyMovieEntry>> loadTopRatedMovies();

    @Insert
    void insertMovie(MyMovieEntry movieEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(MyMovieEntry movieEntry);

    @Delete
    void deleteMovie(MyMovieEntry movieEntry);

    @Query("DELETE from movies WHERE mPopIndex>0 AND mFavorite='false'")
    void deletePopularMovies();

    @Query("DELETE from movies WHERE mTopIndex>0 AND mFavorite='false'")
    void deleteTopRatedMovies();

    @Query("SELECT * from movies WHERE mId= :id")
    LiveData<MyMovieEntry> loadMovieById(int id);
}
