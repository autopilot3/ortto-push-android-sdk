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
import android.util.Log;

import androidx.annotation.NonNull;
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

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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

    protected static Context appContext;
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

    public void init(OrttoConfig newConfig, @NonNull Application application, Logger logger) {
        setLogger(logger);
        init(newConfig, application);
    }

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
            listener.onPermissionGranted();

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

    protected void dispatchIdentifyRequest() {
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
                        } else {
                            log().warning("dispatchPushRequest@dispatchPushRequest firebase.getToken.fail");
                        }
                    }
                });
    }

    /**
     * Set the current session created by the Ortto tracking API
     *
     * @param response Response received from the Registration service
     */
    public void setSession(RegistrationResponse response) {
        this.sessionId = response.sessionId;
        preferences.put("sessionId", response.sessionId);
    }

    public void setPermission(PushPermission permission) {
        this.permission = permission;
        preferences.setPermission(permission);
    }

    public Map<String, String> getTrackingQuery() {
        HashMap<String, String> query = new HashMap<>();
        query.put("an", appContext.getPackageName()); // App Name
        query.put("av", getApplicationVersion()); // App Version
        query.put("sv", getSdkVersion()); // SDK Version
        query.put("os", "android"); // OS Name
        query.put("ov", Build.VERSION.RELEASE); // OS Version
        query.put("dc", Build.MODEL); // Device Name

        return query;
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

    protected static String getSdkVersion() {
        return BuildConfig.SDK_VERSION;
    }

    protected static String getApplicationVersion() {
        try{
            return appContext.getPackageManager()
                    .getPackageInfo(appContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void trackLinkClick(String link, OnTrackedListener listener) {

        Uri uri = Uri.parse(link);
        String trackingUrl = uri.getQueryParameter("tracking_url");
        String decodedString;

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(trackingUrl);
             decodedString = new String(decodedBytes);
        } catch (RuntimeException e) {
            decodedString = trackingUrl;
        }

        LinkUtm utm = LinkUtm.fromUri(uri);

        if (decodedString == null || decodedString.isEmpty()) {
            if (listener != null) {
                listener.onComplete(utm);
            }

            return;
        }


        Call<TrackingClickedResponse> call = client.trackLinkClick(decodedString, Ortto.instance().getTrackingQuery());
        Ortto.log().info("Ortto@trackLinkClick url="+call.request().url());

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
}
