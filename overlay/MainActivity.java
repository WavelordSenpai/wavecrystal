package com.wavecrystal.app;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
  @Override protected void onCreate(Bundle s){
    super.onCreate(s);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){
      startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
    }
  }
  @Override protected void onPause(){
    super.onPause();
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)){
      startService(new Intent(this, FloatingService.class));
    }
  }
  @Override protected void onResume(){
    super.onResume();
    stopService(new Intent(this, FloatingService.class));
  }
}
