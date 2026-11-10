package com.wavecrystal.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

public class FloatingService extends Service {
    private WindowManager windowManager;
    private View floatView;
    private WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = LayoutInflater.from(this).inflate(R.layout.float_crystal, null);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
            180, 180, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 300;

        WebView web = floatView.findViewById(R.id.crystalWeb);
        web.getSettings().setJavaScriptEnabled(true);
        web.setBackgroundColor(0x00000000);
        web.loadUrl("file:///android_asset/crystal.html");

        View close = floatView.findViewById(R.id.closeBtn);
        if (close != null) {
            close.setOnClickListener(v -> stopSelf());
        }

        floatView.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REOPEN_TASK);
            startActivity(i);
            stopSelf();
        });

        floatView.setOnTouchListener(new View.OnTouchListener() {
            private int lastX, lastY;
            private long downTime;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        downTime = System.currentTimeMillis();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x += dx;
                        params.y += dy;
                        windowManager.updateViewLayout(floatView, params);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - downTime < 200) {
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(floatView, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatView != null) windowManager.removeView(floatView);
    }
}
