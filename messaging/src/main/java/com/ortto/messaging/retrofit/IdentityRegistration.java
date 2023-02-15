package com.ortto.messaging.retrofit;

import com.google.gson.annotations.SerializedName;
import com.ortto.messaging.identity.UserID;

public class IdentityRegistration {
    public IdentityRegistration(
            UserID identifier,
            String dataSourceInstanceIDHash,
            String sessionId,
            Boolean skipNonExistingContacts
    ) {
        this.dataSourceInstanceIDHash = dataSourceInstanceIDHash;
        this.associationEmail = identifier.email;
        this.contactID = identifier.contactId;
        this.associationPhone = identifier.phone;
        this.associationExternalID = identifier.externalId;
        this.firstName = identifier.firstName;
        this.lastName = identifier.lastName;
        this.acceptGDPR = identifier.acceptsGdpr;
        this.skipNonExistingContacts = skipNonExistingContacts;
        this.session = sessionId;
    }

    @SerializedName("appk")
    public String dataSourceInstanceIDHash;
    @SerializedName("c")
    public String contactID;
    @SerializedName("e")
    public String associationEmail;
    @SerializedName("p")
    public String associationPhone;
    @SerializedName("ei")
    public String associationExternalID;
    @SerializedName("s")
    public String session;
    @SerializedName("first")
    public String firstName;
    @SerializedName("last")
    public String lastName;
    @SerializedName("ag")
    public Boolean acceptGDPR = true;
    @SerializedName("sne")
    public Boolean skipNonExistingContacts = false;
    @SerializedName("pl")
    public String platform = "android";
}
