package com.example.iosuser11.postonwall;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;


import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class MainActivity extends Activity {
    //UI stuff
    private FrameLayout wallView;
    private CameraPreview cameraPreview;
    private PicturePreview pictureView;
    private Button post;

    //Image processing stuff
    DescriptorMatcher matcher;
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    Mat imgOriginal, imgCurrent;
    Mat descriptorsOriginal, descriptorsCurrent;
    MatOfKeyPoint keypointsOriginal, keypointsCurrent;

    //Sensors
    private GPSTracker gpsTracker;
    private Location originalLocation = null;
    private GRVCoordinates grvCoords;
    private float[] wallCoords;
    private float[] currentCoords;

    //flags
    private boolean afterOnPause = false;
    private boolean cameraPermissionGranted = false;
    private boolean gpsPermissionGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();

        //UI stuff
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        wallView = (FrameLayout) findViewById(R.id.wallView);
        pictureView = new PicturePreview(getApplicationContext());
//        wallView.addView(pictureView);
        post = (Button) findViewById(R.id.post);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wallCoords = grvCoords.getValues();
                Log.d("", "onClick: GRV coords are: "+wallCoords[0]+" "+wallCoords[1]+" "+wallCoords[2]);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        startTracking();
                        return null;
                    }
                }.execute();
            }
        });

        //setting up sensors
        grvCoords = new GRVCoordinates(this);

        //setting up image processing  stuff
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);

        requestCameraPermission();
        wallView.addView(pictureView);
    }

    void startTracking() {
        while (true) {
            currentCoords = grvCoords.getValues();
        }
    }

    private void requestCameraPermission() {
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (cameraPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            cameraPreview = new CameraPreview(getApplicationContext());
            wallView.addView(cameraPreview);
            pictureView.setParentSize(cameraPreview.getPreviewWidth(), cameraPreview.getPreviewHeight());
            pictureView.bringToFront();
            cameraPermissionGranted = true;
            requestGPSPermission();
        } else if (cameraPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void requestGPSPermission() {
        int gpsPermissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (gpsPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            gpsTracker = new GPSTracker(this);
            gpsPermissionGranted = true;
        } else if (gpsPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            gpsPermissionGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (afterOnPause && cameraPermissionGranted)
            cameraPreview.initCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        afterOnPause = true;
        if (cameraPermissionGranted)
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
                    pictureView.setParentSize(cameraPreview.getPreviewWidth(), cameraPreview.getPreviewHeight());
                    pictureView.bringToFront();
                    Log.d("", "onCreate: camerapreview added, coords of the wallview are: "+wallView.getPivotX()+" "+wallView.getPivotY());
                    requestGPSPermission();
                } else {
                    Toast.makeText(getApplicationContext(), "We need the camera, BYE!", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker = new GPSTracker(this);
                    gpsPermissionGranted = true;
                } else {
                    Toast.makeText(getApplicationContext(), "We need the GPS, BYE!", Toast.LENGTH_LONG).show();
                    gpsPermissionGranted = false;
                    finish();
                }
                return;
            }
        }
    }

}