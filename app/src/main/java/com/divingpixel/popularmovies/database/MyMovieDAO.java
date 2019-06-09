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

    @Query("SELECT * from movies WHERE mUpDate= :upDate AND mCategory= :category ORDER BY mIndex")
    LiveData<List<MyMovieEntry>> loadCurrentMovies(String upDate, String category);

    @Insert
    void insertMovie(MyMovieEntry movieEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateMovie(MyMovieEntry movieEntry);

    @Delete
    void deleteMovie(MyMovieEntry movieEntry);

    @Query("DELETE from movies WHERE mFavorite='false'")
    void deleteNonFavorites();

    @Query("SELECT * from movies WHERE mId= :id")
    LiveData<MyMovieEntry> loadMovieById(int id);
}
