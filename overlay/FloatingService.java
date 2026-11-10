package com.wavecrystal.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class FloatingService extends Service {
    private WindowManager wm;
    private View floatView;
    private WindowManager.LayoutParams params;
    private WebView crystalWeb;
    private View closeBtn;

    private int initialX, initialY;
    private float initialTouchX, initialTouchY;
    private long touchDownTime;
    private boolean isDragging = false;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatView = LayoutInflater.from(this).inflate(R.layout.float_crystal, null);
        crystalWeb = floatView.findViewById(R.id.crystalWeb);
        closeBtn = floatView.findViewById(R.id.closeBtn);

        // --- FIXED: Load ONLY the solid, not the whole webpage ---
        WebSettings s = crystalWeb.getSettings();
        s.setJavaScriptEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setDomStorageEnabled(true);
        crystalWeb.setBackgroundColor(0x00000000);

        SharedPreferences prefs = getSharedPreferences("wave", MODE_PRIVATE);
        String shape = prefs.getString("shape", "octa");
        String color = prefs.getString("color", "#FFD700");
        crystalWeb.loadUrl("file:///android_asset/crystal.html?shape=" + shape + "&color=" + color.replace("#", "%23"));

        params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 400;

        wm.addView(floatView, params);

        // --- X BUTTON ONLY closes ---
        closeBtn.setOnClickListener(v -> {
            stopSelf();
        });
        closeBtn.bringToFront();

        View.OnTouchListener dragTouch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        touchDownTime = System.currentTimeMillis();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - initialTouchX);
                        int dy = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isDragging = true;
                        }
                        params.x = initialX + dx;
                        params.y = initialY + dy;
                        wm.updateViewLayout(floatView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        long touchTime = System.currentTimeMillis() - touchDownTime;
                        if (!isDragging && touchTime < 300) {
                            Intent i = new Intent(FloatingService.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(i);
                            stopSelf();
                        }
                        return true;
                }
                return false;
            }
        };

        crystalWeb.setOnTouchListener(dragTouch);
        floatView.setOnTouchListener(dragTouch);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatView != null) {
            try { wm.removeView(floatView); } catch (Exception e) {}
        }
        getSharedPreferences("wave", MODE_PRIVATE).edit().putBoolean("mini", false).apply();
    }
}
