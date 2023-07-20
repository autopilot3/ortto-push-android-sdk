package com.ortto.messaging.widget;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class WebViewMessageBus {
    Context mContext;
    WebViewMessageHandler handler;

    WebViewMessageBus(Context c) {
        mContext = c;
    }

    public void setHandler(WebViewMessageHandler handler) {
        this.handler = handler;
    }

    @JavascriptInterface
    public void showMessage(String message) {
        try {
            JSONObject obj = new JSONObject(message);

            if (handler == null) {
                return;
            }

            handler.onMessageReceived(obj);

            if (obj.has("type")) {
                String type = obj.getString("type");
                switch (type) {
                    case "widget-close":
                        handler.onWidgetClosed(obj.getString("id"));
                        return;
                    case "ap3c-track":
                        handler.onTrack(obj);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
