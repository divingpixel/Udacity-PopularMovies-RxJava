package com.divingpixel.popularmovies;

import android.arch.persistence.room.Update;

import java.util.Calendar;

public class Utils {

    public static final String CATEGORY_FAVORITES = "favorites";
    public static final String CATEGORY_TOP_RATED = "top_rated";
    public static final String CATEGORY_POPULAR = "popular";

    public static String addZeroes(String input, int length) {
        if (input != null) {
            StringBuilder result = new StringBuilder().append(input);
            while (result.length() < length) {
                result.insert(0, "0");
            }
            return result.toString();
        } else return "0";
    }

    public static String addZeroes(int input, int length) {
        StringBuilder result = new StringBuilder().append(input);
        while (result.length() < length) {
            result.insert(0, "0");
        }
        return result.toString();
    }

    public static String makeTimeStamp() {
        Calendar upDate = Calendar.getInstance();
        return Utils.addZeroes(upDate.get(Calendar.YEAR), 4) + "-"
                + Utils.addZeroes(upDate.get(Calendar.MONTH), 2) + "-"
                + Utils.addZeroes(upDate.get(Calendar.DAY_OF_MONTH), 2)+"-"
                + Utils.addZeroes(upDate.get(Calendar.HOUR_OF_DAY),2);
    }

}
