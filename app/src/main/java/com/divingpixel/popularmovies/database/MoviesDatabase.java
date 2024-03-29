package com.divingpixel.popularmovies.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.divingpixel.popularmovies.datamodel.MyMovieEntry;

@Database(entities = {MyMovieEntry.class}, version = 1, exportSchema = false)
@TypeConverters(BooleanConverter.class)
public abstract class MoviesDatabase extends RoomDatabase {

    private static final String LOG_TAG = MoviesDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "popularmovies";
    private static final Object LOCK = new Object();
    private static MoviesDatabase dbInstance;

    public static MoviesDatabase getInstance(Context context) {
        if (dbInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                dbInstance = Room.databaseBuilder(context.getApplicationContext(), MoviesDatabase.class, MoviesDatabase.DATABASE_NAME).build();
            }
        }
        Log.d(LOG_TAG, "Getting database instance");
        return dbInstance;
    }

    public abstract MyMovieDAO myMovieDAO();
}
