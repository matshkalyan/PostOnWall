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
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;


import com.example.iosuser11.postonwall.GL.MyGyozalRenderer;
import com.example.iosuser11.postonwall.Network.Communicator;
import com.example.iosuser11.postonwall.Network.CommunicatorPicsArt;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
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

    public static ArrayList<PictureObject> allPicturesList;
    public static ArrayList<PictureObject> currentPicturesList;

    //UI stuff
    private FrameLayout wallView;
    private CameraPreview cameraPreview;
    private PictureView pictureView;
    private MyGyozalRenderer pictureRenderer;
    private Button post;
    private Button cancel;
    private Switch tracking;
    private SeekBar seekBar;

    //Image processing stuff
    Mat imgCurrent;
    FeatureDetector detector;
    MatOfKeyPoint keypointsCurrent;
    DescriptorExtractor descriptor;
    Mat descriptorsCurrent;
    DescriptorMatcher matcher;
    MatOfDMatch matches;

    //Sensors
    private GPSTracker gpsTracker;
    private Location currentLocation = null;

    //flags
    private boolean afterOnPause = false;
    private boolean cameraPermissionGranted = false;
    private boolean gpsPermissionGranted = false;
    private boolean trackingState = false;
    private boolean photoChosen = false;
    private boolean imageFound = false;

    //FOR SERVER
    private Communicator communicator;
    private CommunicatorPicsArt communicatorPicsArt;

    private Bitmap selectedPicture;
    private int currentPictureIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the system supports OpenGL ES 2.0 (must be done only the first time application runs)
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000
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

        //initialize OpenCV library
        OpenCVLoader.initDebug();

        //server
        communicator = new Communicator();
        communicatorPicsArt = new CommunicatorPicsArt();

        //initializing image lists
        allPicturesList = new ArrayList<PictureObject>();
        currentPicturesList = new ArrayList<PictureObject>();

        //UI stuff
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        post = (Button) findViewById(R.id.post);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setVisibility(View.GONE);
        tracking = (Switch) findViewById(R.id.tracking);
        tracking.setChecked(false);
        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setVisibility(View.GONE);
        wallView = (FrameLayout) findViewById(R.id.wallView);
        //request Camera, External memory and GPS persmissions, initialize the camera
        requestPermissions();

        //initialising orb stuff
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        keypointsCurrent = new MatOfKeyPoint();
        descriptorsCurrent = new Mat();
        matches = new MatOfDMatch();
        imgCurrent = new Mat();

        //ONCICK Listeners
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("", "onClick: text is: " + post.getText());
                if(!photoChosen) {      //the photo is not yet chosen, we clicked on the "choose photo"
                    tracking.setChecked(false);
                    chooseImage();
                    post.setText("POST");
                    photoChosen = true;
                } else {        //the photo is already chosen, we clicked on the "Post"
                    //post the already chosen picture on the wall (save it as an object containing its GPS coordinates, keypoints, descriptors, add that object to the list of objects in the global variables)
                    postImage();
                    photoChosen = false;
                    post.setText("CHOOSE IMAGE");
                    seekBar.setVisibility(View.GONE);
//                    tracking.setChecked(true);
                    pictureRenderer.startPttvel();
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
                if(isChecked) {     //we enabled tracking
                    wallView.removeView(pictureView);
                    findImagesNearby();
                    trackingState = true;
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            while(!imageFound)
                                performImageMatch();
//                            pictureRenderer.startPttvel();

                            return null;
                        }
                    }.execute();

                } else {        //we disabled tracking
                    pictureRenderer.stopPttvel();
                    trackingState = false;
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                updateDistance(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void updateDistance(int d){
        pictureRenderer.updateDistance(20-d);
    }

    private void requestPermissions() {
        ArrayList<String> permissionsToBeRequested = new ArrayList<String>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionsToBeRequested.add(Manifest.permission.CAMERA);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionsToBeRequested.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsToBeRequested.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        if(permissionsToBeRequested.size() == 0) {
            cameraPreview = new CameraPreview(getApplicationContext());
            wallView.addView(cameraPreview);
            gpsTracker = new GPSTracker(this);
        } else {
            ActivityCompat.requestPermissions(this, permissionsToBeRequested.toArray(new String[0]), 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Sorry, we need all of the permissions requested.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    cameraPreview = new CameraPreview(getApplicationContext());
                    wallView.addView(cameraPreview);
                    gpsTracker = new GPSTracker(this);
                }
                return;
            }
        }
    }

    private void chooseImage() {
        if (Build.VERSION.SDK_INT >= 23) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        }
    }

    //posts the image on the wall, gets the original image descriptors, keypoints, grv etc.
    private void postImage() {
        cancel.setVisibility(View.GONE);

        Mat imgOriginal = new Mat();
        MatOfKeyPoint keypointsOriginal = new MatOfKeyPoint();
        Mat descriptorsOriginal = new Mat();
        Location locationOriginal = null;

        byte[] data = cameraPreview.getCurrentFrame();
        Mat pre = new Mat(cameraPreview.getmPreviewSize().height+cameraPreview.getmPreviewSize().height/2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
        pre.put(0, 0, data);
        Imgproc.cvtColor(pre,imgOriginal, Imgproc.COLOR_YUV2GRAY_NV21);
        Core.transpose(imgOriginal,imgOriginal);
        Core.flip(imgOriginal,imgOriginal,1);
        detector.detect(imgOriginal,keypointsOriginal);
        descriptor.compute(imgOriginal, keypointsOriginal, descriptorsOriginal);

        assert gpsTracker!=null;

        PictureObject newPicture = new PictureObject(selectedPicture);
        newPicture.setLocation(gpsTracker.getLocation());
        newPicture.setKeypoints(keypointsOriginal);
        newPicture.setDescriptors(descriptorsOriginal);

        allPicturesList.add(newPicture);
    }

    private void findImagesNearby() {
        //filtering all the pictureobjects having location near our current location
        Location originalLocation;
        currentLocation = gpsTracker.getLocation();
        for(int i = 0; i < allPicturesList.size(); i++) {
            originalLocation = allPicturesList.get(i).getLocation();
            if((currentLocation.getLatitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLatitude())&&
                    (currentLocation.getLatitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLatitude())){
                if((currentLocation.getLongitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLongitude())&&
                        (currentLocation.getLongitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLongitude())) {
                    currentPicturesList.add(allPicturesList.get(i));
                }
            }
        }
    }

    void performImageMatch(){
        byte[] data = cameraPreview.getCurrentFrame();
        Mat pre = new Mat(cameraPreview.getmPreviewSize().height+cameraPreview.getmPreviewSize().height/2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
        pre.put(0, 0, data);
        Imgproc.cvtColor(pre,imgCurrent, Imgproc.COLOR_YUV2GRAY_NV21);
        Core.transpose(imgCurrent,imgCurrent);
        Core.flip(imgCurrent,imgCurrent,1);
        detector.detect(imgCurrent,keypointsCurrent);
        descriptor.compute(imgCurrent, keypointsCurrent, descriptorsCurrent);

        Mat descriptorsOriginal;
        MatOfKeyPoint keypointsOriginal;
        for(int i = 0; i < currentPicturesList.size(); i++) {
            descriptorsOriginal = currentPicturesList.get(i).getDescriptors();
            keypointsOriginal = currentPicturesList.get(i).getKeypoints();

            matcher.match(descriptorsCurrent,descriptorsOriginal,matches);
            List<DMatch> matchesList = matches.toList();
            List<DMatch> matches_final= new ArrayList<DMatch>();
            for(int j = 0; j < matchesList.size(); j++) {
                if (matchesList.get(j).distance <= 40) {
                    matches_final.add(matches.toList().get(j));
                }
            }

            if (matches_final.size() > 4) {
                //we found the image on the wall currently being captured
                imageFound = true;
                currentPictureIndex = i;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pictureView = new PictureView(getApplicationContext(), cameraPreview.getmPreviewSize().width, cameraPreview.getmPreviewSize().height);
                        pictureRenderer = new MyGyozalRenderer(MainActivity.this, currentPicturesList.get(currentPictureIndex).getPicture());
                        pictureView.setEGLContextClientVersion(2);
                        pictureView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                        pictureView.setEGLConfigChooser(8,8,8,8,0,0);
                        pictureView.setZOrderOnTop(true);
                        pictureView.setRenderer(pictureRenderer);
                        wallView.addView(pictureView);
                        pictureView.bringToFront();
                        pictureRenderer.startPttvel();
                    }
                });



//                List<Point> objpoints = new ArrayList<Point>();
//                List<Point> scenepoints = new ArrayList<Point>();
//                List<KeyPoint> keys1 = keypointsOriginal.toList();
//                List<KeyPoint> keys2 = keypointsCurrent.toList();
//                for(int j = 0; j < matches_final.size(); j++) {
//                    objpoints.add(keys1.get((matches_final.get(j)).queryIdx).pt);
//                    scenepoints.add(keys2.get((matches_final.get(j)).trainIdx).pt);
//                }
//                MatOfPoint2f obj = new MatOfPoint2f();
//                obj.fromList(objpoints);
//                MatOfPoint2f scene = new MatOfPoint2f();
//                scene.fromList(scenepoints);
//
//                Mat affine = Imgproc.getAffineTransform(obj,scene);
//                Matrix transformMat = new Matrix();
//                transformMat.setTranslate((float) affine.get(0,2)[0],(float) affine.get(1,2)[0]);
//                pictureView.setTransformMatrix(transformMat);

                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    cancel.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);
                    final Uri imageUri = data.getData();
                    final InputStream imageStream;
                    wallView.removeView(pictureView);
                    cameraPreview.initCamera();

                    pictureView = new PictureView(getApplicationContext(), cameraPreview.getmPreviewSize().width, cameraPreview.getmPreviewSize().height);
                    pictureView.setEGLContextClientVersion(2);
                    pictureView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                    pictureView.setEGLConfigChooser(8,8,8,8,0,0);
                    pictureView.setZOrderOnTop(true);
                    wallView.addView(pictureView);
                    pictureView.bringToFront();
                    try {
                        imageStream = getContentResolver().openInputStream(imageUri);
                        selectedPicture = BitmapFactory.decodeStream(imageStream);
                        pictureRenderer = new MyGyozalRenderer(this, selectedPicture);
                        pictureView.setRenderer(pictureRenderer);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    cameraPreview.initCamera();
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (afterOnPause && cameraPermissionGranted) {
            cameraPreview.initCamera();
//            pictureView = new PictureView(getApplicationContext(), cameraPreview.getmPreviewSize().width, cameraPreview.getmPreviewSize().height);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        afterOnPause = true;
        if (cameraPermissionGranted)
            cameraPreview.pause();
    }
}