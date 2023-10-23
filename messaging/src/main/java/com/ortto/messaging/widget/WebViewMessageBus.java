package com.ortto.messaging.widget;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.ortto.messaging.Ortto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Level;

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
                switch (obj.getString("type")) {
                    case "widget-close":
                        handler.onWidgetClosed(obj.getString("id"));
                        break;
                    case "ap3c-track":
                        handler.onTrack(obj);

                        JSONObject payload = obj.getJSONObject("payload");
                        if (payload.has("cw")) {
                            JSONObject cw = payload.getJSONObject("cw");
                            String widgetId = cw.getString("widget_id");

                            switch (cw.getString("type")) {
                                case WidgetEventType.SHOWN:
                                    handler.onWidgetShown(widgetId);
                                    break;
                                case WidgetEventType.DISMISSED:
                                    handler.onWidgetDismissed(widgetId);
                                    break;
                                case WidgetEventType.FORM_SUBMITTED:
                                    handler.onWidgetFormSubmitted(widgetId);
                                    break;
                            }
                        }
                        break;
                    case "unhandled-error":
                        String errorMessage = obj.getString("message");
                        int lineNumber = obj.getInt("lineno");
                        int columnNumber = obj.getInt("colno");
                        String errorObj = obj.getString("error");

                        Log.d("OrttoWebView", "Unhandled web view error: \n" +
                                "message: " + errorMessage + "\n" +
                                "line: " + lineNumber + "\n" +
                                "column: " + columnNumber + "\n" +
                                "error: " + errorObj);
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

final class WidgetEventType {
    final static String SHOWN = "widget_shown";
    final static String DISMISSED = "widget_dismissed";
    final static String FORM_SUBMITTED = "widget_form_submitted";
}
