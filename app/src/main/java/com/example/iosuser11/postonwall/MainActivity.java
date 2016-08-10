package com.example.iosuser11.postonwall;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.RelativeLayout;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class MainActivity extends Activity {
    CameraPreview cameraPreview;
    PicturePreview pictureView;
    RelativeLayout wallView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        wallView = (RelativeLayout)findViewById(R.id.wallView);
        cameraPreview = new CameraPreview(getApplicationContext());
        wallView.addView(cameraPreview);

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraPreview.init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.pause();
    }
}
