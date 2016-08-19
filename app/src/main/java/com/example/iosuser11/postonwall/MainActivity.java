package com.example.iosuser11.postonwall;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;


import com.example.iosuser11.postonwall.GL.MyGyozalRenderer;
import com.example.iosuser11.postonwall.Network.Communicator;
import com.example.iosuser11.postonwall.Network.CommunicatorPicsArt;

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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class MainActivity extends Activity {

    public static List<PictureObject> allPicturesList;
    public static List<PictureObject> currentPicturesList;

    //UI stuff
    private FrameLayout wallView;
    private CameraPreview cameraPreview;
    private PictureView pictureView;
    private MyGyozalRenderer myGyozalRenderer;
    private Button post;
    private Button cancel;
    private Switch tracking;

    //Image processing stuff
    Mat imgOriginal, imgCurrent;
    FeatureDetector detector;
    MatOfKeyPoint keypointsOriginal, keypointsCurrent;
    DescriptorExtractor descriptor;
    Mat descriptorsOriginal, descriptorsCurrent;
    DescriptorMatcher matcher;
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
    private boolean trackingState = false;
    private boolean photoChosen = false;
    private boolean rendererSet = false;

    //FOR SERVER
    private Communicator communicator;
    private CommunicatorPicsArt communicatorPicsArt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));
        if (!supportsEs2) {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
            finish();
        }

        OpenCVLoader.initDebug();

        //server
        communicator = new Communicator();
        communicatorPicsArt = new CommunicatorPicsArt();

        //UI stuff
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        wallView = (FrameLayout) findViewById(R.id.wallView);
        post = (Button) findViewById(R.id.post);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setVisibility(View.GONE);
        tracking = (Switch) findViewById(R.id.tracking);
        tracking.setChecked(false);

        requestCameraPermission();

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

        //setting up sensors
        grvCoords = new GRVCoordinates(this);

        //setting up image processing  stuff
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);

        //ONCICK Listeners
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("", "onClick: text is: " + post.getText());
                if(!photoChosen) {
                    tracking.setChecked(false);
                    choosePhoto();
                    //choose a photo to post from the image gallery
                    post.setText("POST");
                    //start previewing the picture
                    photoChosen = true;
                } else {
                    //post the already chosen picture on the wall (save it as an object containing its GPS coordinates, keypoints, descriptors, add that object to the list of objects in the global variables)
                    post();
                    photoChosen = false;
                    post.setText("CHOOSE IMAGE");
                    tracking.setChecked(true);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wallView.removeViewAt(1);
                photoChosen = false;
                post.setText("CHOOSE IMAGE");
                cancel.setVisibility(View.GONE);
            }
        });

        tracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    trackingState = true;
                    myGyozalRenderer.startPttvel();
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            findImages();
                            return null;
                        }
                    }.execute();
                } else {
                    myGyozalRenderer.stopPttvek();
                    trackingState = false;
                }
            }
        });

        communicatorPicsArt.picsArtPictureGet();

    }

    private void choosePhoto() {
        if (Build.VERSION.SDK_INT >= 23){
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            3);

                    // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            } else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            }
        } else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        }
    }

    //posts the image on the wall, gets the original image descriptors, keypoints, grv etc.
    private void post(){
        byte[] data = cameraPreview.getCurrentFrame();
        Mat pre = new Mat(cameraPreview.getmPreviewSize().height+cameraPreview.getmPreviewSize().height/2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
        pre.put(0, 0, data);
        Imgproc.cvtColor(pre,imgOriginal, Imgproc.COLOR_YUV2GRAY_NV21);
        Core.transpose(imgOriginal,imgOriginal);
        Core.flip(imgOriginal,imgOriginal,1);
        detector.detect(imgOriginal,keypointsOriginal);
        descriptor.compute(imgOriginal, keypointsOriginal, descriptorsOriginal);


        //grv coords of the original image
        // wallCoords = grvCoords.getValues();

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
            if(!trackingState)
                break;
            currentCoords = grvCoords.getValues();

            //gets current location to compare with original image
            if(mGPSTracker.canGetLocation()){
                currentLocation = mGPSTracker.getLocation();
            }
            else{
                Toast.makeText(this.getApplicationContext(),"dfuq, no location!", Toast.LENGTH_LONG).show();
                mGPSTracker.showSettingsAlert();
            }

            //checks for gps location converts accuracy meters to lat/lon
//            if((currentLocation.getLatitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLatitude())&&
//                    (currentLocation.getLatitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLatitude())){
//                if((currentLocation.getLongitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLongitude())&&
//                        (currentLocation.getLongitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLongitude())) {
                    Log.d("", "close enuff, start tracking");
                    if(performImageMatch())
                        startTracking();
//                }
//            }
        }
    }

    boolean performImageMatch(){
        byte[] data = cameraPreview.getCurrentFrame();
        Mat pre = new Mat(cameraPreview.getmPreviewSize().height+cameraPreview.getmPreviewSize().height/2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
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

//        if (matches_final.size()>4){
//            List<Point> objpoints = new ArrayList<Point>();
//            List<Point> scenepoints = new ArrayList<Point>();
//            List<KeyPoint> keys1 = keypointsOriginal.toList();
//            List<KeyPoint> keys2 = keypointsCurrent.toList();
//            for(int i=0; i < matches_final.size(); i++) {
//                objpoints.add(keys1.get((matches_final.get(i)).queryIdx).pt);
//                scenepoints.add(keys2.get((matches_final.get(i)).trainIdx).pt);
//            }
//            MatOfPoint2f obj = new MatOfPoint2f();
//            obj.fromList(objpoints);
//            MatOfPoint2f scene = new MatOfPoint2f();
//            scene.fromList(scenepoints);

//            Mat affine = Imgproc.getAffineTransform(obj,scene);
//            Matrix transformMat = new Matrix();
//            transformMat.setTranslate((float) affine.get(0,2)[0],(float) affine.get(1,2)[0]);
//            pictureView.setTransformMatrix(transformMat);
            return true;
//        }
//        else {
//            return false;
//        }
    }

    void startTracking() {

//        communicator.pictureGet(picture.getCoordinates(), picture.getDistance());


//        mRenderer.startTracking();
        while (true) {
            if (!trackingState)
                break;
            byte[] data = cameraPreview.getCurrentFrame();
            Mat pre = new Mat(cameraPreview.getmPreviewSize().height + cameraPreview.getmPreviewSize().height / 2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
            pre.put(0, 0, data);
            Imgproc.cvtColor(pre, imgCurrent, Imgproc.COLOR_YUV2GRAY_NV21);
            Core.transpose(imgCurrent, imgCurrent);
            Core.flip(imgCurrent, imgCurrent, 1);
            detector.detect(imgCurrent, keypointsCurrent);
            descriptor.compute(imgCurrent, keypointsCurrent, descriptorsCurrent);
            matcher.match(descriptorsCurrent, descriptorsOriginal, matches);
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final = new ArrayList<DMatch>();
            for (int i = 0; i < matchesList.size(); i++) {
                if (matchesList.get(i).distance <= 40) {
                    matches_final.add(matches.toList().get(i));
                }
            }

//            if (matches_final.size() > 10) {
//                List<Point> objpoints = new ArrayList<Point>();
//                List<Point> scenepoints = new ArrayList<Point>();
//                List<KeyPoint> keys1 = keypointsOriginal.toList();
//                List<KeyPoint> keys2 = keypointsCurrent.toList();
//                for (int i = 0; i < matches_final.size(); i++) {
//                    objpoints.add(keys1.get((matches_final.get(i)).queryIdx).pt);
//                    scenepoints.add(keys2.get((matches_final.get(i)).trainIdx).pt);
//                }
//                MatOfPoint2f obj = new MatOfPoint2f();
//                obj.fromList(objpoints);
//                MatOfPoint2f scene = new MatOfPoint2f();
//                scene.fromList(scenepoints);

//                Mat affine = Imgproc.getAffineTransform(obj, scene);
//                Point translate = new Point((float) affine.get(0, 2)[0], (float) affine.get(1, 2)[0]);

                //use this to translate image


//
//            }
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (afterOnPause && cameraPermissionGranted) {
            cameraPreview.initCamera();
            pictureView = new PictureView(getApplicationContext(), cameraPreview.getmPreviewSize().width, cameraPreview.getmPreviewSize().height);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        afterOnPause = true;
        if (cameraPermissionGranted)
            cameraPreview.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    cameraPreview = new CameraPreview(getApplicationContext());
                    cameraPermissionGranted = true;
                    wallView.addView(cameraPreview);
                    Log.d("", "onCreate: camerapreview added, coords of the wallview are: "+wallView.getPivotX()+" "+wallView.getPivotY());
                    requestGPSPermission();
                } else {
                    Toast.makeText(getApplicationContext(), "We need the camera, BYE!", Toast.LENGTH_LONG).show();
                    finish();
                }
                return;
            }
            case 2: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGPSTracker = new GPSTracker(this);
                    gpsPermissionGranted = true;
                } else {
                    Toast.makeText(getApplicationContext(), "We need the GPS, BYE!", Toast.LENGTH_LONG).show();
                    gpsPermissionGranted = false;
                    finish();
                }
                return;
            }
            case 3: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 1);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cancel.setVisibility(View.VISIBLE);

                    final Uri imageUri = data.getData();
                    final InputStream imageStream;
                    wallView.removeView(pictureView);
//                    pictureView = new PictureView(getApplicationContext(), cameraPreview.getmPreviewSize().width, cameraPreview.getmPreviewSize().height);
                    // Request an OpenGL ES 2.0 compatible context.
                    pictureView.setEGLContextClientVersion(2);
                    // Assign our renderer.
                    pictureView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                    pictureView.setEGLConfigChooser(8,8,8,8,0,0);
                    pictureView.setZOrderOnTop(true);
                    rendererSet = true;

                    wallView.addView(pictureView);
                    pictureView.setZOrderOnTop(true);
                    pictureView.bringToFront();
                    try {
                        imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        myGyozalRenderer = new MyGyozalRenderer(this, selectedImage);
                        pictureView.setRenderer(myGyozalRenderer);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}