package com.ortto.messaging.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class LinkUtm {
    @SerializedName("utm_campaign")
    public String campaign;
    @SerializedName("utm_medium")
    public String medium;
    @SerializedName("utm_source")
    public String source;
    @SerializedName("utm_content")
    public String content;

    public static LinkUtm fromUri(Uri uri) {
        LinkUtm utm = new LinkUtm();

        for (String key : uri.getQueryParameterNames()) {
            String value = uri.getQueryParameter(key);

            switch (key) {
                case "utm_campaign":
                    utm.campaign = value;
                    break;
                case "utm_medium":
                    utm.medium = value;
                    break;
                case "utm_source":
                    utm.source = value;
                    break;
                case "utm_content":
                    utm.content = value;
                    break;
            }
        }

        return utm;
    }

    @NonNull
    public String toString() {
        return new Gson().toJson(this);
    }
}
