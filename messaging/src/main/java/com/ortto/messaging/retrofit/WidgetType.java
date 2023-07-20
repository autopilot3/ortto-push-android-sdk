package com.ortto.messaging.retrofit;

import com.google.gson.annotations.SerializedName;

public enum WidgetType {
    @SerializedName("talk")
    TALK,
    @SerializedName("form")
    FORM,
    @SerializedName("popup")
    POPUP,
    @SerializedName("bar")
    BAR,
    @SerializedName("notification")
    NOTIFICATION,
}
