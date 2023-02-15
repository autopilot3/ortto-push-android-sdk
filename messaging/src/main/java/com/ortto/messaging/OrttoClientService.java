package com.ortto.messaging;

import com.ortto.messaging.retrofit.IdentityRegistration;
import com.ortto.messaging.retrofit.NotificationDeliveryResponse;
import com.ortto.messaging.retrofit.RegistrationResponse;
import com.ortto.messaging.retrofit.TokenRegistration;
import com.ortto.messaging.retrofit.TrackingClickedResponse;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 *
 */
public interface OrttoClientService {
    @POST("-/events/push-permission")
    Call<RegistrationResponse> createToken(@Body TokenRegistration token);

    @POST("-/events/push-mobile-session")
    Call<RegistrationResponse> createIdentity(@Body IdentityRegistration request);

    @GET
    Call<TrackingClickedResponse> trackLinkClick(@Url String url, @QueryMap(encoded = true) Map<String, String> params);

    @GET
    Call<NotificationDeliveryResponse> trackNotificationDelivery(@Url String url);
}
