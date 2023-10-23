package com.ortto.messaging.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

public class LocalContentWebViewClient extends WebViewClientCompat {
    private final WebViewAssetLoader assetLoader;
    private final Context context;

    LocalContentWebViewClient(WebViewAssetLoader assetLoader, Context context) {
        this.assetLoader = assetLoader;
        this.context = context;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return assetLoader.shouldInterceptRequest(request.getUrl());
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return assetLoader.shouldInterceptRequest(Uri.parse(url));
    }

    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
        Uri url = request.getUrl();

        if (url.getHost().equals("appassets.androidplatform.net")) {
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        return true;
    }
}
