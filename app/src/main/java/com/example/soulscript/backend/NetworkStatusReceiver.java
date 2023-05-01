package com.example.soulscript.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.util.Log;

// This class is used to monitor the status of the network connection.
public class NetworkStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStatusReceiver";
    private ConnectivityManager.NetworkCallback networkCallback;
    private NetworkStatusListener networkStatusListener;

    public NetworkStatusReceiver(ConnectivityManager.NetworkCallback networkCallback) {
        this.networkCallback = networkCallback;
    }

    public NetworkStatusReceiver(NetworkStatusListener networkStatusListener) {
        this.networkStatusListener = networkStatusListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "NETWORK_STATUS".equals(intent.getAction())) {
            boolean isConnected = intent.getBooleanExtra("status", false);
            if (networkStatusListener != null) {
                networkStatusListener.onNetworkStatusChanged(isConnected);
            } else {
                Log.w(TAG, "NetworkStatusListener is null");
            }
        }
    }

    public interface NetworkStatusListener {
        void onNetworkStatusChanged(boolean isConnected);
    }
}
