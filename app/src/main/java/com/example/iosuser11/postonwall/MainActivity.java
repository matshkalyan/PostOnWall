package com.example.iosuser11.postonwall;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.widget.RelativeLayout;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class MainActivity extends Activity {
    CameraPreview cameraPreview;
    PicturePreview pictureView;
    RelativeLayout wallView;
    private boolean afterOnPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
        }
        else if(permissionCheck == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        afterOnPause = false;
        wallView = (RelativeLayout)findViewById(R.id.wallView);
        cameraPreview = new CameraPreview(getApplicationContext());
        wallView.addView(cameraPreview);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(afterOnPause)
            cameraPreview.initCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        afterOnPause = true;
        cameraPreview.pause();
    }
}
