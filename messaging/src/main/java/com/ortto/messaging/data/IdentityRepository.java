package com.ortto.messaging.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.ortto.messaging.BuildConfig;
import com.ortto.messaging.Ortto;
import com.ortto.messaging.identity.UserID;
import com.ortto.messaging.retrofit.IdentityRegistration;
import com.ortto.messaging.retrofit.RegistrationResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.CompletableFuture;

public class IdentityRepository {

    protected SharedPreferences sharedPreferences;

    protected Call<RegistrationResponse> call;

    public String sessionId;

    public UserID identifier = null;

    public PushPermission permission = PushPermission.Automatic;

    public IdentityRepository(Context context) {
        sharedPreferences = context
            .getApplicationContext()
            .getSharedPreferences("com.ortto.preferences", Context.MODE_PRIVATE);

        if (sharedPreferences.contains("identity")) {
            identifier = new Gson().fromJson(get("identity"), UserID.class);
            Ortto.log().info("IdentityRepository@identity: "+identifier.externalId);
        }

        if (sharedPreferences.contains("permission")) {
            this.permission = PushPermission.fromString(
                    sharedPreferences.getString("permission", PushPermission.Automatic.toString())
            );
            Ortto.log().info("IdentityRepository@permission: "+permission.toString());
        }

        if (sharedPreferences.contains("sessionId")) {
            this.sessionId = get("sessionId");
            Ortto.log().info("IdentityRepository@sessionId: "+sessionId);
        }
    }

    public void put(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public void setPermission(PushPermission permission) {
        this.permission = permission;
        this.put("permission", permission.toString());
    }

    public void setIdentifier(UserID identifier) {
        this.identifier = identifier;
        if (identifier != null) {
            this.put("identity", identifier.toJson());
        } else {
            sharedPreferences.edit().remove("identity").apply();
        }
    }

    public String get(String key) {
        return sharedPreferences.getString(key, "DEFAULT");
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
        this.setIdentifier(null);
        Ortto.log().info("IdentityRepository@clearAll");
    }

    public CompletableFuture<Void> sendIdentityToServer(UserID identity, String sessionId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (this.call != null && this.call.isExecuted()) {
            Ortto.log().warning("IdentityRepository.alreadyRequesting");
            future.complete(null);
            return future;
        }

        if (Ortto.instance().identity == null) {
            Ortto.log().warning("IdentityRepository.notIdentified");
            future.complete(null);
            return future;
        }

        IdentityRegistration identityRegistration = new IdentityRegistration(
                identity,
                Ortto.instance().getConfig().appKey,
                sessionId,
                Ortto.instance().getConfig().shouldSkipNonExistingContacts
        );

        // json encode registration and log it
        Ortto.log().info("IdentityRepository@sendIdentityToServer: "+(new Gson().toJson(identityRegistration)));

        this.call = Ortto.instance()
                .client
                .createIdentity(identityRegistration, Ortto.instance().getTrackingQuery());

        this.call.enqueue(new Callback<RegistrationResponse>() {
            @Override
            public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                reset();
                if (!response.isSuccessful()) {
                    future.completeExceptionally(new Exception("Request failed with code: " + response.code()));
                    return;
                }

                RegistrationResponse body = response.body();
                if (body != null) {
                    Ortto.instance().setSession(body.sessionId);
                }
                future.complete(null);
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
}
