package com.ortto.messaging;

import android.app.TaskStackBuilder;
import android.content.Context;

/**
 * Interface which an application context must implement to overwrite the deep linking function
 */
public interface CustomDeeplinkCallback {
    TaskStackBuilder createTaskStackFromPayload(Context context, String deepLink, PushNotificationPayload payload, int actionIndex);
}