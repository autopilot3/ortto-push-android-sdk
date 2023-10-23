package com.ortto.messaging.retrofit;

import com.google.gson.annotations.SerializedName;
import com.ortto.messaging.retrofit.widget.Widget;

import java.util.List;

public class WidgetsGetResponse {
    public List<Widget> widgets;
    @SerializedName("has_logo")
    public boolean hasLogo;
    @SerializedName("enabled_gdpr")
    public boolean enabledGdpr;
    @SerializedName("recaptcha_site_key")
    public String recaptchaSiteKey;
    @SerializedName("country_code")
    public String countryCode;
    @SerializedName("service_worker_url")
    public String serviceWorkerUrl;
    @SerializedName("cdn_url")
    public String cdnUrl;
    @SerializedName("session_id")
    public String sessionId;

    /** This is not actually part of the response.
     *  Just used to override expiry. */
    public long expiry;
}