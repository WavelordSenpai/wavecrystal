package com.wavecrystal.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    private static final int OVERLAY_REQUEST = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ONLY ask permission, DO NOT start bubble here
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_REQUEST);
        }
        getSharedPreferences("wave", MODE_PRIVATE).edit().putBoolean("mini", false).apply();
    }

    public class MiniBridge {
        @JavascriptInterface
        public void enableMini() {
            getSharedPreferences("wave", MODE_PRIVATE).edit().putBoolean("mini", true).apply();
            moveTaskToBack(true);
        }
    }

    private void startFloatingService() {
        Intent intent = new Intent(this, FloatingService.class);
        startService(intent);
    }

    private void stopFloatingService() {
        Intent intent = new Intent(this, FloatingService.class);
        stopService(intent);
    }

    private boolean isMiniMode() {
        return getSharedPreferences("wave", MODE_PRIVATE).getBoolean("mini", false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        // App is open = HIDE bubble always
        stopFloatingService();
        getSharedPreferences("wave", MODE_PRIVATE).edit().putBoolean("mini", false).apply();
        try {
            getBridge().getWebView().addJavascriptInterface(new MiniBridge(), "AndroidMini");
        } catch (Exception e) {}
    }

    @Override
    public void onPause() {
        super.onPause();
        // ONLY show bubble IF user tapped Mini
        if (isMiniMode() && Settings.canDrawOverlays(this)) {
            startFloatingService();
        }
    }
}
