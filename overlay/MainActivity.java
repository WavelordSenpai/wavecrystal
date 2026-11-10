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
            startService(new Intent(MainActivity.this, FloatingService.class));
            moveTaskToBack(true);
        }
        @JavascriptInterface
        public void setShape(String shape) {
            getSharedPreferences("wave", MODE_PRIVATE).edit().putString("shape", shape).apply();
        }
        @JavascriptInterface
        public void setColor(String color) {
            getSharedPreferences("wave", MODE_PRIVATE).edit().putString("color", color).apply();
        }
    }

    private void startFloatingService() {
        startService(new Intent(this, FloatingService.class));
    }
    private void stopFloatingService() {
        stopService(new Intent(this, FloatingService.class));
    }
    private boolean isMiniMode() {
        return getSharedPreferences("wave", MODE_PRIVATE).getBoolean("mini", false);
    }

    @Override
    public void onResume() {
        super.onResume();
        stopFloatingService();
        getSharedPreferences("wave", MODE_PRIVATE).edit().putBoolean("mini", false).apply();
        try { getBridge().getWebView().addJavascriptInterface(new MiniBridge(), "AndroidMini"); } catch (Exception e) {}
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isMiniMode() && Settings.canDrawOverlays(this)) {
            startFloatingService();
        }
    }
}
