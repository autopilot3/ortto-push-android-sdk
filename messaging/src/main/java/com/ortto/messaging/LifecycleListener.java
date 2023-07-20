package com.ortto.messaging;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ortto.messaging.widget.OrttoCapture;

import java.util.Optional;

public class LifecycleListener implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        Ortto.instance().getCapture().ifPresent(capture -> capture.setActivity(activity));

        Bundle extras = activity.getIntent().getExtras();

        if (extras == null || extras.isEmpty()) {
            return;
        }

        int messageID = extras.getInt(PushNotificationHandler.MESSAGE_ID, 0);
        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(messageID);

        DeepLinkHandler deepLinkHandler = new DeepLinkHandler(activity);
        deepLinkHandler.handleIntent(activity.getIntent());
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Ortto.instance().getCapture().ifPresent(OrttoCapture::processNextWidgetFromQueue);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
