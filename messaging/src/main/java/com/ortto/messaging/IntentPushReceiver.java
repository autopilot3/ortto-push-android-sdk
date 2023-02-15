package com.ortto.messaging;

import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

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

        PushNotificationPayload payload = intent.getExtras().getParcelable("payload");

        if (payload == null) {
            return;
        }

        handleDeepLink(context, payload);
    }

    /**
     * Handle a deep link action click
     *
     * @param context Application context
     * @param payload Push notification payload
     */
    public static void handleDeepLink(Context context, PushNotificationPayload payload) {

        // Return early if theres no link included
        String link = payload.deepLink;

        if (link == null || link.isEmpty()) {
            return;
        }

        if (Ortto.instance().hasCustomDeeplinkCallback()) {
            return;
        }

        // If intent could not be found
        Intent intent = findDeeplinkApps(context, Uri.parse(link));
        if (intent == null) {
            return;
        }

        try {
            context.startActivity(intent);
        } catch (RuntimeException e) {
            Ortto.log().warning("IntentPushReceiver@handleDeeplink.fail: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public static Intent findDeeplinkApps(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(context.getPackageName());

        Ortto.log().info("IntentPushReceiver@findDeeplinkApps uri="+uri.toString());

        int flags = Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP;

        ComponentName name = intent.resolveActivity(context.getPackageManager());

        intent.setFlags(flags);
        intent.setClassName(context.getPackageName(), name.getClassName());

        if (name != null) {
            return intent;
        }

        return null;
    }
}