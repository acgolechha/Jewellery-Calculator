package com.golechha.calcy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWebView = findViewById(R.id.myWebView);

        // --- 1. STEALTH FIX: Hide the WebView immediately ---
        myWebView.setVisibility(View.INVISIBLE);
        myWebView.setBackgroundColor(Color.parseColor("#F6F4FB"));
        myWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setAllowFileAccess(true);

        // --- 2. MAGIC BRIDGE: Image Sharing logic ---
        myWebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void shareImage(String base64Data) {
                try {
                    String base64Image = base64Data.split(",")[1];
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Quotation", null);
                    Uri uri = Uri.parse(path);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/png");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(shareIntent, "Share Quotation"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "AndroidBridge");

        // --- 3. STEALTH REVEAL: Show WebView ONLY when fully loaded ---
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // The white screen is gone! Reveal the calculator:
                myWebView.setVisibility(View.VISIBLE);
            }
        });

        // Start loading the HTML
        myWebView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myWebView != null) { myWebView.onPause(); myWebView.pauseTimers(); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myWebView != null) { myWebView.onResume(); myWebView.resumeTimers(); }
    }

    @Override
    public void onBackPressed() {
        if (myWebView != null && myWebView.canGoBack()) { myWebView.goBack(); }
        else { super.onBackPressed(); }
    }
}