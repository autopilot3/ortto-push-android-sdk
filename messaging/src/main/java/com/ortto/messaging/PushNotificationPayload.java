package com.ortto.messaging;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class PushNotificationPayload implements Parcelable {

    public String deepLink;
    public String id;
    public Bundle extras;
    public String title;
    public String body;

    protected PushNotificationPayload(Parcel in) {
        deepLink = in.readString();
        id = in.readString();
        extras = in.readBundle(PushNotificationPayload.class.getClassLoader());
        title = in.readString();
        body = in.readString();
    }

    public PushNotificationPayload(
            String link,
            String id,
            Bundle extras,
            String title,
            String body
    ) {
        this.deepLink = link;
        this.id = id;
        this.extras = extras;
        this.title = title;
        this.body = body;
    }

    public static final Creator<PushNotificationPayload> CREATOR = new Creator<PushNotificationPayload>() {
        @Override
        public PushNotificationPayload createFromParcel(Parcel in) {
            return new PushNotificationPayload(in);
        }

        @Override
        public PushNotificationPayload[] newArray(int size) {
            return new PushNotificationPayload[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeString(deepLink);
        out.writeString(id);
        out.writeBundle(extras);
        out.writeString(title);
        out.writeString(body);
    }
}
