package com.ortto.demo

import android.app.Application
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.ortto.messaging.CustomDeeplinkCallback
import com.ortto.messaging.Ortto
import com.ortto.messaging.OrttoConfig
import com.ortto.messaging.PushNotificationPayload
import com.ortto.messaging.widget.CaptureConfig

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (!DemoConfig.hasOrttoConfig) {
            DemoLog.demo("Ortto configuration missing; copy demo/local.properties.example")
            return
        }

        val config = OrttoConfig(DemoConfig.appKey, DemoConfig.endpoint)
        Ortto.instance().init(config, this, DemoLog.sdkLogger())
        Ortto.instance().ensureChannelIsCreated()
        Ortto.instance().setDeeplinkHandler(DemoDeepLinkCallback)
        DemoLog.demo("Ortto SDK initialized for Android ${com.ortto.messaging.BuildConfig.SDK_VERSION}")

        if (DemoConfig.hasCaptureConfig) {
            Ortto.instance().initCapture(
                CaptureConfig(
                    DemoConfig.captureDataSourceKey,
                    DemoConfig.captureJsUrl,
                    DemoConfig.endpoint,
                ),
            )
            DemoLog.demo("Ortto Capture initialized")
        } else {
            DemoLog.demo("Capture disabled; data-source key or capture JS URL missing")
        }
    }
}

private object DemoDeepLinkCallback : CustomDeeplinkCallback {
    override fun createTaskStackFromPayload(
        context: Context,
        deepLink: String,
        payload: PushNotificationPayload,
        actionIndex: Int,
    ): TaskStackBuilder {
        val intent = Intent(Intent.ACTION_VIEW, deepLink.toUri(), context, MainActivity::class.java).apply {
            putExtra("payload", payload)
            putExtra("link", deepLink)
            putExtra("action_index", actionIndex)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return TaskStackBuilder.create(context).addNextIntentWithParentStack(intent)
    }
}
