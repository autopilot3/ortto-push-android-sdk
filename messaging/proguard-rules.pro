# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Ortto SDK Retrofit models from being obfuscated
# This ensures Gson can correctly serialize/deserialize based on field names or @SerializedName annotations
-keep class com.ortto.messaging.identity.** { *; }
-keep class com.ortto.messaging.retrofit.** { *; }
-keep class com.ortto.messaging.ActionItem { *; }
-keep class com.ortto.messaging.data.LinkUtm { *; }
-keep class com.ortto.messaging.widget.QueuedWidget { *; } # Keep specifically for SharedPreferences/Gson

# Keep JavaScript Interface class and methods
-keep class com.ortto.messaging.widget.WebViewMessageBus { *; }
-keepclassmembers class com.ortto.messaging.widget.WebViewMessageBus {
    @android.webkit.JavascriptInterface <methods>;
}

-keep enum com.ortto.messaging.retrofit.WidgetType { *; }
-keepclassmembers enum com.ortto.messaging.retrofit.WidgetType {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class com.ortto.messaging.PushNotificationPayload { *; }
-keepclassmembers class com.ortto.messaging.PushNotificationPayload implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator CREATOR;
}

# Keep classes used with Gson (SharedPreferences, Widget structure, etc.)
-keep class com.ortto.messaging.identity.UserID { *; }
-keep class com.ortto.messaging.widget.QueuedWidget { *; }
-keep class com.ortto.messaging.retrofit.widget.Page { *; }
-keep class com.ortto.messaging.retrofit.widget.Filter { *; }
-keep class com.ortto.messaging.retrofit.widget.Filter$Or { *; }
-keep class com.ortto.messaging.retrofit.widget.Filter$Or$StrIs { *; }
-keep class com.ortto.messaging.retrofit.widget.Filter$Or$StrContains { *; }
-keep class com.ortto.messaging.retrofit.widget.Filter$Or$StrStarts { *; }
-keep class com.ortto.messaging.retrofit.widget.When { *; }
-keep class com.ortto.messaging.retrofit.widget.Where { *; }
-keep class com.ortto.messaging.retrofit.widget.Trigger { *; }
-keep class com.ortto.messaging.retrofit.widget.Trigger$Rules { *; }
-keep class com.ortto.messaging.retrofit.widget.Trigger$Rules$Condition { *; }
