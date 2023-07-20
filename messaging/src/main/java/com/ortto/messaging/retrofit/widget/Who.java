// com.ortto.messaging.retrofit.widget package
package com.ortto.messaging.retrofit.widget;

import com.google.gson.annotations.JsonAdapter;

import java.util.List;

class Who {
    public String selection;
    @JsonAdapter(FilterDeserializer.class)
    public List<Filter> filter;
}
