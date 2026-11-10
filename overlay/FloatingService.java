package com.wavecrystal.app;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
public class FloatingService extends Service {
  private WindowManager wm;
  private View bubble;
  @Override public IBinder onBind(Intent i) { return null; }
  @Override public void onCreate() {
    super.onCreate();
    wm = (WindowManager) getSystemService(WINDOW_SERVICE);
    bubble = LayoutInflater.from(this).inflate(R.layout.float_crystal, null);
    int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
    WindowManager.LayoutParams p = new WindowManager.LayoutParams(200, 200, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
    p.x = 0; p.y = 200;
    wm.addView(bubble, p);
    ImageView img = bubble.findViewById(R.id.crystalIcon);
    img.setOnTouchListener(new View.OnTouchListener() {
      private int lastX, lastY; private float downX, downY;
      @Override public boolean onTouch(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) { lastX = p.x; lastY = p.y; downX = e.getRawX(); downY = e.getRawY(); return true; }
        if (e.getAction() == MotionEvent.ACTION_MOVE) { p.x = lastX + (int)(e.getRawX() - downX); p.y = lastY + (int)(e.getRawY() - downY); wm.updateViewLayout(bubble, p); return true; }
        if (e.getAction() == MotionEvent.ACTION_UP) {
          if (Math.abs(e.getRawX() - downX) < 10 && Math.abs(e.getRawY() - downY) < 10) {
            Intent intent = new Intent(FloatingService.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
          }
          return true;
        }
        return false;
      }
    });
  }
  @Override public void onDestroy() { super.onDestroy(); if (bubble != null) wm.removeView(bubble); }
}
