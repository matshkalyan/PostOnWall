package com.example.iosuser11.postonwall;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
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
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class MainActivity extends Activity {
    //UI stuff
    private FrameLayout wallView;
    private CameraPreview cameraPreview;
    private PicturePreview pictureView;
    private Button post;
    private Camera.Size previewSize;

    //Image processing stuff
    DescriptorMatcher matcher;
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    Mat imgOriginal, imgCurrent;
    Mat descriptorsOriginal, descriptorsCurrent;
    MatOfKeyPoint keypointsOriginal, keypointsCurrent;
    MatOfDMatch matches;

    //Sensors
    private GPSTracker mGPSTracker;
    private Location originalLocation = null;
    private Location currentLocation = null;
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

        //initialising orb stuff
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        descriptorsCurrent = new Mat();
        descriptorsOriginal = new Mat();
        keypointsCurrent = new MatOfKeyPoint();
        keypointsOriginal = new MatOfKeyPoint();
        matches = new MatOfDMatch();
        imgCurrent = new Mat();
        imgOriginal = new Mat();

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wallCoords = grvCoords.getValues();
                Log.d("", "onClick: GRV coords are: "+wallCoords[0]+" "+wallCoords[1]+" "+wallCoords[2]);
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        capture();
                        findImages();
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

    //posts the image on the wall, gets the original image descriptors, keypoints, grv etc.
    //TODO:  upload to server
    private void capture(){
        previewSize = cameraPreview.getmPreviewSize();
        byte[] data = cameraPreview.getCurrentFrame();
        Mat pre = new Mat(previewSize.height+previewSize.height/2, previewSize.width, CvType.CV_8UC1);
        pre.put(0, 0, data);
        Imgproc.cvtColor(pre,imgOriginal, Imgproc.COLOR_YUV2GRAY_NV21);
        Core.transpose(imgOriginal,imgOriginal);
        Core.flip(imgOriginal,imgOriginal,1);
        detector.detect(imgOriginal,keypointsOriginal);
        descriptor.compute(imgOriginal, keypointsOriginal, descriptorsOriginal);


        //grv coords of the original image
        currentCoords = grvCoords.getValues();

        //GPS coords of the original image
        if(mGPSTracker.canGetLocation()){
            originalLocation = mGPSTracker.getLocation();
        }
        else{
            //shows GPS  Settongs for the user to enable GPS
            mGPSTracker.showSettingsAlert();
        }
    }

    private void findImages(){
        while (true) {
            //gets current location to compare with original image
            if(mGPSTracker.canGetLocation()){
                currentLocation = mGPSTracker.getLocation();
            }
            else{
                Toast.makeText(this.getApplicationContext(),"dfuq, no location!", Toast.LENGTH_LONG).show();
                //shows GPS  Settongs for the user to enable GPS
                mGPSTracker.showSettingsAlert();
            }

            //checks for gps location converts accuracy meters to lat/lon
            if((currentLocation.getLatitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLatitude())&&
                    (currentLocation.getLatitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLatitude())){
                if((currentLocation.getLongitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLongitude())&&
                        (currentLocation.getLongitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLongitude())) {
                    //compares grv coordinates
                    if(Math.abs((currentCoords[1] + 0.7) - (wallCoords[1] + 0.7)) > 0.04
                            && Math.abs((currentCoords[1] + 0.7) - (wallCoords[1] + 0.7)) <= 0.09) {
                        Log.d("", "close enuff, start tracking");
//                            startTracking();
                        if(performImageMatch())
                            startTracking();
                    }
                    else {
                        Log.d("", "aint enough,   FarAway");
                    }


                }
            }


        }
    }
    boolean performImageMatch(){
        previewSize = cameraPreview.getmPreviewSize();
        byte[] data = cameraPreview.getCurrentFrame();
        Mat pre = new Mat(previewSize.height+previewSize.height/2, previewSize.width, CvType.CV_8UC1);
        pre.put(0, 0, data);
        Imgproc.cvtColor(pre,imgCurrent, Imgproc.COLOR_YUV2GRAY_NV21);
        Core.transpose(imgCurrent,imgCurrent);
        Core.flip(imgCurrent,imgCurrent,1);
        detector.detect(imgCurrent,keypointsCurrent);
        descriptor.compute(imgCurrent, keypointsCurrent, descriptorsCurrent);

        matcher.match(descriptorsCurrent,descriptorsOriginal,matches);
        List<DMatch> matchesList = matches.toList();
        List<DMatch> matches_final= new ArrayList<DMatch>();
        for(int i = 0; i < matchesList.size(); i++) {
            if (matchesList.get(i).distance <= 40) {
                matches_final.add(matches.toList().get(i));
            }
        }

        if (matches_final.size()>4){
            List<Point> objpoints = new ArrayList<Point>();
            List<Point> scenepoints = new ArrayList<Point>();
            List<KeyPoint> keys1 = keypointsOriginal.toList();
            List<KeyPoint> keys2 = keypointsCurrent.toList();
            for(int i=0; i < matches_final.size(); i++) {
                objpoints.add(keys1.get((matches_final.get(i)).queryIdx).pt);
                scenepoints.add(keys2.get((matches_final.get(i)).trainIdx).pt);
            }
            MatOfPoint2f obj = new MatOfPoint2f();
            obj.fromList(objpoints);
            MatOfPoint2f scene = new MatOfPoint2f();
            scene.fromList(scenepoints);

            Mat affine = Imgproc.getAffineTransform(obj,scene);
            Matrix transformMat = new Matrix();
            transformMat.setTranslate((float) affine.get(0,2)[0],(float) affine.get(1,2)[0]);
            pictureView.setTransformMatrix(transformMat);
            return true;
        }
        else
        {
            return false;
        }
    }


    void startTracking() {
        //to use grv coords to track image;

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
            mGPSTracker = new GPSTracker(this);
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