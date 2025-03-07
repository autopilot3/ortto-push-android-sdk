package com.ortto.messaging;

public class OrttoConfig
{
    public String appKey;
    public String endpoint;
    public boolean shouldSkipNonExistingContacts = false;

    public boolean debug = false;

    public OrttoConfig(String appKey, String endpoint, Boolean shouldSkipNonExistingContacts) {
        this(appKey, endpoint);
        this.shouldSkipNonExistingContacts = shouldSkipNonExistingContacts;
    }

    /**
     * @deprecated Use {@link OrttoConfig#OrttoConfig(String, String, Boolean)} instead
     */
    @Deprecated
    public OrttoConfig(String appKey, String endpoint, Boolean shouldSkipNonExistingContacts, Boolean allowAnonUsers) {
        this(appKey, endpoint, shouldSkipNonExistingContacts);
        Ortto.log().warning("OrttoConfig: Deprecated constructor used. Use OrttoConfig(String, String, Boolean) instead");
    }

    public OrttoConfig(String appKey, String endpoint) {
        if (appKey != null) {
            this.appKey = appKey;
        }

        if (endpoint != null) {
            this.endpoint = endpoint;
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
