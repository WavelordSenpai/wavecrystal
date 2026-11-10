package com.wavecrystal.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

public class FloatingService extends Service {
    private WindowManager wm;
    private View floatView;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = LayoutInflater.from(this).inflate(R.layout.float_crystal, null);

        int type = android.os.Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            280, 280, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        p.x = 20;
        p.y = 200;
        wm.addView(floatView, p);

        floatView.setOnTouchListener(new View.OnTouchListener(){
            int ox, oy, sx, sy;
            boolean moved = false;
            public boolean onTouch(View v, MotionEvent e){
                if(e.getAction()==MotionEvent.ACTION_DOWN){
                    ox=p.x; oy=p.y; sx=(int)e.getRawX(); sy=(int)e.getRawY(); moved=false; return true;
                }
                if(e.getAction()==MotionEvent.ACTION_MOVE){
                    int dx=(int)e.getRawX()-sx, dy=(int)e.getRawY()-sy;
                    if(Math.abs(dx)>10||Math.abs(dy)>10) moved=true;
                    p.x=ox+dx; p.y=oy+dy; wm.updateViewLayout(floatView,p); return true;
                }
                if(e.getAction()==MotionEvent.ACTION_UP){
                    if(!moved){
                        Intent i=new Intent(FloatingService.this, MainActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        stopSelf();
                    }
                    return true;
                }
                return false;
            }
        });

        WebView wv = floatView.findViewById(R.id.floatWeb);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl("file:///android_asset/public/index.html#mini");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(floatView!=null) wm.removeView(floatView);
    }

    @Override
    public IBinder onBind(Intent i){ return null; }
    }
