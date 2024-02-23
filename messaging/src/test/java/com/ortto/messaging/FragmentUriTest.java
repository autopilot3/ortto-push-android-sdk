package com.ortto.messaging;

import android.net.Uri;

import org.junit.Test;

import java.util.Optional;

import android.net.Uri;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class FragmentUriTest {
    @Test
    public void testGetWidgetIdFromFragment_ValidUri() {
        Uri validUri = Uri.parse("main://example.com/page#widget_id=123abc");
        Optional<String> widgetId = DeepLinkHandler.getWidgetIdFromFragment(validUri);
        assertTrue(widgetId.isPresent());
        assertEquals("123abc", widgetId.get());
    }

    @Test
    public void testGetWidgetIdFromFragment_InvalidUri() {
        Uri invalidUri = Uri.parse("test://example.com/page#some_other_param=value");
        Optional<String> widgetId = DeepLinkHandler.getWidgetIdFromFragment(invalidUri);
        assertFalse(widgetId.isPresent());
    }

    @Test
    public void testGetWidgetIdFromFragment_NoFragment() {
        Uri noFragmentUri = Uri.parse("test://example.com/page");
        Optional<String> widgetId = DeepLinkHandler.getWidgetIdFromFragment(noFragmentUri);
        assertFalse(widgetId.isPresent());
    }
}
