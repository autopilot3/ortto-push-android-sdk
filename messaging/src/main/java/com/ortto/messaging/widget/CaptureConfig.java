package com.ortto.messaging.widget;

import java.net.MalformedURLException;
import java.net.URL;

public class CaptureConfig {
    private String dataSourceKey;
    private String captureJsUrl;
    private String apiHost;

    public CaptureConfig(String dataSourceKey, String captureJsUrl, String apiHost) {
        this.dataSourceKey = dataSourceKey;
        this.captureJsUrl = captureJsUrl;
        this.apiHost = apiHost;
    }

    public String getDataSourceKey() {
        return dataSourceKey;
    }

    public String getCaptureJsUrl() {
        return captureJsUrl;
    }

    public String getApiHost() {
        if (apiHost.endsWith("/")) {
            return apiHost;
        }

        return apiHost + "/";
    }
}
