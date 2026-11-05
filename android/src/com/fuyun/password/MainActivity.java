package com.fuyun.password;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 福运密码 · Fortune Password — a thin, fully offline shell around the
 * single-file web UI. The whole app is one HTML file in assets/.
 */
public class MainActivity extends Activity {

    private WebView web;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor("#8E1018"));
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#F0D9D3"));
        web.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setTextZoom(100);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                // The app is offline by design: never leave the bundled assets.
                return !url.startsWith("file:///android_asset/");
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");

        root.addView(web);
        setContentView(root);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#8E1018"));
            getWindow().setNavigationBarColor(Color.parseColor("#8E1018"));
        }
        web.loadUrl("file:///android_asset/index.html");
    }

    /** Native clipboard fallback for WebView builds without async clipboard. */
    private class Bridge {
        @JavascriptInterface
        public void copy(String text) {
            if (text == null || text.isEmpty()) return;
            final ClipboardManager cm =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm == null) return;
            cm.setPrimaryClip(ClipData.newPlainText("password", text));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "已复制 · Copied", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public String platform() {
            return "android";
        }
    }

    @Override
    public void onBackPressed() {
        // Back closes the floating window first, then leaves the app.
        web.evaluateJavascript(
                "(function(){try{if(typeof isOpen!=='undefined'&&isOpen){closeWin();return 'closed';}}catch(e){}return 'exit';})()",
                new android.webkit.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if (value == null || value.contains("exit")) finish();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) web.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
    }
}
