package com.ortto.messaging.widget;

public class WidgetNotFoundException extends Exception {
    WidgetNotFoundException(String id) {
        super("Widget not found with ID: " + id);
    }
}
