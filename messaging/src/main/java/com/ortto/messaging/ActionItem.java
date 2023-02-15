package com.ortto.messaging;

import com.google.gson.annotations.SerializedName;

/**
 * Struct which represents a single action item to present on a notification
 */
public class ActionItem {
    @SerializedName("action")
    public String action;
    @SerializedName("title")
    public String title;
    @SerializedName("link")
    public String link;
}
