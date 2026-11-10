package com.wavecrystal.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If your website calls window.startFloating(), this starts the bubble
        // For now, we check the intent - if it has "float" extra, start service
        if (getIntent() != null && getIntent().getBooleanExtra("startFloat", false)) {
            startFloating();
        }
    }

    public void startFloating() {
        if (!Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(i, 123);
        } else {
            startService(new Intent(this, FloatingService.class));
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, FloatingService.class));
                moveTaskToBack(true);
            }
        }
    }
}
