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
import android.graphics.Matrix;
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
import android.widget.TextView;
import android.widget.Toast;


import com.example.iosuser11.postonwall.GL.PictureRenderer;
import com.example.iosuser11.postonwall.Network.Communicator;
import com.example.iosuser11.postonwall.Network.CommunicatorPicsArt;

import org.opencv.android.OpenCVLoader;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
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

    public static ArrayList<PictureObject> allPicturesList;     //all of the pictures posted via this app, must be in the server in future
    public ArrayList<PictureObject> nearbyPicturesList;      //pictures posted nearby, is reinitialized ever time we enable tracking
    public ArrayList<PicViewAndRend> currentPicturesList;     //pictures currently being tracked, is reinitialized ever time we enable tracking
    public ArrayList currentPicturesIndexesList;     //indexes of the pictures currently being tracked in the list of the nearbyPicturesList, is reinitialized ever time we enable tracking

    //UI stuff
    private FrameLayout wallView;
    private CameraPreview cameraPreview;
    private PictureView pictureView;
    private PictureRenderer pictureRenderer;
    private Button post;
    private Button cancel;
    private Switch tracking;
    private SeekBar seekBar;
    private TextView messages;

    //Image processing stuff
    Mat imgCurrent;
    Mat pre;
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    DescriptorMatcher matcher;
    MatOfKeyPoint keyPointsOriginal;
    MatOfKeyPoint keypointsPrevious;
    MatOfKeyPoint keypointsCurrent;
    Mat descriptorsOriginal;
    Mat descriptorsPrevious;
    Mat descriptorsCurrent;
    MatOfDMatch matches;
    MatOfDMatch successiveMatches;

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
    private boolean newPictureAdded = false;

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
        allPicturesList = new ArrayList();      //should get the pictureobjects from the server

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
        messages = (TextView)findViewById(R.id.messages);
        messages.setVisibility(View.GONE);
        wallView = (FrameLayout) findViewById(R.id.wallView);
        requestPermissions();   //request Camera, External memory and GPS persmissions, initialize the camera, must be improved

        //initialising orb stuff
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);;
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        imgCurrent = new Mat();
        keypointsCurrent = new MatOfKeyPoint();
        descriptorsCurrent = new Mat();
        matches = new MatOfDMatch();

        //Listeners
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!photoChosen) {
//                    tracking.setChecked(false);
                    chooseImage();
                } else {
                    postImage();    //save it as an object containing its GPS coordinates, keypoints, descriptors, add that object to the list of objects

                    wallView.removeView(pictureView);
                    tracking.setChecked(true);

                    post.setText("CHOOSE IMAGE");
                    photoChosen = false;
                    seekBar.setVisibility(View.GONE);
                    pictureRenderer.attachToWall();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wallView.removeView(pictureView);
                photoChosen = false;
                post.setText("CHOOSE IMAGE");
                cancel.setVisibility(View.GONE);
                seekBar.setVisibility(View.GONE);
            }
        });

        tracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {     //we enabled tracking
                    findImagesNearby();
                    currentPicturesList = new ArrayList<PicViewAndRend>();
                    currentPicturesIndexesList = new ArrayList();
                    messages.setVisibility(View.VISIBLE);
                    messages.setText("There are " + nearbyPicturesList.size() + " / " + allPicturesList.size() + " pictures posted nearby.");
                    trackingState = true;
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            tracking();
                            return null;
                        }
                    }.execute();
                } else {        //we disabled tracking
                    trackingState = false;
                    for(int i = 0; i<currentPicturesList.size(); i++) {
                        wallView.removeView(currentPicturesList.get(i).getView());
                    }
                    messages.setVisibility(View.GONE);
                    imageFound = false;
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                pictureRenderer.updateDistance(20-i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
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
            pre = new Mat(cameraPreview.getmPreviewSize().height+cameraPreview.getmPreviewSize().height/2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
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
                    pre = new Mat(cameraPreview.getmPreviewSize().height+cameraPreview.getmPreviewSize().height/2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    photoChosen = true;
                    cancel.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);
                    seekBar.setProgress(seekBar.getMax()/2);
                    post.setText("POST");

                    final Uri imageUri = data.getData();
                    final InputStream imageStream;
                    try {
                        imageStream = getContentResolver().openInputStream(imageUri);
                        selectedPicture = BitmapFactory.decodeStream(imageStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    pictureView = new PictureView(getApplicationContext(), cameraPreview.getmPreviewSize().width, cameraPreview.getmPreviewSize().height);
                    pictureView.setEGLContextClientVersion(2);
                    pictureView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                    pictureView.setEGLConfigChooser(8,8,8,8,0,0);
                    pictureView.setZOrderOnTop(true);
                    pictureRenderer = new PictureRenderer(this, selectedPicture);
                    pictureView.setRenderer(pictureRenderer);
                    wallView.addView(pictureView);
                    pictureView.bringToFront();
                }
        }
    }

    private void postImage() {
        cancel.setVisibility(View.GONE);

        Mat imgCurrent = new Mat();
        MatOfKeyPoint keypointsCurrent = new MatOfKeyPoint();
        Mat descriptorsCurrent = new Mat();

        getCurrentCameraFrame(imgCurrent);
        detector.detect(imgCurrent,keypointsCurrent);
        descriptor.compute(imgCurrent, keypointsCurrent, descriptorsCurrent);

        PictureObject newPicture = new PictureObject(selectedPicture);
        newPicture.setLocation(gpsTracker.getLocation());
        newPicture.setKeypoints(keypointsCurrent);
        newPicture.setDescriptors(descriptorsCurrent);
        newPicture.setScale(seekBar.getProgress());
        Log.d("", "postImage: seekbar progress is: ");

        allPicturesList.add(newPicture);
        findImagesNearby();
    }

    private void findImagesNearby() {
        nearbyPicturesList = new ArrayList<>();
        Location originalLocation;
        currentLocation = gpsTracker.getLocation();
        for(int i = 0; i < allPicturesList.size(); i++) {
            originalLocation = allPicturesList.get(i).getLocation();
            if((currentLocation.getLatitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLatitude())&&
                    (currentLocation.getLatitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLatitude())){
                if((currentLocation.getLongitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLongitude())&&
                        (currentLocation.getLongitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLongitude())) {
                    nearbyPicturesList.add(allPicturesList.get(i));
                }
            }
        }
    }

    void getCurrentCameraFrame(Mat imgCurrent) {
        byte[] data = cameraPreview.getCurrentFrame();
        pre.put(0, 0, data);
        Imgproc.cvtColor(pre, imgCurrent, Imgproc.COLOR_YUV2GRAY_NV21);
        Core.transpose(imgCurrent, imgCurrent);
        Core.flip(imgCurrent, imgCurrent, 1);
    }

    void tracking() {
        while(trackingState) {
            trackCurrentPictures();
            findPictureOnCurrentWall();
        }
    }

    void trackCurrentPictures() {
        //
        getCurrentCameraFrame(imgCurrent);

        if(keypointsCurrent == null || descriptorsCurrent == null) {
            detector.detect(imgCurrent, keypointsPrevious);
            descriptor.compute(imgCurrent, keypointsPrevious, descriptorsPrevious);
        } else {
            keypointsPrevious = keypointsCurrent;
            descriptorsPrevious = descriptorsCurrent;
        }
        detector.detect(imgCurrent, keypointsCurrent);
        descriptor.compute(imgCurrent, keypointsCurrent, descriptorsCurrent);

        //match current frame with the previous frame to compute homography and update all of the picures in the currentPicturesList
        if(currentPicturesList.size() > 0) {
            successiveMatches = new MatOfDMatch();
            matcher.match(descriptorsCurrent, descriptorsPrevious, successiveMatches);
            List<DMatch> matchesList2 = successiveMatches.toList();
            List<DMatch> matches_final2 = new ArrayList<>();
            for(int j = 0; j < matchesList2.size(); j++) {
                if (matchesList2.get(j).distance <= 40) {
                    matches_final2.add(successiveMatches.toList().get(j));
                }
            }
            List<Point> objpoints = new ArrayList<Point>();
            List<Point> scenepoints = new ArrayList<Point>();
            for(int i=0; i < matches_final2.size(); i++) {
                objpoints.add(keypointsPrevious.toList().get((matches_final2.get(i)).queryIdx).pt);
                scenepoints.add(keypointsCurrent.toList().get((matches_final2.get(i)).trainIdx).pt);
            }
            MatOfPoint2f obj = new MatOfPoint2f();
            obj.fromList(objpoints);
            MatOfPoint2f scene = new MatOfPoint2f();
            scene.fromList(scenepoints);
            Mat homography = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 0.1);
            Log.d("", "translation is: " + homography.get(0, 2)[0] + homography.get(1, 2)[0]);
            for(int i = 0; i<currentPicturesList.size(); i++) {
                currentPicturesList.get(i).getRenderer().setTranslation(homography.get(0, 2)[0], homography.get(1, 2)[0]);

//                currentPicturesList.get(i).getView().setTranslationX(1 * (float) homography.get(0, 2)[0]);
//                currentPicturesList.get(i).getView().setTranslationY(1 * (float) homography.get(1, 2)[0]);
            }
        }
    }

    void findPictureOnCurrentWall(){
        //every cycle of this while loop tries to find a picture posted on the wall being viewed at the time of this cycle
//        while(trackingState) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            getCurrentCameraFrame(imgCurrent);
//            detector.detect(imgCurrent, keypointsCurrent);
//            descriptor.compute(imgCurrent, keypointsCurrent, descriptorsCurrent);
            //current descriptors are already computed in the trackCurrentPictures()

            //match current frame with the frames of the nearby pictures,,, here only original keypoints/descriptors are computed
            for(int i = 0; i < nearbyPicturesList.size() && !imageFound; i++) {
                //take descriptors of the wall of picture[i], match it with the wall being currently displayed
                descriptorsOriginal = nearbyPicturesList.get(i).getDescriptors();
                matcher.match(descriptorsCurrent, descriptorsOriginal, matches);
                List<DMatch> matchesList = matches.toList();
                List<DMatch> matches_final= new ArrayList<>();
                for(int j = 0; j < matchesList.size(); j++) {
                    if (matchesList.get(j).distance <= 40) {
                        matches_final.add(matches.toList().get(j));
                    }
                }
                if (matches_final.size() > 200) {
                    imageFound = true;
                    currentPictureIndex = i;
                }
                if(imageFound && !pictureIsViewed(currentPictureIndex)) {
                    currentPicturesIndexesList.add(currentPictureIndex);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newTrackedPictureView(nearbyPicturesList.get(currentPictureIndex).getPicture());
                            currentPicturesList.get(currentPicturesList.size() - 1).getRenderer().updateDistance(20- nearbyPicturesList.get(currentPictureIndex).getScale());
                        }
                    });
                }
                if(newPictureAdded)
                    currentPicturesList.get(currentPicturesList.size() - 1).getRenderer().attachToWall();
            }
            imageFound = false;
            newPictureAdded = false;
//        }
    }

    boolean pictureIsViewed(int index) {
        for(int i = 0; i < currentPicturesIndexesList.size(); i++) {
            if(currentPicturesIndexesList.get(i) == index)
                return true;
        }
        return false;
    }

    void newTrackedPictureView(Bitmap image) {
        PictureView pView = new PictureView(getApplicationContext(), cameraPreview.getmPreviewSize().width, cameraPreview.getmPreviewSize().height);
        pView.setEGLContextClientVersion(2);
        pView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        pView.setEGLConfigChooser(8,8,8,8,0,0);
        pView.setZOrderMediaOverlay(true);
        PictureRenderer pRenderer = new PictureRenderer(this, image);
        pView.setRenderer(pRenderer);
        wallView.addView(pView);
        pView.bringToFront();

        PicViewAndRend picViewAndRend = new PicViewAndRend(pView, pRenderer);
        currentPicturesList.add(picViewAndRend);
        newPictureAdded = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (afterOnPause) {
            cameraPreview.initCamera();
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