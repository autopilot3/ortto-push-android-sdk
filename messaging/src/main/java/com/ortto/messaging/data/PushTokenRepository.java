package com.ortto.messaging.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.ortto.messaging.Ortto;
import com.ortto.messaging.retrofit.TokenRegistration;
import com.ortto.messaging.retrofit.RegistrationResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;

/**
 * Repository class to handle push notification tokens.
 */
public class PushTokenRepository {

    protected Context context;

    protected Call<RegistrationResponse> call;

    private static final String PREFS_NAME = "com.ortto.messaging.prefs";
    private static final String KEY_PUSH_TOKEN = "push_token";

    public PushTokenRepository(Context context) {
        this.context = context;
    }

    /**
     * Forward the new FCM token to Ortto's API
     * @param token FCM Token
     */
    public CompletableFuture<RegistrationResponse> sendToServer(String token) {
        CompletableFuture<RegistrationResponse> future = new CompletableFuture<>();
        
        String lastToken = getStoredToken();
        if (token != null && token.equals(lastToken)) {
            Ortto.log().info("PushTokenRepository@sendToServer: Token unchanged, skipping send.");
            future.complete(null);
            return future;
        }

        if (this.call != null && this.call.isExecuted()) {
            Ortto.log().warning("PushTokenRepository@sendToServer.alreadyRequesting");
            future.complete(null);
            return future;
        }

        if (!canConnectToGoogleServices()) {
            Ortto.log().warning("PushTokenRepository@sendToServer.googleServicesConnection.fail");
            future.complete(null);
            return future;
        }

        TokenRegistration tokenRegistration = new TokenRegistration(
                Ortto.instance().getConfig().appKey,
                Ortto.instance().sessionId,
                token,
                isPermissionGranted()
        );

        this.call = Ortto.instance()
                .client
                .createToken(tokenRegistration, Ortto.instance().getTrackingQuery());

        this.call.enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegistrationResponse> call, @NonNull Response<RegistrationResponse> response) {
                reset();
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new Exception("Request failed with code: " + response.code()));
                    return;
                }
                RegistrationResponse body = response.body();
                if (body != null) {
                    Ortto.instance().setSession(body.sessionId);
                }
                setStoredToken(token);
                future.complete(body);
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                reset();
                Ortto.log().warning("res.fail code=" + t.getMessage());
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    protected void reset() {
        this.call = null;
    }

    private Boolean isPermissionGranted() {
        Ortto.log().info("PTR.isPermissionGranted : "+Ortto.instance().permission.toString());

        switch (Ortto.instance().permission) {
            case Accept:
                return true;
            case Deny:
                return false;
            default:
                return Ortto.instance().hasUserGrantedPushPermissions();
        }
    }

    private boolean canConnectToGoogleServices() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.context) == ConnectionResult.SUCCESS;
    }

    public CompletableFuture<RegistrationResponse> unsubscribe(String token) {
        CompletableFuture<RegistrationResponse> future = new CompletableFuture<>();

        // Generate the registration request
        TokenRegistration tokenRegistration = new TokenRegistration(
                Ortto.instance().getConfig().appKey,
                Ortto.instance().sessionId,
                token,
                false
        );

        this.call = Ortto.instance()
                .client
                .createToken(tokenRegistration, Ortto.instance().getTrackingQuery());

        this.call.enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(@NonNull Call<RegistrationResponse> call, @NonNull Response<RegistrationResponse> response) {
                reset();
                Ortto.log().info("PushTokenRepository@res.complete code="+response.code());

                if (!response.isSuccessful()) {
                    future.completeExceptionally(new Exception("Unsuccessful response"));
                    return;
                }

                Ortto.instance().setSession(null);
                setStoredToken(null);

                future.complete(response.body());
            }

            @Override
            public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                Log.d("ortto@sdk", "onFailure");
                reset();
                Ortto.log().warning("res.fail code="+t.getMessage());
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    private SharedPreferences getPrefs() {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private String getStoredToken() {
        return getPrefs().getString(KEY_PUSH_TOKEN, null);
    }

    private void setStoredToken(String token) {
        getPrefs().edit().putString(KEY_PUSH_TOKEN, token).apply();
    }
}