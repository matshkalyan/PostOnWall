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
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class MainActivity extends Activity {
    CameraPreview cameraPreview;
    PicturePreview pictureView;
    RelativeLayout wallView;
    private boolean afterOnPause;
    private boolean cameraPermissionGranted = false;
    private boolean gpsPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        if(!OpenCVLoader.initDebug()){
            Log.d("ONCREATE", "OpenCV not loaded");
        } else {
            Log.d("ONCREATE", "OpenCV loaded");
        }

        afterOnPause = false;
        wallView = (RelativeLayout)findViewById(R.id.wallView);
        requestCameraPermission();
        pictureView = new PicturePreview(getApplicationContext());
        wallView.addView(pictureView);

    }

    private void requestCameraPermission(){
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if(cameraPermissionCheck == PackageManager.PERMISSION_GRANTED){
            cameraPreview = new CameraPreview(getApplicationContext());
            wallView.addView(cameraPreview);
            cameraPermissionGranted = true;
        }
        else if(cameraPermissionCheck
                != PackageManager.PERMISSION_GRANTED
                ){
            cameraPermissionGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void requestGPSPermission(){
        int gpsPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if(gpsPermissionCheck == PackageManager.PERMISSION_GRANTED){
            //do the gps thing
            gpsPermissionGranted = true;
        }
        else if(gpsPermissionCheck
                != PackageManager.PERMISSION_GRANTED
                ){
            gpsPermissionGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(afterOnPause && cameraPermissionGranted)
            cameraPreview.initCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        afterOnPause = true;
        if(cameraPermissionGranted)
            cameraPreview.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    cameraPreview = new CameraPreview(getApplicationContext());
                    cameraPermissionGranted = true;
                    wallView.addView(cameraPreview);
                } else {
                    Toast.makeText(getApplicationContext(),"We need the camera, BYE!", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //do gpsstuff here
                    gpsPermissionGranted = true;
                } else {
                    Toast.makeText(getApplicationContext(),"We need the GPS, BYE!", Toast.LENGTH_LONG).show();
                    cameraPermissionGranted = false;
                    finish();
                }
                return;
            }
        }
    }

}