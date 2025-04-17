package com.ortto.messaging.widget;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ortto.messaging.Ortto;

import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class OrttoCapture implements AutoCloseable {
    private OrttoWebView webView;
    protected Activity activity;
    private String tag = "OrttoCapture";
    private CaptureConfig config;
    private Timer _timer;
    private WidgetQueue widgetQueue;
    protected ConnectivityManager.NetworkCallback networkCallback;
    protected ConnectivityManager connectivityManager;

    public OrttoCapture(CaptureConfig config, Application application) {
        this.config = config;
        this.widgetQueue = new WidgetQueue(application);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                processNextWidgetFromQueue();
            }

            @Override
            public void onLost(@NonNull Network network) {
                if (_timer != null) {
                    _timer.cancel();
                }
            }
        };

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        this.webView = new OrttoWebView(activity, config);
    }

    void onWidgetClosed(String id) {
        Log.d(tag, "Widget dismissed: " + id);
        processNextWidgetFromQueue();
    }

    public void queueWidget(String id, Map<String, String> metadata) {
        widgetQueue.queue(id, metadata);
    }

    public void queueWidget(String id) {
        queueWidget(id, null);
    }

    public void processNextWidgetFromQueue() {
        if (widgetQueue.isEmpty()) {
            return;
        }

        if (_timer != null) {
            _timer.cancel();
        }

        _timer = new Timer();
        _timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Optional<QueuedWidget> widget = widgetQueue.dequeue();
                widget.ifPresent(queuedWidget -> showWidget(queuedWidget.id, null));
            }
        }, 3000);
    }

    public void showWidget(String id, Ortto.WidgetCallback callback) {
        widgetQueue.remove(id);
        webView.showWidget(id, callback);
    }

    @Override
    public void close() throws Exception {
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
}

