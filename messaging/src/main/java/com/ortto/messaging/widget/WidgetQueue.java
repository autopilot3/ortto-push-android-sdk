package com.ortto.messaging.widget;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

public class WidgetQueue {
    static final String ORTTO_WIDGET_QUEUE = "ortto_widget_queue";
    static final String tag = "OrttoCapture";

    private final Context context;
    private final GsonBuilder gsonBuilder;
    private Collection<QueuedWidget> cache = null;

    public WidgetQueue(Context context) {
        this.context = context;
        this.gsonBuilder = new GsonBuilder();
    }

    public void queue(String id, Map<String, String> metadata) {
        Collection<QueuedWidget> queue = getCollection();

        if (queue.stream().anyMatch(item -> item.id.equalsIgnoreCase(id))) {
            return;
        }

        QueuedWidget item = new QueuedWidget(id, Optional.ofNullable(metadata));

        queue.add(item);

        saveToSharedPreferences(queue);
    }

    public boolean isEmpty() {
        return getCollection().isEmpty();
    }

    public Optional<QueuedWidget> dequeue() {
        Stack<QueuedWidget> stack = new Stack<>();
        stack.addAll(getCollection());

        QueuedWidget item = stack.pop();
        saveToSharedPreferences(stack);

        return Optional.ofNullable(item);
    }

    public void remove(String id) {
        Collection<QueuedWidget> filtered = getCollection().stream().filter(item -> !item.id.equalsIgnoreCase(id)).collect(Collectors.toList());

        if (filtered.size() != getCollection().size()) {
            saveToSharedPreferences(filtered);
        }
    }

    private Collection<QueuedWidget> getCollection() {
        if (cache == null) {
            cache = loadFromSharedPreferences();
        }

        return cache;
    }

    public Collection<QueuedWidget> loadFromSharedPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);

        Gson gson = new GsonBuilder().create();
        String jsonStr = preferences.getString(ORTTO_WIDGET_QUEUE, "[]");

        QueuedWidget[] queue = gson.fromJson(jsonStr, QueuedWidget[].class);

        List<QueuedWidget> list = new ArrayList<>(queue.length);
        Collections.addAll(list, queue);

        return list;
    }

    private void saveToSharedPreferences(Collection<QueuedWidget> items) {
        Gson gson = gsonBuilder.create();
        String queueJson = gson.toJson(items);

        SharedPreferences preferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(ORTTO_WIDGET_QUEUE, queueJson);
        editor.commit();

        // update cache
        cache = items;
    }
}
