package com.ortto.messaging.retrofit;

import com.google.gson.annotations.SerializedName;
import com.ortto.messaging.identity.UserID;

public class TokenRegistration {
    public TokenRegistration(
        String dataSourceInstanceIDHash,
        String session,
        String deviceToken,
        Boolean permission
    ) {
        this.dataSourceInstanceIDHash = dataSourceInstanceIDHash;
        this.session = session;
        this.permission = permission;
        this.deviceToken = deviceToken;
    }

    @SerializedName("appk")
    public String dataSourceInstanceIDHash;
    @SerializedName("s")
    public String session;
    @SerializedName("pm")
    public Boolean permission = true;
    @SerializedName("pl")
    public String platform = "android";
    @SerializedName("ptk")
    public String deviceToken;
    @SerializedName("ptkt")
    public String pushTokenType = "fcm";
}
