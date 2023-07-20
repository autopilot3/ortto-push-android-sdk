package com.ortto.messaging.widget;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class OrttoCapture {
    private OrttoWebView webView;
    Activity activity;
    private String tag = "OrttoCapture";
    private CaptureConfig config;
    private Timer _timer;
    private WidgetQueue widgetQueue;

    public OrttoCapture(CaptureConfig config, Application application) {
        this.config = config;
        this.widgetQueue = new WidgetQueue(application);
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
                widget.ifPresent(queuedWidget -> showWidget(queuedWidget.id));
            }
        }, 3000);
    }

    public void showWidget(String id) {
        widgetQueue.remove(id);
        webView.showWidget(id);
    }
}

