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
                320, 320,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.x = 100;
        params.y = 400;

        // --- WHICH SOLID DID YOU LEAVE IT ON? ---
        String solid = getSharedPreferences("wave", MODE_PRIVATE).getString("selected_solid", "icosahedron");

        // --- THE LIVE ANIMATION ITSELF, NOT A PICTURE ---
        WebView web = bubble.findViewById(R.id.crystalWeb);
        web.getSettings().setJavaScriptEnabled(true);
        web.setBackgroundColor(Color.TRANSPARENT);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        String html = "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<style>body{margin:0;background:transparent;overflow:hidden;}canvas{display:block;}</style>"
                + "<script src='https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js'></script>"
                + "</head><body><script>"
                + "var scene=new THREE.Scene();"
                + "var camera=new THREE.PerspectiveCamera(60,1,0.1,100);camera.position.z=3;"
                + "var renderer=new THREE.WebGLRenderer({alpha:true,antialias:true});"
                + "renderer.setSize(320,320);renderer.setClearColor(0x000000,0);"
                + "document.body.appendChild(renderer.domElement);"
                + "var geo;"
                + "var solidName='" + solid + "';"
                + "if(solidName=='tetrahedron') geo=new THREE.TetrahedronGeometry(1.2);"
                + "else if(solidName=='cube') geo=new THREE.BoxGeometry(1.6,1.6,1.6);"
                + "else if(solidName=='octahedron') geo=new THREE.OctahedronGeometry(1.3);"
                + "else if(solidName=='dodecahedron') geo=new THREE.DodecahedronGeometry(1.2);"
                + "else geo=new THREE.IcosahedronGeometry(1.3);"
                + "var mat=new THREE.MeshStandardMaterial({color:0xffd700,metalness:0.9,roughness:0.2});"
                + "var mesh=new THREE.Mesh(geo,mat);"
                + "var wire=new THREE.LineSegments(new THREE.WireframeGeometry(geo), new THREE.LineBasicMaterial({color:0xffffff,transparent:true,opacity:0.35}));"
                + "scene.add(mesh);scene.add(wire);"
                + "var light=new THREE.PointLight(0xffffff,1.5);light.position.set(3,3,3);scene.add(light);"
                + "var amb=new THREE.AmbientLight(0x888888);scene.add(amb);"
                + "function animate(){requestAnimationFrame(animate);mesh.rotation.y+=0.02;mesh.rotation.x+=0.005;wire.rotation.copy(mesh.rotation);renderer.render(scene,camera);}"
                + "animate();"
                + "</script></body></html>";

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
