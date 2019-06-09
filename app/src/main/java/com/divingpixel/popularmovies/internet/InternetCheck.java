package com.divingpixel.popularmovies.internet;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;

public class InternetCheck extends ConnectivityManager.NetworkCallback {

    private final NetworkRequest networkRequest;
    private final ConnectivityManager connectivityManager;
    private ConnectionChangeListener netListener;

    public boolean hasConnection() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return (activeNetwork != null) && activeNetwork.isConnected();
    }

    public InternetCheck(Application application, Context context) {
        connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        if (context instanceof ConnectionChangeListener) {
            netListener = (ConnectionChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConnectionChangeListener");
        }
        enable();
    }

    public void enable() {
        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    public void disable() {
        connectivityManager.unregisterNetworkCallback(this);
    }

    @Override
    public void onAvailable(Network network) {
        netListener.onConnectionChange(true);
    }

    @Override
    public void onUnavailable() {
        netListener.onConnectionChange(false);
    }

    public interface ConnectionChangeListener {
        void onConnectionChange(boolean status);
    }
}
