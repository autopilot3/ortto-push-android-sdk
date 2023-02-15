package com.ortto.messaging;

public class OrttoConfig {
    public String appKey;
    public String endpoint;
    public Boolean shouldSkipNonExistingContacts;
    public Boolean allowAnonUsers;

    public OrttoConfig(String appKey, String endpoint, Boolean shouldSkipNonExistingContacts) {
        this(appKey, endpoint, shouldSkipNonExistingContacts, false);
    }

    public OrttoConfig(String appKey, String endpoint, Boolean shouldSkipNonExistingContacts, Boolean allowAnonUsers) {
        if (appKey != null) {
            this.appKey = appKey;
        }

        if (endpoint != null) {
            this.endpoint = endpoint;
        }

        this.shouldSkipNonExistingContacts = shouldSkipNonExistingContacts;
        this.allowAnonUsers = allowAnonUsers;
    }

    public OrttoConfig(String appKey, String endpoint) {
        this(appKey, endpoint, false);
    }
}
