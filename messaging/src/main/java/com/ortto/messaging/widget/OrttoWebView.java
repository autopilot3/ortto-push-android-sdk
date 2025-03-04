package com.ortto.messaging.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebViewAssetLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.ortto.messaging.Ortto;
import com.ortto.messaging.R;
import com.ortto.messaging.retrofit.WidgetType;
import com.ortto.messaging.retrofit.WidgetsGetRequest;
import com.ortto.messaging.retrofit.WidgetsGetResponse;
import com.ortto.messaging.retrofit.widget.Widget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("SetJavaScriptEnabled")
class OrttoWebView extends FrameLayout {
    static final String tag = "OrttoWebView";
    static final String pageUrl = "https://appassets.androidplatform.net/assets/shared/webview/www/index.html";

    WebView webView;
    WebViewMessageBus webViewMessageBus;
    WebViewAssetLoader assetLoader;
    CaptureConfig config;
    Activity activity;
    final int originalSoftInputMode;

    public OrttoWebView(Activity activity, CaptureConfig config) {
        super(activity.getBaseContext());

        this.activity = activity;
        this.config = config;
        this.originalSoftInputMode = activity.getWindow().getAttributes().softInputMode;

        Context context = activity.getBaseContext();

        webView = new WebView(context);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d(tag, consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }
        });
        webView.setBackgroundColor(0);
        webView.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setDomStorageEnabled(true);

        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(webView.getContext()))
                .build();

        FrameLayout self = this;

        webViewMessageBus = new WebViewMessageBus(activity.getBaseContext());
        webViewMessageBus.setHandler(new WebViewMessageHandler() {
            @Override
            public void onWidgetClosed(String id) {
                // A delay before removing the WebView allows time
                // for the close animation to play
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup viewGroup = (ViewGroup) self.getParent();
                        viewGroup.removeView(self);

                        Ortto.instance().capture.onWidgetClosed(id);
                    }
                }, 500);
            }

            @Override
            public void onWidgetShown(String id) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View focused = activity.getCurrentFocus();
                        if (focused != null) {
                            InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
                        }

                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        // Listen for changes in the layout that indicate the keyboard is shown or hidden
                        self.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                Rect r = new Rect();
                                self.getWindowVisibleDisplayFrame(r);

                                int screenHeight = self.getRootView().getHeight();

                                // r.bottom is the position above soft keypad or device button.
                                // If keypad is shown, the r.bottom is smaller than the screen height.
                                int keypadHeight = screenHeight - r.bottom;

                                if (keypadHeight > screenHeight * 0.15) { // assuming keyboard takes up > 15% of the screen
                                    // Keyboard is open
                                    layoutParams.bottomMargin = keypadHeight;
                                } else {
                                    // Keyboard is closed
                                    layoutParams.bottomMargin = 0;
                                }

                                self.setLayoutParams(layoutParams);
                            }
                        });

                        View decorView = activity.getWindow().getDecorView();

                        // The decor view may not be a ViewGroup
                        // This allows us to overlay the action and status bars of the app
                        // Otherwise, we fall back to adding it to the activity's content view
                        if (decorView instanceof ViewGroup) {
                            ((ViewGroup)decorView).addView(self, layoutParams);
                        } else {
                            activity.addContentView(self, layoutParams);
                        };
                    }
                });
            }

            @Override
            public void onTrack(JSONObject options) {
                Log.d(tag, "ap3c-track: " + options.toString());
            }
        });
        webView.addJavascriptInterface(webViewMessageBus, "Android");

        this.addView(webView);
    }

    public void showWidget(String id) {
        Call<WidgetsGetResponse> widgets = Ortto.instance().client.getWidgets(createWidgetsGetRequest());

        FrameLayout frameLayout = this;

        widgets.enqueue(new Callback<WidgetsGetResponse>() {
            @Override
            public void onResponse(Call<WidgetsGetResponse> call, Response<WidgetsGetResponse> response) {
                try {
                    WidgetsGetResponse newBody = transformWidgetsData(response.body(), id);

                    if (newBody.sessionId != null) {
                        Ortto.instance().setSession(newBody.sessionId);
                    }

                    if (newBody.widgets.size() == 0) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Ortto.instance().capture.processNextWidgetFromQueue();
                            }
                        });

                        return;
                    }

                    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                    JsonObject pageData = new JsonObject();
                    pageData.addProperty("token", config.getDataSourceKey());
                    pageData.addProperty("endpoint", config.getApiHost());
                    pageData.addProperty("captureJsUrl", config.getCaptureJsUrl());
                    pageData.add("data", gson.toJsonTree(newBody));
                    pageData.add("context", gson.toJsonTree(getPageContext()));

                    String injectPageData = "ap3cWebView.setConfig(" + gson.toJson(pageData) + "); ap3cWebView.start();";

                    Log.d(tag, injectPageData);

                    final LocalContentWebViewClient webViewClient = new LocalContentWebViewClient(assetLoader, webView.getContext()) {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            webView.evaluateJavascript(injectPageData, null);
                        }
                    };
                    webView.setWebViewClient(webViewClient);
                    webView.loadUrl(getPageUrl());
                } catch (Exception e) {
                    // do nothing
                    Log.e(tag, "Error", e);
                }
            }

            @Override
            public void onFailure(Call<WidgetsGetResponse> call, Throwable t) {
                Log.e(tag, "Error", t);
            }
        });
    }

    private String getAppName() {
        Context context = this.getContext();
        ApplicationInfo applicationInfo =  context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private Optional<String> getScreenName() {
        return Optional.ofNullable(Ortto.instance().screenName);
    }

    private Map<String, String> getPageContext() {
        Map<String, String> context = new HashMap<>();

        Optional<String> screenName = getScreenName();
        if (screenName.isPresent()) {
            context.put("shown_on_screen", screenName.get());
        } else {
            context.put("shown_on_screen", getAppName() + " (Android)");
        }

        return context;
    }

    private void setSoftInputMode(SoftInputMode mode) {
        Window window = activity.getWindow();
        switch (mode) {
            case ADJUST_RESIZE:
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                break;
            case ORIGINAL:
                window.setSoftInputMode(originalSoftInputMode);
                break;
        }
    }

    private WidgetsGetRequest createWidgetsGetRequest() {
        WidgetsGetRequest request = new WidgetsGetRequest();
        request.sessionId = Ortto.instance().sessionId;
        request.contactId = Ortto.instance().identity.contactId;
        request.emailAddress = Ortto.instance().identity.email;
        request.applicationKey = this.config.getDataSourceKey();

        return request;
    }

    private WidgetsGetResponse transformWidgetsData(WidgetsGetResponse data, String widgetId) {
        List<Widget> widgets = data.widgets;

        // only popup type widgets are supported at the moment
        List<Widget> popups = widgets.stream()
                .filter(widget -> widget.type == WidgetType.POPUP)
                .filter(widget -> widget.id.equalsIgnoreCase(widgetId))
                .filter(widget -> {
                    if (widget.expiry != null) {
                        return Instant.now().isBefore(widget.expiry.toInstant());
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // injected data will be cached for 30 minutes
        long ttl = 30 * 60 * 1000;

        WidgetsGetResponse newBody = data;
        newBody.widgets = popups;
        newBody.expiry = Instant.now().toEpochMilli() + ttl;

        return newBody;
    }

    private String getPageUrl() {
        Ortto ortto = Ortto.instance();

        return pageUrl +
                "?ap3c=" + urlEncode(ortto.sessionId) +
                "&email=" + urlEncode(ortto.identity.email);
    }

    private String urlEncode(String str) {
        if (str == null) {
            return "";
        }

        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    static enum SoftInputMode {
        ORIGINAL,
        ADJUST_RESIZE,
    }
}
