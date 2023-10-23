package com.ortto.messaging.retrofit;

import com.google.gson.annotations.SerializedName;

public class WidgetsGetRequest {
    @SerializedName("s")
    public String sessionId;
    @SerializedName("c")
    public String contactId;
    @SerializedName("e")
    public String emailAddress;
    @SerializedName("p")
    public String phoneNumber;
    @SerializedName("h")
    public String applicationKey;
    @SerializedName("tk")
    public boolean talkEnabled = false;
    @SerializedName("tt")
    public String talkToken;
    @SerializedName("u")
    public String url;
    @SerializedName("ottlk")
    public String ottlk = "";
}
