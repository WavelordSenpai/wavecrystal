package com.wavecrystal.app;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.*;
import android.widget.ImageView;

public class FloatingService extends Service {
  WindowManager wm; View bubble;
  @Override public void onCreate(){
    super.onCreate();
    wm = (WindowManager)getSystemService(WINDOW_SERVICE);
    bubble = LayoutInflater.from(this).inflate(R.layout.float_crystal, null);
    int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
    int size = (int)(84 * getResources().getDisplayMetrics().density);
    WindowManager.LayoutParams p = new WindowManager.LayoutParams(size, size, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
    p.gravity = Gravity.TOP | Gravity.END; p.x = 20; p.y = 200;
    wm.addView(bubble, p);
    ImageView img = bubble.findViewById(R.id.bubbleImg);
    final int[] last = new int[2]; final boolean[] moved = {false};
    img.setOnTouchListener((v, e) -> {
      switch(e.getAction()){
        case MotionEvent.ACTION_DOWN: last[0]=(int)e.getRawX(); last[1]=(int)e.getRawY(); moved[0]=false; return true;
        case MotionEvent.ACTION_MOVE:
          int dx=(int)e.getRawX()-last[0], dy=(int)e.getRawY()-last[1];
          if(Math.abs(dx)>10||Math.abs(dy)>10) moved[0]=true;
          p.x-=dx; p.y+=dy; wm.updateViewLayout(bubble,p);
          last[0]=(int)e.getRawX(); last[1]=(int)e.getRawY(); return true;
        case MotionEvent.ACTION_UP:
          if(!moved[0]){
            Intent i=new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
          } return true;
      } return false;
    });
  }
  @Override public void onDestroy(){ super.onDestroy(); if(bubble!=null) wm.removeView(bubble); }
  @Override public IBinder onBind(Intent i){ return null; }
}
