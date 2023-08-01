package com.ortto.messaging.widget;

import android.util.Log;

import org.json.JSONObject;

public interface WebViewMessageHandler {
    default void onMessageReceived(JSONObject message) {
    }

    default void onWidgetClosed(String id) {
    }

    default void onTrack(JSONObject options) {
        Log.d("WebViewMessageHandler", options.toString());
    }

    default void onWidgetShown(String id) {
    }

    default void onWidgetDismissed(String id) {
    }

    default void onWidgetFormSubmitted(String id) {
    }
}
