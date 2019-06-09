package com.divingpixel.popularmovies;

import java.util.Calendar;

public class Utils {

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

    public static String makeDate() {
        Calendar upDate = Calendar.getInstance();
        return Utils.addZeroes(upDate.get(Calendar.YEAR), 4) + "-"
                + Utils.addZeroes(upDate.get(Calendar.MONTH), 2) + "-"
                + Utils.addZeroes(upDate.get(Calendar.DAY_OF_MONTH), 2);
    }

}
