package com.divingpixel.popularmovies.internet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectionStatus {

    private static final String LOG_TAG = ConnectionStatus.class.getSimpleName();

    public static boolean isConnected(final Context context) {
        boolean status = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.i(LOG_TAG, "INTERNET CONNECTED TROUGH WIFI");
                status = true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                Log.i(LOG_TAG, "INTERNET CONNECTED TROUGH MOBILE DATA");
                status = true;
            }
        } else {
            Log.i(LOG_TAG, "NO INTERNET CONNECTION");
            status = false;
        }
        return status;
    }
}
