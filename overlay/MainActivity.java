package com.wavecrystal.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import com.getcapacitor.BridgeActivity;
import java.util.Locale;

public class MainActivity extends BridgeActivity {
    private static final int OVERLAY_REQUEST = 1234;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_REQUEST);
        }
        getSharedPreferences("wave", MODE_PRIVATE).edit().putBoolean("mini", false).apply();

        // Init Android TTS once
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.95f);
            }
        });
    }

    public void speakText(String text, String langCode) {
        if (tts == null || text == null || text.isEmpty()) return;
        try {
            Locale loc = Locale.US;
            if (langCode != null) {
                if (langCode.startsWith("es")) loc = new Locale("es", "ES");
                else if (langCode.startsWith("it")) loc = new Locale("it", "IT");
                else if (langCode.startsWith("fr")) loc = new Locale("fr", "FR");
                else if (langCode.startsWith("pt")) loc = new Locale("pt", "PT");
                else if (langCode.startsWith("de")) loc = new Locale("de", "DE");
                else if (langCode.startsWith("ja")) loc = new Locale("ja", "JP");
                else if (langCode.startsWith("hi")) loc = new Locale("hi", "IN");
            }
            tts.setLanguage(loc);
            tts.stop();
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wave-tts");
        } catch (Exception e) {}
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
        @JavascriptInterface
        public void speak(String text) {
            speakText(text, "en-US");
        }
        @JavascriptInterface
        public void speakLang(String text, String langCode) {
            speakText(text, langCode);
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

    @Override
    protected void onDestroy() {
        if (tts != null) { try { tts.stop(); tts.shutdown(); } catch (Exception e) {} }
        super.onDestroy();
    }
}
