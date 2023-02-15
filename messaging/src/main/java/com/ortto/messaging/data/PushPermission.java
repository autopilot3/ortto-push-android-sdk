package com.ortto.messaging.data;

public enum PushPermission {
    Accept,
    Deny,
    Automatic;

    public static PushPermission fromString(String permission) {
        try {
            return valueOf(permission);
        } catch (Exception ex) {
            // For error cases
            return Automatic;
        }
    }
}
