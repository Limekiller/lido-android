package com.bryceyoder.lido;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        WebView webView = findViewById(R.id.main_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Since this is supposed to be a generic app for any Lido deployment,
        // we don't assume that they've deployed SSL. Intercept any http:// requests and
        // rewrite them to https://. Also, ignore certificate errors.
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError er) {
                handler.proceed();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://")) {
                    url = url.replace("http://", "https://");
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }
        });

        // Using 720p as a base with scale at 80,
        // determine what scale should be for current resolution
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels;
        double multiplier = (height / 720.0);
        double scale = 80 * multiplier;

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setInitialScale((int)scale);

        webView.loadUrl("https://media.bryceyoder.com/");
    }
}
