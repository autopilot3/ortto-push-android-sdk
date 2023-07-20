package com.ortto.messaging;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeepLinkHandler {
    Context context;

    public DeepLinkHandler(Context context) {
        this.context = context;
    }

    /**
     * Handle a deep link action click
     *
     * @param intent Push notification intent
     */
    public void handleIntent(@NonNull Intent intent) {
        Optional<Uri> deepLink = getDeepLink(intent);

        // Return early if theres no link included
        if (!deepLink.isPresent()) {
            return;
        }

        if (Ortto.instance().hasCustomDeeplinkCallback()) {
            return;
        }

        Optional<String> widgetId = getWidgetIdFromFragment(deepLink.get());
        widgetId.ifPresent(id -> {
            try {
                Ortto.instance().queueWidget(id);
            } catch (OrttoCaptureInitException e) {
                Ortto.log().warning("DeepLinkHandler@handle.fail: " + e.getMessage());
                e.printStackTrace();
            }
        });

        Intent activity = findDeeplinkApps(context, deepLink.get());

        try {
            context.startActivity(activity);
        } catch (RuntimeException e) {
            Ortto.log().warning("DeepLinkHandler@handle.fail: "+e.getMessage());
            e.printStackTrace();
        }
    }

    public Intent findDeeplinkApps(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage(context.getPackageName());

        Ortto.log().info("DeepLinkHandler@findDeeplinkApps uri="+uri.toString());

        int flags = Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP;

        ComponentName name = intent.resolveActivity(context.getPackageManager());

        intent.setFlags(flags);
        intent.setClassName(context.getPackageName(), name.getClassName());

        return intent;
    }

    public static Optional<String> getWidgetIdFromFragment(Uri uri) {
        if (uri != null) {
            Pattern pattern = Pattern.compile("widget_id=(?<widgetId>[a-z0-9]+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(uri.getFragment());

            if (matcher.find()) {
                String widgetId = matcher.group("widgetId");

                return Optional.of(widgetId);
            }
        }

        return Optional.empty();
    }

    private Optional<Uri> getDeepLink(Intent intent) {
        PushNotificationPayload payload = intent.getExtras().getParcelable("payload");
        if (payload == null) {
            return Optional.empty();
        }

        String link = payload.deepLink;
        if (link == null || link.isEmpty()) {
            link = intent.getExtras().getString("link");
        }

        if (link == null || link.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(Uri.parse(link));
    }
}
