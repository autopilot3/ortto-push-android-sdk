package com.ortto.messaging.widget;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OrttoLifecycleCallbackHandler implements Application.ActivityLifecycleCallbacks {
    private OrttoCapture capture;

    public OrttoLifecycleCallbackHandler(OrttoCapture capture) {
        this.capture = capture;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        capture.processNextWidgetFromQueue();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

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
