package com.ortto.demo

import android.content.Context
import com.google.firebase.FirebaseApp

object DemoConfig {
    val appKey: String get() = BuildConfig.ORTTO_APP_KEY.trim()
    val endpoint: String get() = BuildConfig.ORTTO_API_ENDPOINT.trim().ensureTrailingSlash()
    val captureDataSourceKey: String get() = BuildConfig.ORTTO_CAPTURE_DATA_SOURCE_KEY.trim()
    val captureJsUrl: String get() = BuildConfig.ORTTO_CAPTURE_JS_URL.trim()
    val deepLinkScheme: String get() = BuildConfig.DEEP_LINK_SCHEME

    val hasOrttoConfig: Boolean
        get() = appKey.isConfigured() && endpoint.isConfigured()

    val hasCaptureConfig: Boolean
        get() = captureDataSourceKey.isConfigured() && captureJsUrl.isConfigured() && endpoint.isConfigured()

    fun hasFirebaseConfig(context: Context): Boolean =
        BuildConfig.HAS_GOOGLE_SERVICES && FirebaseApp.getApps(context).isNotEmpty()

    private fun String.isConfigured(): Boolean {
        if (isBlank()) return false
        val normalized = lowercase()
        return listOf("replace-with", "your-", "paste_").none(normalized::contains)
    }

    private fun String.ensureTrailingSlash(): String =
        if (isBlank() || endsWith('/')) this else "$this/"
}
