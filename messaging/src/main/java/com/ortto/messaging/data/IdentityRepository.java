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
        Ortto.log().info("IdentityRepository@clearAll: identifier set to null");

    }

    public void sendIdentityToServer(UserID identity, String sessionId) {
        Ortto.log().info("IdentityRepository@sendIdentityToServer");

        if (this.call != null && this.call.isExecuted()) {
            Ortto.log().warning("IdentityRepository.alreadyRequesting");

            return;
        }

        // Verify the user has been identified
        if (Ortto.instance().identity == null) {
            Ortto.log().warning("IdentityRepository.notIdentified");

            return;
        }

        // if identity is null, we should still be able to register

         IdentityRegistration identityRegistration = new IdentityRegistration(
                 identity,
                 Ortto.instance().getConfig().appKey,
                 sessionId,
                 Ortto.instance().getConfig().shouldSkipNonExistingContacts
         );

        String json = (new Gson()).toJson(identityRegistration);
        Ortto.log().info(json);

        this.call = Ortto.instance()
                .client
                .createIdentity(identityRegistration, Ortto.instance().getTrackingQuery());

        this.call.enqueue(new Callback<RegistrationResponse>() {
             @Override
             public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                 reset();
                 Ortto.log().info("IdentityRepository@res.complete code="+response.code());

                 if (!response.isSuccessful()) {
                     return;
                 }

                 RegistrationResponse body = response.body();

                 if (body != null) {
                     Ortto.instance().setSession(body.sessionId);
                 }
             }

             @Override
             public void onFailure(Call<RegistrationResponse> call, Throwable t) {
                 reset();
                 Ortto.log().warning("res.fail code="+t.getMessage());
             }
         });
    }

    protected void reset() {
        this.call = null;
    }
}
