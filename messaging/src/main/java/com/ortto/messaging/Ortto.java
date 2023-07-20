package com.ortto.messaging;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ortto.messaging.data.LinkUtm;
import com.ortto.messaging.data.IdentityRepository;
import com.ortto.messaging.data.PushPermission;
import com.ortto.messaging.data.PushTokenRepository;
import com.ortto.messaging.identity.UserID;
import com.ortto.messaging.retrofit.RegistrationResponse;
import com.ortto.messaging.retrofit.TrackingClickedResponse;
import com.ortto.messaging.widget.CaptureConfig;
import com.ortto.messaging.widget.OrttoCapture;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Ortto Controller class
 */
public class Ortto {

    protected static Application appContext;
    protected static Ortto INSTANCE = null;
    protected static Logger logger = Logger.getLogger("ortto@sdk");

    protected OrttoConfig config;
    protected CustomDeeplinkCallback deeplinkCallback;
    protected IdentityRepository preferences;
    protected PushTokenRepository tokenRepository;

    public UserID identity = null;
    public String sessionId;
    public PushPermission permission = PushPermission.Automatic;

    protected Retrofit retrofit;
    public OrttoClientService client;

    public OrttoCapture capture;

    public void init(OrttoConfig newConfig, @NonNull Application application) {
        appContext = application;
        config = newConfig;
        preferences = new IdentityRepository(appContext);
        tokenRepository = new PushTokenRepository(appContext);
        sessionId = preferences.sessionId;
        identity = preferences.identifier;
        permission = preferences.permission;
        retrofit = new Retrofit.Builder()
                .baseUrl(getConfig().endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        client = retrofit.create(OrttoClientService.class);

        // Register activity lifecycle hooks
        application.registerActivityLifecycleCallbacks(new LifecycleListener());

        //
        dispatchIdentifyRequest();
    }

    public void initCapture(CaptureConfig config) {
        if (capture == null) {
            capture = new OrttoCapture(config, appContext);
        }
    }

    public static Logger log() {
        return INSTANCE.logger;
    }

    /**
     * Overwrite the internal SDK logger
     * @param logger Logger class to intercept SKD logs
     */
    public void setLogger(Logger logger) {
        INSTANCE.logger = logger;
    }

    public OrttoConfig getConfig() {
        return config;
    }

    public Optional<OrttoCapture> getCapture() {
        return Optional.ofNullable(capture);
    }

    public static Ortto instance() {
        if (INSTANCE == null) {
            INSTANCE = new Ortto();
        }

        return INSTANCE;
    }

    public void setDeeplinkHandler(CustomDeeplinkCallback deeplinkCallback) {
        this.deeplinkCallback = deeplinkCallback;
    }

    public Boolean getIsChannelActive() {
        ensureChannelIsCreated();

        NotificationManager manager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = manager.getNotificationChannel(PushNotificationHandler.PUSH_ACTION);

        if (channel == null) {
            log().info("channel is null");
            return false;
        }

        return channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }

    public CustomDeeplinkCallback getDeeplinkCallback() {
        return this.deeplinkCallback;
    }

    /**
     * Retrieve the name of the Android application running the Ortto SDK
     *
     * @return string
     */
    public static String getApplicationName() {
        ApplicationInfo applicationInfo = appContext.getApplicationInfo();
        int stringId = applicationInfo.labelRes;

        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : appContext.getString(stringId);
    }

    /**
     * Has the application registered a deeplink interception callback?
     *
     * @return bool
     */
    public boolean hasCustomDeeplinkCallback() {
        return this.deeplinkCallback != null;
    }

    /**
     * Determine if the user has granted push permissions to the SDK and/or app
     *
     * @return bool
     */
    public boolean hasUserGrantedPushPermissions() {
        NotificationManager manager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!manager.areNotificationsEnabled()) {
            return false;
        }

        return getIsChannelActive();
    }

    /**
     * Grab the current FCM token and sync with Ortto tracking api
     * @param token FCM Token
     */
    public void sendRegistrationToServer(String token) {
        if (!config.allowAnonUsers && sessionId == null) {
            // don't register if we haven't identified the user yet !
            return;
        }

        tokenRepository.sendToServer(token);
    }

    /**
     * Create a notification Channel for the settings screen to control
     */
    public void requestPermissions(Activity activity, PermissionUtil.PermissionAskListener listener) {
        if (hasUserGrantedPushPermissions()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                listener.onPermissionGranted();
                Ortto.instance().dispatchPushRequest();
            } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                listener.onPermissionPreviouslyDenied();
            } else {
                listener.onPermissionAsk();
            }
        } else {
            listener.onPermissionGranted();
            Ortto.instance().dispatchPushRequest();
        }
    }

    public void ensureChannelIsCreated() {
        NotificationChannel channel = new NotificationChannel(
                PushNotificationHandler.PUSH_ACTION,
                getApplicationName()+" Notifications",
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationManagerCompat
                .from(appContext)
                .createNotificationChannel(channel);
    }

    /**
     * Delete the instance and start over
     */
    public static void clear() {
        INSTANCE = null;
    }

    /**
     * Delete all saved identity data
     */
    public void clearData() {
        preferences.clearAll();
    }

    /**
     * Set the current user
     * @param identifier User Identity to associate with the session
     */
    public void identify(UserID identifier) {
        this.identity = identifier;
        preferences.setIdentifier(identifier);

        dispatchIdentifyRequest();
    }

    public void dispatchIdentifyRequest() {
        preferences.sendIdentityToServer(this.identity, this.sessionId);
    }

    public void dispatchPushRequest() {
        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful()) {
                            tokenRepository.sendToServer(task.getResult());
                        }
                    }
                });
    }

    /**
     * Set the current session created by the Ortto tracking API
     *
     * @param sessionId Session ID received from the Registration service or Capture API
     */
    public void setSession(String sessionId) {
        this.sessionId = sessionId;
        preferences.put("sessionId", sessionId);
    }

    public void setPermission(PushPermission permission) {
        this.permission = permission;
        preferences.setPermission(permission);
    }

    public void trackLinkClick(String link, OnTrackedListener listener) {

        Uri uri = Uri.parse(link);
        String trackingUrl = uri.getQueryParameter("tracking_url");
        String decodedString;

        Ortto.log().info("trackingUrl: "+trackingUrl);

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(trackingUrl);
             decodedString = new String(decodedBytes);
        } catch (RuntimeException e) {
            decodedString = trackingUrl;
        }

        Map<String, String> map = new HashMap<String, String>();
        LinkUtm utm = LinkUtm.fromUri(uri);

        if (decodedString == null || decodedString.isEmpty()) {
            if (listener != null) {
                listener.onComplete(utm);
            }

            return;
        }

        Call<TrackingClickedResponse> call = client.trackLinkClick(decodedString, map);

        call.enqueue(new Callback<TrackingClickedResponse>() {
            @Override
            public void onResponse(@NonNull Call<TrackingClickedResponse> call, @NonNull Response<TrackingClickedResponse> response) {
                Ortto.log().info("Ortto@trackLinkClick.req.complete code="+response.code());
                if (listener != null) {
                    listener.onComplete(utm);
                }
            }

            @Override
            public void onFailure(Call<TrackingClickedResponse> call, Throwable t) {
                Ortto.log().warning("Ortto@trackLinkClick.req.fail msg="+t.getMessage());
            }
        });
    }

    public interface OnTrackedListener {
        void onComplete(LinkUtm utm);
    }

    public void showWidget(String id) throws OrttoCaptureInitException {
        if (capture == null) {
            throw new OrttoCaptureInitException();
        }

        capture.showWidget(id);
    }

    public void queueWidget(String id) throws OrttoCaptureInitException {
        queueWidget(id, null);
    }

    public void queueWidget(String id, Map<String, String> metadata) throws OrttoCaptureInitException {
        if (capture == null) {
            throw new OrttoCaptureInitException();
        }

        capture.queueWidget(id, metadata);
    }
}

