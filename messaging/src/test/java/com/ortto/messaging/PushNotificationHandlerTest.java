package com.ortto.messaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.google.firebase.messaging.RemoteMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 32)
public class PushNotificationHandlerTest {
    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        Ortto.INSTANCE = new Ortto();
    }

    @Test
    public void legacyHandleMessageRequestsVisibleNotification() {
        RecordingHandler handler = new RecordingHandler(message(true));

        assertTrue(handler.handleMessage(context));
        assertTrue(handler.displayNotification);
    }

    @Test
    public void displayDisabledStillHandlesAndTracksOrttoMessage() {
        RecordingHandler handler = new RecordingHandler(message(true));

        assertTrue(handler.handleMessage(context, false));
        assertFalse(handler.displayNotification);
        assertTrue(handler.trackedDelivery);
    }

    @Test
    public void displayDisabledRejectsNonOrttoMessage() {
        RecordingHandler handler = new RecordingHandler(message(false));

        assertFalse(handler.handleMessage(context, false));
        assertFalse(handler.trackedDelivery);
    }

    private static RemoteMessage message(boolean ortto) {
        Map<String, String> data = new HashMap<>();
        if (ortto) {
            data.put(PushNotificationHandler.KEY_NOTIFICATION, "notification-id");
            data.put(PushNotificationHandler.KEY_TRACKING_URL, "https://example.test/delivered");
        }
        return new RemoteMessage.Builder("test").setData(data).build();
    }

    private static final class RecordingHandler extends PushNotificationHandler {
        private boolean displayNotification;
        private boolean trackedDelivery;

        RecordingHandler(RemoteMessage remoteMessage) {
            super(remoteMessage);
        }

        @Override
        public boolean handleMessage(Context context, boolean displayNotification) {
            this.displayNotification = displayNotification;
            if (displayNotification) {
                return true;
            }
            return super.handleMessage(context, displayNotification);
        }

        @Override
        protected void trackNotificationDelivery(String url) {
            trackedDelivery = true;
        }
    }
}
