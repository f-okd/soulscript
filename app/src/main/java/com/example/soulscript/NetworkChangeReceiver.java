package com.example.soulscript;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.widget.Toast;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private ConnectivityManager.NetworkCallback networkCallback;

    public NetworkChangeReceiver(ConnectivityManager.NetworkCallback networkCallback) {
        this.networkCallback = networkCallback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method will not be called anymore, but the class still needs to extend BroadcastReceiver
    }
}