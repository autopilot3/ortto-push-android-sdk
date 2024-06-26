package com.ortto.messaging;

import static junit.framework.TestCase.assertEquals;

import android.net.Uri;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.Optional;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class DeepLinkHandlerTest {
    @Test
    public void deepLink_canLoadWidgetIdFromFragment() {
        Uri deepLink = Uri.parse("deep://some-domain?utm_campaign=abc123&tracking_url=xxxxx#widget_id=63bf88a934cb");

        Optional<String> actual = DeepLinkHandler.getWidgetIdFromFragment(deepLink);

        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals("63bf88a934cb", actual.get());
    }
}
