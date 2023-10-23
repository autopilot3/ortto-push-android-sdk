package com.ortto.messaging;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receive FCM push notification tap intents
 */
public class IntentPushReceiver extends BroadcastReceiver {

    /**
     * Receive a deep link notification intent
     * @param context Application context
     * @param intent  Push notification intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Return early if context or intent is empty
        if (context == null || intent == null) {
            return;
        }

        int messageID = intent.getIntExtra(PushNotificationHandler.MESSAGE_ID, 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(messageID);

        DeepLinkHandler deepLinkHandler = new DeepLinkHandler(context);
        deepLinkHandler.handleIntent(intent);
    }
}