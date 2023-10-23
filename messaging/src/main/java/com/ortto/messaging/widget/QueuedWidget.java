package com.ortto.messaging.widget;

import java.util.Map;
import java.util.Optional;

public class QueuedWidget {
    public String id;
    public Optional<Map<String, String>> metadata;

    public QueuedWidget(String id, Optional<Map<String, String>> metadata) {
        this.id = id;
        this.metadata = metadata;
    }
}
