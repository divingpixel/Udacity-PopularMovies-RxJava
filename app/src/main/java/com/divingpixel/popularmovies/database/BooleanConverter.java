package com.divingpixel.popularmovies.database;

import androidx.room.TypeConverter;

public class BooleanConverter {
    @TypeConverter
    public static boolean toBoolean(String value) {
        return value.equalsIgnoreCase("true");
    }

    @TypeConverter
    public static String toString(boolean value) {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }
}
