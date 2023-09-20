package com.ortto.messaging;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
public class ActionItemTest {
    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    @Test
    public void testSerialization() {
        ActionItem actionItem = new ActionItem();
        actionItem.action = "click";
        actionItem.title = "Open";
        actionItem.link = "https://example.com";

        String json = gson.toJson(actionItem);
        assertEquals("{\"action\":\"click\",\"title\":\"Open\",\"link\":\"https://example.com\"}", json);
    }

    @Test
    public void testDeserialization() {
        String json = "{\"action\":\"click\",\"title\":\"Open\",\"link\":\"https://example.com\"}";
        ActionItem actionItem = gson.fromJson(json, ActionItem.class);

        assertEquals("click", actionItem.action);
        assertEquals("Open", actionItem.title);
        assertEquals("https://example.com", actionItem.link);
    }
}
