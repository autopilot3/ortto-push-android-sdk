package com.ortto.demo

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.FirebaseApp
import com.ortto.messaging.Ortto
import com.ortto.messaging.data.PushPermission
import com.ortto.messaging.identity.UserID
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class DemoViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences("ortto_push_demo", Context.MODE_PRIVATE)
    private val worker = Executors.newSingleThreadExecutor()

    var email by mutableStateOf(preferences.getString("last_email", "") ?: "")
    var signedInEmail by mutableStateOf(preferences.getString("signed_in_email", "") ?: "")
        private set
    var selectedTab by mutableStateOf(DemoTab.Home)
        private set
    var sessionId by mutableStateOf<String?>(null)
        private set
    var fcmToken by mutableStateOf("")
        private set
    var tokenOverride by mutableStateOf("")
    var permissionStatus by mutableStateOf("unknown")
        private set
    var registrationStatus by mutableStateOf("Not registered")
        private set
    var trackedDeepLink by mutableStateOf("")
    var widgetId by mutableStateOf("")
    var widgets by mutableStateOf<List<DemoWidget>>(emptyList())
        private set
    var isIdentifying by mutableStateOf(false)
        private set
    var isRegistering by mutableStateOf(false)
        private set
    var isLoggingOut by mutableStateOf(false)
        private set
    var isLoadingWidgets by mutableStateOf(false)
        private set
    var isTrackingLink by mutableStateOf(false)
        private set
    var showTechnicalDetails by mutableStateOf(false)
    var pendingConfirmationLink by mutableStateOf<String?>(null)
        private set
    var toastMessage by mutableStateOf<String?>(null)
        private set

    val actionStatuses = mutableStateMapOf<String, DemoActionStatus>()
    val logEntries get() = DemoLog.entries

    private var lastHandledDeepLink: String? = null

    init {
        DemoLog.demo("app boot: FCM, signedIn=${signedInEmail.isNotBlank()}")
        refreshState()
        if (signedInEmail.isNotBlank() && DemoConfig.hasOrttoConfig) {
            identify(signedInEmail, fromLogin = false)
            Ortto.instance().dispatchPushRequest()
            DemoLog.demo("app boot: dispatched cached SDK token")
        }
        bootstrapFirebaseToken()
    }

    val isSignedIn: Boolean get() = signedInEmail.isNotBlank()
    val hasToken: Boolean get() = fcmToken.isNotBlank()
    val activeToken: String get() = tokenOverride.trim().ifBlank { fcmToken }

    val configurationIssues: List<ConfigurationIssue>
        get() = buildList {
            if (!DemoConfig.hasOrttoConfig) add(
                ConfigurationIssue(
                    "Ortto configuration missing",
                    "Copy demo/local.properties.example to demo/local.properties and set the endpoint and app key.",
                    true,
                ),
            )
            if (!DemoConfig.hasFirebaseConfig(getApplication())) add(
                ConfigurationIssue(
                    "Firebase configuration missing",
                    "Add a Firebase Android app for com.ortto.demo and copy google-services.json into demo/.",
                    true,
                ),
            )
            if (sessionId == null) add(
                ConfigurationIssue(
                    "No active SDK session",
                    "Identify a contact before treating token dispatch as associated with a user.",
                    false,
                ),
            )
            if (permissionStatus == "denied") add(
                ConfigurationIssue(
                    "Notifications disabled",
                    "Enable notifications for Ortto Push Demo in Android system settings.",
                    true,
                ),
            )
            if (!DemoConfig.hasCaptureConfig) add(
                ConfigurationIssue(
                    "In-app notifications not configured",
                    "Set the capture data-source key and capture JS URL to load and show widgets.",
                    false,
                ),
            )
        }

    fun signIn() {
        val cleaned = DemoLogic.normalizeEmail(email)
        if (cleaned.isBlank()) {
            toast("Enter an email address")
            return
        }
        signedInEmail = cleaned
        preferences.edit {
            putString("signed_in_email", cleaned)
            putString("last_email", cleaned)
        }
        DemoLog.demo("customer signed in")
        identify(cleaned, fromLogin = true)
    }

    fun identifyCurrentContact() {
        if (signedInEmail.isBlank()) return
        identify(signedInEmail, fromLogin = false)
    }

    private fun identify(value: String, fromLogin: Boolean) {
        if (!DemoConfig.hasOrttoConfig) {
            status("identify", "SDK configuration missing", DemoTone.Blocked)
            toast("Configure Ortto before identifying")
            return
        }
        if (isIdentifying) return
        isIdentifying = true
        status("identify", "Identifying contact…", DemoTone.Working)
        DemoLog.demo("calling Ortto.identify")
        val identity = UserID.make().setEmail(value).setExternalId("android-demo-$value")
        Ortto.instance().identify(identity, object : Ortto.OnIdentifyListener {
            override fun onComplete() = onMain {
                isIdentifying = false
                sessionId = Ortto.instance().sessionId
                status("identify", "Session is active", DemoTone.Success)
                toast(if (fromLogin) "Signed in and identified" else "Contact identified")
                DemoLog.demo("Ortto.identify completed; session ${DemoLogic.short(sessionId)}")
            }

            override fun onError(error: Throwable) = onMain {
                isIdentifying = false
                status("identify", "Identify failed", DemoTone.Warning)
                toast("Identify failed: ${error.message ?: "unknown error"}")
                DemoLog.demo("Ortto.identify failed: ${error.message}", "err")
            }
        })
    }

    fun logout() {
        if (isLoggingOut) return
        if (!DemoConfig.hasOrttoConfig) {
            completeLocalLogout()
            return
        }
        isLoggingOut = true
        DemoLog.demo("calling Ortto.clearIdentity")
        Ortto.instance().clearIdentity(object : Ortto.ClearIdentityCallback {
            override fun onSuccess(response: com.ortto.messaging.retrofit.RegistrationResponse?) = onMain {
                DemoLog.demo("Ortto.clearIdentity completed")
                completeLocalLogout()
            }

            override fun onFailure(error: Throwable) = onMain {
                DemoLog.demo("Ortto.clearIdentity failed: ${error.message}", "warn")
                completeLocalLogout()
            }
        })
    }

    private fun completeLocalLogout() {
        isLoggingOut = false
        signedInEmail = ""
        sessionId = null
        registrationStatus = "Not registered"
        preferences.edit {
            remove("signed_in_email")
            remove("registered_fcm_fingerprint")
        }
        selectedTab = DemoTab.Home
        toast("Signed out")
    }

    fun requestPushRegistration(requestPermission: () -> Unit) {
        if (!DemoConfig.hasOrttoConfig) {
            status("register", "Ortto configuration missing", DemoTone.Blocked)
            toast("Configure Ortto first")
            return
        }
        if (!DemoConfig.hasFirebaseConfig(getApplication())) {
            status("register", "Firebase configuration missing", DemoTone.Blocked)
            toast("Add demo/google-services.json")
            return
        }
        isRegistering = true
        status("register", "Requesting notification permission…", DemoTone.Working)
        DemoLog.demo("requesting Android notification permission")
        requestPermission()
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        permissionStatus = if (granted) "authorized" else "denied"
        if (!DemoConfig.hasOrttoConfig) return
        Ortto.instance().setPermission(if (granted) PushPermission.Accept else PushPermission.Deny)
        DemoLog.demo("notification permission result granted=$granted")
        if (granted) requestFirebaseTokenAndRegister() else {
            isRegistering = false
            status("register", "Permission denied", DemoTone.Blocked)
            toast("Notification permission denied")
        }
    }

    fun consumeFirstOpenPermissionRequest(): Boolean {
        if (preferences.getBoolean("asked_first_open_push", false)) return false
        preferences.edit { putBoolean("asked_first_open_push", true) }
        return DemoConfig.hasOrttoConfig && DemoConfig.hasFirebaseConfig(getApplication())
    }

    fun registerTokenOverride() {
        val token = activeToken
        if (token.isBlank()) {
            toast("No FCM token available")
            return
        }
        registerToken(token)
    }

    private fun requestFirebaseTokenAndRegister() {
        status("register", "Requesting Firebase registration token…", DemoTone.Working)
        DemoLog.demo("calling Ortto.getFirebaseToken")
        Ortto.instance().getFirebaseToken()
            .thenAccept { token -> onMain {
                fcmToken = token
                DemoLog.demo("FCM token received: SHA-256 ${DemoLogic.tokenFingerprint(token)}… (${token.length} chars)")
                registerToken(token)
            } }
            .exceptionally { error ->
                onMain {
                    isRegistering = false
                    status("register", "Firebase token failed", DemoTone.Warning)
                    toast("Firebase token failed: ${error.message}")
                    DemoLog.demo("Firebase token failed: ${error.message}", "err")
                }
                null
            }
    }

    private fun registerToken(token: String) {
        isRegistering = true
        status("register", "Registering FCM token with Ortto…", DemoTone.Working)
        DemoLog.demo("calling Ortto.registerDeviceToken with token fingerprint ${DemoLogic.tokenFingerprint(token)}")
        Ortto.instance().registerDeviceToken(token) {
            onMain {
                isRegistering = false
                registrationStatus = "Registered"
                sessionId = Ortto.instance().sessionId
                preferences.edit {
                    putString("registered_fcm_fingerprint", DemoLogic.tokenFingerprint(token))
                }
                status("register", "FCM token registered", DemoTone.Success)
                toast("FCM registration completed")
                DemoLog.demo("Ortto.registerDeviceToken completed")
            }
        }
    }

    fun redispatchToken() {
        if (!DemoConfig.hasOrttoConfig || !hasToken) {
            status("redispatch", "Register an FCM token first", DemoTone.Blocked)
            return
        }
        Ortto.instance().dispatchPushRequest()
        status("redispatch", "Redispatch called; watch the Log", DemoTone.Success)
        DemoLog.demo("called Ortto.dispatchPushRequest")
        toast("Cached token redispatched")
    }

    fun refreshPermission() {
        val context = getApplication<Application>()
        val manager = NotificationManagerCompat.from(context)
        val channel = context.getSystemService(NotificationManager::class.java)
            ?.getNotificationChannel(com.ortto.messaging.PushNotificationHandler.PUSH_ACTION)
        permissionStatus = when {
            !manager.areNotificationsEnabled() -> "denied"
            channel?.importance == NotificationManager.IMPORTANCE_NONE -> "channel blocked"
            else -> "authorized"
        }
        status("permission", "Permission: $permissionStatus", DemoTone.Success)
        DemoLog.demo("refreshed notification permission: $permissionStatus")
    }

    fun trackLink(value: String = trackedDeepLink) {
        val link = value.trim()
        if (link.isBlank() || !DemoLogic.hasTrackingUrl(link)) {
            status("track", "Paste a link containing tracking_url", DemoTone.Blocked)
            toast("Tracked deep link required")
            return
        }
        if (!DemoConfig.hasOrttoConfig) return
        isTrackingLink = true
        status("track", "Tracking click…", DemoTone.Working)
        DemoLog.demo("calling Ortto.trackLinkClick")
        Ortto.instance().trackLinkClick(link) {
            onMain {
                isTrackingLink = false
                status("track", "Click tracking completed", DemoTone.Success)
                DemoLog.demo("Ortto.trackLinkClick.success")
                toast("Click tracked")
            }
        }
    }

    fun handleIntent(intent: Intent?) {
        val link = intent?.dataString ?: intent?.getStringExtra("link") ?: return
        if (link == lastHandledDeepLink) return
        lastHandledDeepLink = link
        trackedDeepLink = link
        val deepLink = link.toUri()
        DemoLog.demo("notification deeplink opened: ${deepLink.scheme}://${deepLink.host}")
        if (DemoLogic.hasTrackingUrl(link) && DemoConfig.hasOrttoConfig) trackLink(link)
        if (deepLink.host.equals("confirm", ignoreCase = true)) {
            pendingConfirmationLink = link
        } else {
            DemoLogic.deepLinkTarget(link)?.let(::selectTab)
        }
    }

    fun confirmDeepLink() {
        pendingConfirmationLink = null
        selectTab(DemoTab.Delivery)
        DemoLog.demo("notification deeplink confirmed; opening Delivery")
    }

    fun dismissDeepLink() {
        pendingConfirmationLink = null
        DemoLog.demo("notification deeplink dismissed")
    }

    fun selectTab(tab: DemoTab) {
        selectedTab = tab
        if (DemoConfig.hasOrttoConfig) Ortto.instance().screen(tab.screenName)
        DemoLog.demo("screen viewed: ${tab.screenName}")
    }

    fun showWidget(value: String = widgetId) {
        val id = value.trim()
        if (!DemoConfig.hasCaptureConfig || id.isBlank()) {
            status("widget", "Configure Capture and enter a widget ID", DemoTone.Blocked)
            toast("Widget configuration missing")
            return
        }
        status("widget", "Showing widget…", DemoTone.Working)
        DemoLog.demo("calling Ortto.showWidget for $id")
        Ortto.instance().showWidget(id, object : Ortto.WidgetCallback {
            override fun onSuccess() = onMain {
                status("widget", "Widget shown", DemoTone.Success)
                toast("Widget shown")
            }

            override fun onFailure(t: Throwable) = onMain {
                status("widget", "Widget failed", DemoTone.Warning)
                toast("Widget failed: ${t.message}")
            }
        })
    }

    fun loadWidgets() {
        if (!DemoConfig.hasCaptureConfig) {
            status("loadWidgets", "Capture configuration missing", DemoTone.Blocked)
            toast("Configure Capture first")
            return
        }
        isLoadingWidgets = true
        status("loadWidgets", "Loading popup widgets…", DemoTone.Working)
        worker.execute {
            runCatching { fetchWidgets() }
                .onSuccess { result -> onMain {
                    widgets = result.filter { it.type == "popup" }
                    isLoadingWidgets = false
                    status("loadWidgets", "${widgets.size} popup widget(s) loaded", DemoTone.Success)
                    DemoLog.demo("widget list loaded: ${widgets.size} popup")
                } }
                .onFailure { error -> onMain {
                    isLoadingWidgets = false
                    status("loadWidgets", "Widget list failed", DemoTone.Warning)
                    toast("Could not load widgets: ${error.message}")
                    DemoLog.demo("widget list failed: ${error.message}", "warn")
                } }
        }
    }

    private fun fetchWidgets(): List<DemoWidget> {
        val connection = URL(DemoConfig.endpoint + "-/widgets/get").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        val body = JSONObject()
            .put("h", DemoConfig.captureDataSourceKey)
            .put("tk", false)
            .put("ottlk", "")
        sessionId?.let { body.put("s", it) }
        connection.outputStream.use { it.write(body.toString().toByteArray()) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val raw = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) error("HTTP $code")
        val array = JSONObject(raw.ifBlank { "{}" }).optJSONArray("widgets") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val id = item.optString("id")
                val type = item.optString("type")
                if (id.isNotBlank() && type.isNotBlank()) add(DemoWidget(id, type))
            }
        }
    }

    private fun bootstrapFirebaseToken() {
        if (!DemoConfig.hasFirebaseConfig(getApplication())) return
        FirebaseApp.getInstance()
        Ortto.instance().getFirebaseToken()
            .thenAccept { token -> onMain {
                fcmToken = token
                registrationStatus = if (
                    preferences.getString("registered_fcm_fingerprint", null) == DemoLogic.tokenFingerprint(token)
                ) "Registered" else "Token available"
                DemoLog.demo("Firebase bootstrap token: SHA-256 ${DemoLogic.tokenFingerprint(token)}… (${token.length} chars)")
            } }
            .exceptionally { error ->
                DemoLog.demo("Firebase bootstrap failed: ${error.message}", "warn")
                null
            }
    }

    private fun refreshState() {
        sessionId = if (DemoConfig.hasOrttoConfig) Ortto.instance().sessionId else null
        refreshPermission()
    }

    private fun status(id: String, text: String, tone: DemoTone) {
        actionStatuses[id] = DemoActionStatus(text, tone)
    }

    private fun toast(message: String) {
        toastMessage = message
    }

    fun consumeToast() {
        toastMessage = null
    }

    private fun onMain(block: () -> Unit) {
        ContextCompat.getMainExecutor(getApplication()).execute(block)
    }

    override fun onCleared() {
        worker.shutdownNow()
        super.onCleared()
    }
}
