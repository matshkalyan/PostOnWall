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
import android.location.Location;

import com.example.iosuser11.postonwall.GL.PictureRenderer;
import com.example.iosuser11.postonwall.Network.Communicator;
import com.example.iosuser11.postonwall.Network.CommunicatorPicsArt;
import com.example.iosuser11.postonwall.ServerModels.PicsArtPicturesObject;
import com.example.iosuser11.postonwall.ServerModels.Picture;
import com.example.iosuser11.postonwall.ServerModels.PictureObject;


import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class MainActivity extends Activity {

    public static ArrayList<NormalPictureObject> allPicturesList;
        public static ArrayList<PicViewAndRend> currentPicturesList;
    public static ArrayList currentPicturesIndexesList;
    private static PictureObject picObject;
    private static PicsArtPicturesObject picsObject;
    public ArrayList<NormalPictureObject> nearbyPicturesList;



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
    MatOfKeyPoint keypointsCurrent;
    DescriptorExtractor descriptor;
    Mat descriptorsCurrent;
    DescriptorMatcher matcher;
    MatOfDMatch matches;

    //Sensors
    private GPSTracker gpsTracker;
    private android.location.Location currentLocation;

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
    private Picture picture;

    private double longitude;
    private double latitude;
    private String currentCoordinates;
    private double distance = 20.0;

    private Bitmap selectedPicture;
    private int currentPictureIndex;
    byte[] data;
    NormalPictureObject nPO;


    String url;
    String id;
    private static boolean getFinished = false;
    private boolean newPictureAdded = false;

    public static boolean setGetFinished(boolean getFinished) {
        MainActivity.getFinished = getFinished;
        return getFinished;
    }


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
        //     allPicturesList = new ArrayList();
        currentPicturesIndexesList = new ArrayList();
        currentPicturesList = new ArrayList<PicViewAndRend>();     //is initialized every time we search for nearby images (every time we enable tracking)


        //UI stuff
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        post = (Button) findViewById(R.id.post);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setVisibility(View.GONE);
        tracking = (Switch) findViewById(R.id.tracking);
        tracking.setChecked(false);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setVisibility(View.GONE);
        messages = (TextView) findViewById(R.id.messages);
        messages.setVisibility(View.GONE);
        wallView = (FrameLayout) findViewById(R.id.wallView);
        requestPermissions();   //request Camera, External memory and GPS persmissions, initialize the camera, must be improved

        //initialising orb stuff
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        imgCurrent = new Mat();
        keypointsCurrent = new MatOfKeyPoint();
        descriptorsCurrent = new Mat();
        matches = new MatOfDMatch();

        //Listeners
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!photoChosen) {
                   // tracking.setChecked(false); //////////////
                    chooseImage();
                } else {
                    postImage();    //save it as an object containing its GPS coordinates, keypoints, descriptors, add that object to the list of objects
                    wallView.removeView(pictureView);
                    tracking.setChecked(false);
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
                if (isChecked) {     //we enabled tracking
                    findImagesNearby();
                    currentPicturesList = new ArrayList<PicViewAndRend>();
                    currentPicturesIndexesList = new ArrayList();
                    messages.setVisibility(View.VISIBLE);
                    //messages.setText("There are " + nearbyPicturesList.size() +" pictures posted nearby.");
                    trackingState = true;
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            while (!communicator.isFinished() || picObject == null) {
                      //  System.out.println("NOT! NOT!");
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messages.setText("There are " + picObject.getPhotos().size() +" pictures posted nearby.");
                                }
                            });

                            makeNormalObjectsList();
                            communicator.setFinished(false);

                           performImageMatch();
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
                pictureRenderer.updateDistance(20 - i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

           communicatorPicsArt.picsArtPictureGet();


    }




    private void requestPermissions() {
        ArrayList<String> permissionsToBeRequested = new ArrayList<String>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            permissionsToBeRequested.add(Manifest.permission.CAMERA);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionsToBeRequested.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            permissionsToBeRequested.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionsToBeRequested.size() == 0) {
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
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
//                 Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                 photoPickerIntent.setType("image/*");
//                startActivityForResult(photoPickerIntent, 1);


            Intent intent = new Intent(MainActivity.this, PicsArtActivity.class);
            startActivityForResult(intent, 1);
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
                    seekBar.setProgress(seekBar.getMax() / 2);
                    post.setText("POST");

//                    final Uri imageUri = data.getData();
//                    final InputStream imageStream;
//                    try {
//                        imageStream = getContentResolver().openInputStream(imageUri);
//                        selectedPicture = BitmapFactory.decodeStream(imageStream);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                        e.printStackTrace();
//                    }

                    url = data.getStringExtra("url");
                    id = data.getStringExtra("id");
//                    System.out.println("MY URL IS : " + url);
//                    System.out.println("MY ID IS: " + id);



                    String path = data.getStringExtra("filePath");
                    selectedPicture = BitmapFactory.decodeFile(path);

                    System.out.println(path);
                    System.out.println(path);

                    createImageView(selectedPicture);       //we updated the pictureView and pictureRenderer to point to the newly added view and its renderer




                }
        }
    }

    private void postImage() {
        cancel.setVisibility(View.GONE);

//        Mat imgCurrent = new Mat();
//        MatOfKeyPoint keypointsCurrent = new MatOfKeyPoint();
//        Mat descriptorsCurrent = new Mat();

        data = cameraPreview.getCurrentFrame();
        Mat pre = new Mat(cameraPreview.getmPreviewSize().height + cameraPreview.getmPreviewSize().height / 2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
        pre.put(0, 0, data);
        Imgproc.cvtColor(pre, imgCurrent, Imgproc.COLOR_YUV2GRAY_NV21);
        Core.transpose(imgCurrent, imgCurrent);
        Core.flip(imgCurrent, imgCurrent, 1);
        detector.detect(imgCurrent, keypointsCurrent);
        descriptor.compute(imgCurrent, keypointsCurrent, descriptorsCurrent);

        NormalPictureObject normPicObject = new NormalPictureObject(selectedPicture);
        normPicObject.setLocation(gpsTracker.getLocation());
        normPicObject.setKeypoints(keypointsCurrent);
        normPicObject.setDescriptors(descriptorsCurrent);
        normPicObject.setScale(seekBar.getProgress());
        Log.d("", "postImage: seekbar progress is: ");

//        PictureObject newPicture = new PictureObject(selectedPicture);
//        newPicture.setLocation(gpsTracker.getLocation());
//        newPicture.setKeypoints(keypointsCurrent);
//        newPicture.setDescriptors(descriptorsCurrent);
//        newPicture.setScale(seekBar.getProgress());
//        Log.d("", "postImage: seekbar progress is: ");
//
//        allPicturesList.add(newPicture);

        //take GRV coordinates
            // grvCapturedValues = grvCoordinates.getValues();
        //Server


        currentLocation = gpsTracker.getLocation();
        com.example.iosuser11.postonwall.ServerModels.Location loc = new com.example.iosuser11.postonwall.ServerModels.Location(currentLocation.getLongitude(), currentLocation.getLatitude());
        picture = new Picture(normPicObject.getDescriptors().dump(), "a ", normPicObject.getKeypoints().dump(), (double)seekBar.getProgress(), 0, 0, 0, 0, 0, 0,Long.parseLong(id), url, loc);
        System.out.println(picture);

        //Post image to Server
       communicator.picturePost(picture);

    }

    private void findImagesNearby() {

        nearbyPicturesList = new ArrayList<>();
        longitude = gpsTracker.getLongitude();
        latitude = gpsTracker.getLatitude();
        currentCoordinates = String.valueOf(longitude) + "," + String.valueOf(latitude);

        communicator.pictureGet(currentCoordinates, distance);  // got picObject

        // currentPicturesList = new ArrayList<>();
//        com.example.iosuser11.postonwall.ServerModels.Location originalLocation;
//        currentLocation = gpsTracker.getLocation();
//        for(int i = 0; i < allPicturesList.size(); i++) {
//            originalLocation = allPicturesList.get(i).getLocation();
//            if((currentLocation.getLatitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLatitude())&&
//                    (currentLocation.getLatitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLatitude())){
//                if((currentLocation.getLongitude() + (currentLocation.getAccuracy()/111111.0) > originalLocation.getLongitude())&&
//                        (currentLocation.getLongitude() - (currentLocation.getAccuracy()/111111.0) < originalLocation.getLongitude())) {
//                    currentPicturesList.add(allPicturesList.get(i));
//                }
//            }
//        }
    }


    void makeNormalObjectsList() {
      //  Bitmap image = null;
        Location location = new Location("me");
        for(int i = 0; i < picObject.getPhotos().size(); i++) {
            nPO = new NormalPictureObject();
            location.setLongitude(picObject.getPhotos().get(i).getLoc().getLongitude());
            location.setLatitude(picObject.getPhotos().get(i).getLoc().getLatitude());
            nPO.setLocation(location);
          //  nPO.getLocation().setLatitude(i.getLoc().getLatitude());
            nPO.setKeypoints(getKeypoints(picObject.getPhotos().get(i).getKey_points()));
            nPO.setDescriptors(getDescriptors(picObject.getPhotos().get(i).getFeature_vector()));
            nPO.setScale((int) picObject.getPhotos().get(i).getX());

           nPO.setUrl(picObject.getPhotos().get(i).getUrl());
            nearbyPicturesList.add(nPO);

        }

        }




    void performImageMatch() {
        while (trackingState) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            byte[] data = cameraPreview.getCurrentFrame();
           // pre = new Mat(cameraPreview.getmPreviewSize().height + cameraPreview.getmPreviewSize().height / 2, cameraPreview.getmPreviewSize().width, CvType.CV_8UC1);
            pre.put(0, 0, data);
            Imgproc.cvtColor(pre, imgCurrent, Imgproc.COLOR_YUV2GRAY_NV21);
            Core.transpose(imgCurrent, imgCurrent);
            Core.flip(imgCurrent, imgCurrent, 1);
            detector.detect(imgCurrent, keypointsCurrent);
            descriptor.compute(imgCurrent, keypointsCurrent, descriptorsCurrent);

            Mat descriptorsOriginal;
            for (int i = 0; i < nearbyPicturesList.size() && !imageFound; i++) {
                //take descriptors of the wall of picture[i], match it with the wall being currently displayed
                descriptorsOriginal = nearbyPicturesList.get(i).getDescriptors();
                matcher.match(descriptorsCurrent, descriptorsOriginal, matches);
                List<DMatch> matchesList = matches.toList();
                List<DMatch> matches_final = new ArrayList<>();
                 for (int j = 0; j < matchesList.size(); j++) {
                    if (matchesList.get(j).distance <= 40) {
                        matches_final.add(matches.toList().get(j));
                    }
                }
                if (matches_final.size() > 125) {
                    imageFound = true;
                    currentPictureIndex = i;
                }
                if (imageFound && !pictureIsViewed(currentPictureIndex)) {

                    URL uRl;
                    Bitmap image = null;
                    try {
                        uRl = new URL(nearbyPicturesList.get(currentPictureIndex).getUrl());
                        image = BitmapFactory.decodeStream(uRl.openConnection().getInputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    currentPicturesIndexesList.add(currentPictureIndex);
                     nearbyPicturesList.get(currentPictureIndex).setPicture(image);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newTrackedPictureView(nearbyPicturesList.get(currentPictureIndex).getPicture());
                            currentPicturesList.get(currentPicturesList.size() - 1).getRenderer().updateDistance(20- nearbyPicturesList.get(currentPictureIndex).getScale());
                        }
                    });
                }
               // pictureRenderer.attachToWall();
                if(newPictureAdded)
                    currentPicturesList.get(currentPicturesList.size() - 1).getRenderer().attachToWall();
            }
            imageFound = false;
            newPictureAdded = false;
        }
    }




    boolean pictureIsViewed(int index) {
        for (int i = 0; i < currentPicturesIndexesList.size(); i++) {
            if ((int) currentPicturesIndexesList.get(i) == index)
                return true;
        }
        return false;
    }




    void createImageView(Bitmap image) {
        pictureView = new PictureView(getApplicationContext(), cameraPreview.getmPreviewSize().width, cameraPreview.getmPreviewSize().height);
        pictureView.setEGLContextClientVersion(2);
        pictureView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        pictureView.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        pictureView.setZOrderOnTop(true);
        pictureRenderer = new PictureRenderer(this, image);
        pictureView.setRenderer(pictureRenderer);
        wallView.addView(pictureView);
        pictureView.bringToFront();
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

    public static void setPicObject(PictureObject pictureObject) {
        picObject = pictureObject;
    }

    public static PictureObject getPicObject() {
        return picObject;
    }


    public static MatOfKeyPoint getKeypoints(String string) {
        string = string.replaceAll("\\s+", "");
        string = string.replaceAll("]", "");
        string = string.replaceAll("\\[", "");
        StringTokenizer semicolonTokenizer = new StringTokenizer(string, ";");
        StringTokenizer commaTokenizer;
        List keyPointStrings = new ArrayList();
        List<List> keyPointsList = new ArrayList<>();
        List keyPoint = null;

        while (semicolonTokenizer.hasMoreTokens()) {
            keyPointStrings.add(semicolonTokenizer.nextToken());
        }


        for (Object keyPointString : keyPointStrings) {
            commaTokenizer = new StringTokenizer((String) keyPointString, ",");
            keyPoint = new ArrayList();
            while (commaTokenizer.hasMoreTokens()) {
                keyPoint.add(commaTokenizer.nextToken());
            }
            keyPointsList.add(keyPoint);
        }

        KeyPoint[] keyPoints = new KeyPoint[keyPointsList.size()];

        for (int i = 0; i < keyPoints.length; i++) {
            switch (keyPointsList.get(i).size()) {
                case 3:
                    keyPoint = keyPointsList.get(i);
                    keyPoints[i] = new KeyPoint(Float.parseFloat((String) keyPoint.get(0)), Float.parseFloat((String) keyPoint.get(1)), Float.parseFloat((String) keyPoint.get(2)));
                    break;
                case 4:
                    keyPoints[i] = new KeyPoint(Float.parseFloat((String) keyPoint.get(0)), Float.parseFloat((String) keyPoint.get(1)), Float.parseFloat((String) keyPoint.get(2)), Float.parseFloat((String) keyPoint.get(3)));
                    break;
                case 5:
                    keyPoints[i] = new KeyPoint(Float.parseFloat((String) keyPoint.get(0)), Float.parseFloat((String) keyPoint.get(1)), Float.parseFloat((String) keyPoint.get(2)), Float.parseFloat((String) keyPoint.get(3)), Float.parseFloat((String) keyPoint.get(4)));
                    break;
                case 6:
                    keyPoints[i] = new KeyPoint(Float.parseFloat((String) keyPoint.get(0)), Float.parseFloat((String) keyPoint.get(1)), Float.parseFloat((String) keyPoint.get(2)), Float.parseFloat((String) keyPoint.get(3)), Float.parseFloat((String) keyPoint.get(4)), Integer.parseInt((String) keyPoint.get(5)));
                    break;
                default:
                    keyPoints[i] = new KeyPoint(Float.parseFloat((String) keyPoint.get(0)), Float.parseFloat((String) keyPoint.get(1)), Float.parseFloat((String) keyPoint.get(2)), Float.parseFloat((String) keyPoint.get(3)), Float.parseFloat((String) keyPoint.get(4)), Integer.parseInt((String) keyPoint.get(5)), Integer.parseInt((String) keyPoint.get(6)));
            }
        }

        MatOfKeyPoint keypoints = new MatOfKeyPoint(keyPoints);

        return keypoints;
    }


    public static Mat getDescriptors(String string) {
        string = string.replaceAll("\\s+", "");
        string = string.replaceAll("]","");
        string = string.replaceAll("\\[", "");


        StringTokenizer semicolonTokenizer = new StringTokenizer(string, ";");
        StringTokenizer commaTokenizer;
        List descriptorStrings = new ArrayList();
        List<String> descriptors = new ArrayList<String>();
        int height;
        int width = 32;
        Mat desc;
        int[] data;
        int[] items;
        int commas = 0;

        while (semicolonTokenizer.hasMoreTokens()) {
            descriptorStrings.add(semicolonTokenizer.nextToken());
        }

        for (Object descriptorString : descriptorStrings) {
            commaTokenizer = new StringTokenizer((String) descriptorString, ",");
            while (commaTokenizer.hasMoreTokens()) {
                descriptors.add(commaTokenizer.nextToken());
                commas ++;
            }
        }
        data = new int[commas];

        for(int i = 0; i < descriptors.size(); i++) {
             data[i] = Integer.parseInt(descriptors.get(i));
        }



        //data = new byte[descriptors.size()];
        //for (int i = 0; i < data.length; i++) {
        //    data[i] = (byte) descriptors.get(i);
        //}

        height = data.length / 32;
        desc = new Mat(height, width, CvType.CV_32S);

        desc.put(0, 0, data);
        Mat m = new Mat(height,width,CvType.CV_8U);
        desc.convertTo(m,CvType.CV_8U);
        return m;
    }


    public static void setPicsObject(PicsArtPicturesObject picsArtPictureObject) {
        picsObject = picsArtPictureObject;
    }


    public static PicsArtPicturesObject getPicsObject() {
        return picsObject;
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
}