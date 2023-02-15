package com.ortto.messaging.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegistrationResponse {
    @SerializedName("session_id")
    @Expose
    public String sessionId;
}
