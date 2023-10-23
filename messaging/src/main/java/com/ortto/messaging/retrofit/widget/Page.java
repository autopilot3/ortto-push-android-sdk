// com.ortto.messaging.retrofit.widget package
package com.ortto.messaging.retrofit.widget;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;

class Page {
    public String selection;
    @JsonAdapter(FilterDeserializer.class)
    @SerializedName("filter")
    public List<Filter> filters;
    public String device;
    public List<String> platforms;
}
