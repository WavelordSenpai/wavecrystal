package com.wavecrystal.app.overlay;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import com.wavecrystal.app.MainActivity;
import com.wavecrystal.app.R;

public class FloatingService extends Service {

    private WindowManager windowManager;
    private View bubble;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        bubble = LayoutInflater.from(this).inflate(R.layout.float_crystal, null);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                300, 300,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.x = 100;
        params.y = 400;

        // --- THE LIVING CRYSTAL ---
        WebView web = bubble.findViewById(R.id.crystalWeb);
        web.getSettings().setJavaScriptEnabled(true);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        String html = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<style>"
                + "body{margin:0;background:transparent;display:flex;justify-content:center;align-items:center;height:100vh;overflow:hidden;}"
                + ".wrap{perspective:600px;}"
                + ".crystal{width:78px;height:78px;border-radius:50%;animation:spin 4s linear infinite;filter:drop-shadow(0 0 14px rgba(255,215,0,0.9)) drop-shadow(0 0 30px rgba(255,170,0,0.5));}"
                + "@keyframes spin{from{transform:rotateY(0deg) rotateX(10deg);}to{transform:rotateY(360deg) rotateX(10deg);}}"
                + ".glow{position:absolute;width:90px;height:90px;border-radius:50%;background:radial-gradient(circle, rgba(255,215,0,0.35) 0%, transparent 70%);animation:pulse 2s ease-in-out infinite;}"
                + "@keyframes pulse{0%,100%{opacity:0.6;transform:scale(1);}50%{opacity:1;transform:scale(1.15);}}"
                + "</style></head><body>"
                + "<div class='glow'></div>"
                + "<div class='wrap'><img class='crystal' src='file:///android_res/drawable/ic_launcher.png' /></div>"
                + "</body></html>";

        web.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        // --- DRAG vs TAP ---
        final float[] downPos = new float[2];
        final int[] downParams = new int[2];
        final boolean[] moved = {false};

        bubble.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downPos[0] = event.getRawX();
                        downPos[1] = event.getRawY();
                        downParams[0] = params.x;
                        downParams[1] = params.y;
                        moved[0] = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - downPos[0]);
                        int dy = (int) (event.getRawY() - downPos[1]);
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) moved[0] = true;
                        params.x = downParams[0] + dx;
                        params.y = downParams[1] + dy;
                        windowManager.updateViewLayout(bubble, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved[0]) {
                            // TAP = open app
                            Intent intent = new Intent(FloatingService.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            stopSelf();
                        }
                        return true;
                }
                return false;
            }
        });

        windowManager.addView(bubble, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bubble != null) windowManager.removeView(bubble);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
