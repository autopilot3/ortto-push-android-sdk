package com.ortto.messaging.retrofit.widget;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

class FilterDeserializer implements JsonDeserializer<List<Filter>> {
    @Override
    public List<Filter> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonArray()) {
            return context.deserialize(json, new TypeToken<List<Filter>>() {
            }.getType());
        } else {
            Filter singleFilter = context.deserialize(json, Filter.class);
            return Collections.singletonList(singleFilter);
        }
    }
}
