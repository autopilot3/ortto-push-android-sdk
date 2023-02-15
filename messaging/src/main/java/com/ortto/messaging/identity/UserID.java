package com.ortto.messaging.identity;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class UserID {

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    @SerializedName("accepts_gdpr")
    public boolean acceptsGdpr;

    @SerializedName("contact_id")
    public String contactId;

    @SerializedName("email")
    public String email;

    @SerializedName("external_id")
    public String externalId;

    @SerializedName("phone_number")
    public String phone;

    public String toJson() {
        Gson gson = new Gson();

        return  gson.toJson(this);
    }

    public static UserID make() {
        return new UserID();
    }

    public UserID setExternalId(String uid) {
        this.externalId = uid;

        return this;
    }

    public UserID setEmail(String email) {
        this.email = email;

        return this;
    }

    public UserID setName(String first, String last) {
        this.firstName = first;
        this.lastName = last;

        return this;
    }

    public UserID setPhone(String phoneNumber) {
        this.phone = phoneNumber;

        return this;
    }

    public UserID setAcceptsGdpr(boolean accepts) {
        this.acceptsGdpr = accepts;

        return this;
    }

    public UserID setContactId(String cid) {
        this.contactId = cid;

        return this;
    }
}


