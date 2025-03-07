package com.ortto.messaging;

import android.Manifest;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ortto.messaging.retrofit.NotificationDeliveryResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Class which processes remote push notifications. It is configured to use data keys so messages
 * are able to be dlievered in the background
 */
public class PushNotificationHandler {

    public final static String KEY_DEEPLINK  = "link";
    public final static String KEY_IMAGE  = "image";
    public final static String KEY_NOTIFICATION  = "ortto_notification_id";
    public final static String KEY_TRACKING_URL  = "event_tracking_url";
    public final static String KEY_ACTIONS_LIST = "actions";
    public final static String KEY_COLOR  = "color";

    private static final String TAG = "ortto";

    public static String MESSAGE_ID = "message_id";
    public static String PUSH_ACTION = "com.ortto.messaging.PUSH_ACTION";

    private static final String FCM_COLOR = "com.google.firebase.messaging.default_notification_color";

    protected RemoteMessage remoteMessage;

    public PushNotificationHandler(RemoteMessage remoteMessage) {
        this.remoteMessage = remoteMessage;
    }

    /**
     * Process this objects remoteMessage
     * @param context Application
     * @return If Ortto SDK has processed the notification
     */
    public boolean handleMessage(Context context) {
        Ortto.log().info("PushNotificationHandler@handleMessage."+remoteMessage.getMessageId());

        Map<String, String> data = remoteMessage.getData();
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        // Ignore non-ortto originated messages
        if (!data.containsKey(KEY_NOTIFICATION)) {
            return false;
        }

        if (data.containsKey(KEY_TRACKING_URL)) {
            trackNotificationDelivery(data.get(KEY_TRACKING_URL));
        }

        // Extract out notification action list
        List<ActionItem> actionItemList = new ArrayList<ActionItem>();
        ActionItem primaryAction = null;

        try {
            Ortto.log().info("items: "+data.get(KEY_ACTIONS_LIST));
            actionItemList = new Gson().fromJson(data.get(KEY_ACTIONS_LIST), new TypeToken<List<ActionItem>>(){}.getType());
            primaryAction = new Gson().fromJson(data.get("primary_action"), new TypeToken<ActionItem>(){}.getType());
        } catch (JsonSyntaxException e) {
            Ortto.log().info("PushNotificationHandler@parseActionList.fail: ."+e.getLocalizedMessage());
        }

        Bundle bundle = new Bundle();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            bundle.putString(entry.getKey(), entry.getValue());
        }

        String title = notification == null
                ? bundle.getString("title", "Push Notification")
                : notification.getTitle();
        String body = notification == null
            ? bundle.getString("body", "")
            : notification.getBody();
        String notificationId = data.get(KEY_NOTIFICATION);

        // Set up action
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Request channel permissions
        Ortto.instance().ensureChannelIsCreated();

        // Use default sound
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int messageID = (int) (System.currentTimeMillis()/1000);
        bundle.putInt(MESSAGE_ID, messageID);

        PushNotificationPayload payload = new PushNotificationPayload(
                bundle.getString("link"),
                notificationId,
                bundle,
                title,
                body
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, PushNotificationHandler.PUSH_ACTION)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setChannelId(PushNotificationHandler.PUSH_ACTION)
                .setTicker(context.getApplicationInfo().loadLabel(context.getPackageManager()).toString())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        ApplicationInfo applicationInfo;
        Bundle metadata;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            metadata = applicationInfo.metaData;
        } catch (Exception e) {
            e.printStackTrace();
            Ortto.log().warning("PushNotificationHandler@getApplicationInfo.fail: "+e.getLocalizedMessage());

            return false;
        }

        // Set Tint
        try {
            String tintColor = notification == null
                    ? bundle.getString(KEY_COLOR, bundle.getString(FCM_COLOR))
                    : notification.getColor();
            notificationBuilder.setColor(parseTintColour(context, metadata, tintColor));
        } catch (NullPointerException e) {
            Ortto.log().warning("PushNotificationHandler@getTint.fail: "+e.getLocalizedMessage());
        }

        // Set Icon
        String iconValue = notification == null
                ? null
                : notification.getIcon();
        int icon = (iconValue != null)
                ? parseIcon(context, metadata, iconValue)
                : R.drawable.ic_notification;
        notificationBuilder.setSmallIcon(icon);

        // Set Image
        try {
            String image = notification == null || notification.getImageUrl() == null
                    ? bundle.getString(KEY_IMAGE)
                    : notification.getImageUrl().toString();
            addImage(image, notificationBuilder);
        } catch (Exception e) {
            // continue, but there was an error grabbing the image
            Ortto.log().warning("PushNotificationHandler@getImage.fail: "+e.getLocalizedMessage());
        }

        if (actionItemList != null && !actionItemList.isEmpty()) {
            // There are items in the action list
            for(int i = 0; i < actionItemList.size(); i++) {
                ActionItem item = actionItemList.get(i);
                PendingIntent actionIntent = this.createIntent(context, payload, item.link, messageID, i);

                if (actionIntent != null) {
                    NotificationCompat.Action action = new NotificationCompat.Action(0, item.title, actionIntent);
                    notificationBuilder.addAction(action);
                }
            }
        }

        if (primaryAction != null) {
            // primary_action = json of (action, url)
            // Set default intent because the action list is empty
            PendingIntent intent = this.createIntent(context, payload, primaryAction.link, messageID, -1);
            if (intent != null) {
                notificationBuilder.setContentIntent(intent);
            }
        }

        // Send notification
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) { // API 33
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted, proceed with notification
                notificationManager.notify(messageID, notificationBuilder.build());
            }
        } else {
            // For API < 33, no runtime permission is needed
            notificationManager.notify(messageID, notificationBuilder.build());
        }

        return true;
    }

    private void trackNotificationDelivery(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }

        Call<NotificationDeliveryResponse> call = Ortto.instance()
                .client
                .trackNotificationDelivery(url, Ortto.instance().getTrackingQuery());

        Ortto.log().info("PushNotificationHandler@trackNotificationDelivery url="+call.request().url());

        call.enqueue(new Callback<NotificationDeliveryResponse>() {
            @Override
            public void onResponse(@NonNull Call<NotificationDeliveryResponse> call, @NonNull Response<NotificationDeliveryResponse> response) {
                Ortto.log().info("PushNotificationHandler@trackNotificationDelivery.complete code="+response.code());
            }

            @Override
            public void onFailure(Call<NotificationDeliveryResponse> call, Throwable t) {
                Ortto.log().warning("PushNotificationHandler@trackNotificationDelivery.fail code="+t.getMessage());
            }
        });
    }

    private Integer parseIcon(Context context, Bundle metadata, String icon) {
        if (icon != null && !icon.isEmpty()) {
            return context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
        }

        return context.getApplicationInfo().icon;
    }

    /**
     * Parse a color hex code and return its Color type
     * @param context
     * @param metadata
     * @param color
     * @return
     */
    private Integer parseTintColour(Context context, Bundle metadata, String color) {
        if (color != null) {
            try {
                return Color.parseColor(color);
            } catch (NumberFormatException e) {
                // Do nothing, but the API probably sent a strange hex color code.
            }
        }

        int fcmColor = metadata.getInt(FCM_COLOR, -1);
        if (fcmColor > -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return context.getColor(fcmColor);
            }
        }

        String fcmColorString = metadata.getString(FCM_COLOR);
        if (fcmColorString != null) {
            return Color.parseColor(fcmColorString);
        }

        return null;
    }

    /**
     * Parse an image URL and attach the resource to the notification
     * @param imageUrl
     * @param builder
     */
    public void addImage(String imageUrl, NotificationCompat.Builder builder) {
        // Skip if image is empty
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .bigLargeIcon((Bitmap) null)
                .setSummaryText("Summary");

        URL url;

        try {
            url = new URL(imageUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        InputStream stream;

        try {
            stream = url.openStream();
            Bitmap bitmap = BitmapFactory.decodeStream(stream);
            style.bigPicture(bitmap);
            builder.setLargeIcon(bitmap);
            builder.setStyle(style);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    /**
     * Create a PendingIntent class that can be forwarded through the notification drawer
     * @param context     Application Context
     * @param payload     Payload wrapper class
     * @param deeplink    Link to open when the intent is clicked on
     * @param requestCode Notification ID
     * @return PendingIntent
     */
    public PendingIntent createIntent(Context context, PushNotificationPayload payload, String deeplink, int requestCode, int actionIndex) {
        if (deeplink == null || deeplink.isEmpty()) {
            return null;
        }

        int intentFlags = Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP;
        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;

        if (Ortto.instance().hasCustomDeeplinkCallback()) {
            TaskStackBuilder builder = Ortto.instance()
                    .getDeeplinkCallback()
                    .createTaskStackFromPayload(context, deeplink, payload, -1);

            return builder.getPendingIntent(requestCode, flags);
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deeplink));
        intent.setPackage(context.getPackageName());
        intent.putExtra("link", deeplink);
        intent.putExtra("action_index", actionIndex);

        if (context.getApplicationInfo().targetSdkVersion > Build.VERSION_CODES.R) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API 30
                intent.setFlags(intentFlags | Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER);
            } else {
                intent.setFlags(intentFlags);
            }

            intent.putExtra("payload", payload);
            intent.putExtras(payload.extras);

            return TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(intent)
                    .getPendingIntent(requestCode, flags);

        } else {
            ComponentName name = intent.resolveActivity(context.getPackageManager());
            intent.setClassName(context.getPackageName(), name.getClassName());
            intent.putExtra("payload", payload);
            intent.putExtras(payload.extras);

            return PendingIntent.getActivity(context, requestCode, intent, flags);
        }
    }
}
