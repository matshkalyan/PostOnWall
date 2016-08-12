package com.example.iosuser11.postonwall;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
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
    //
    CameraPreview cameraPreview;
    PicturePreview pictureView;
    RelativeLayout wallView;

    GPSTracker mGPSTracker;
    //ORB stuff
    DescriptorMatcher matcher;
    FeatureDetector detector;
    DescriptorExtractor descriptor;

    //original image locators

    Location originalLocation = null;

    //keyponts/desc of currently comparing images
    Mat imgOriginal, imgCurrent;
    Mat descriptorsOriginal, descriptorsCurrent;
    MatOfKeyPoint keypointsOriginal, keypointsCurrent;

    private boolean afterOnPause;
    private boolean cameraPermissionGranted = false;
    private boolean gpsPermissionGranted = false;
    private GRVCoordinates grvCoords;
    private float[] wallCoords;
    private float[] currentCoords;
    private Button post;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        OpenCVLoader.initDebug();
        afterOnPause = false;
        grvCoords = new GRVCoordinates(this);
        wallView = (RelativeLayout) findViewById(R.id.wallView);
        requestCameraPermission();
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        pictureView = new PicturePreview(getApplicationContext());
        wallView.addView(pictureView);
        post = (Button) findViewById(R.id.post);
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wallCoords = grvCoords.getValues();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        startTracking();
                        return null;
                    }
                }.execute();
            }
        });
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
            cameraPermissionGranted = true;
            requestGPSPermission();
        } else if (cameraPermissionCheck
                != PackageManager.PERMISSION_GRANTED
                ) {
            cameraPermissionGranted = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
    }

    private void requestGPSPermission() {
        int gpsPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (gpsPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            //do the gps thing
            mGPSTracker = new GPSTracker(this);
            gpsPermissionGranted = true;
        } else if (gpsPermissionCheck
                != PackageManager.PERMISSION_GRANTED
                ) {
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
                    mGPSTracker = new GPSTracker(this);
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