package com.example.iosuser11.postonwall;

import android.graphics.Bitmap;
import android.location.Location;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;


public class PictureObject {
    private Location location;
    private MatOfKeyPoint keypoints;
    private Mat descriptors;
    private Bitmap picture;

    void setLocation(Location l) {
        location = l;
    }

    void setKeypoints(MatOfKeyPoint k) {
        keypoints = k;
    }

    void setDescriptors(Mat d) {
        descriptors = d;
    }

    Location getLocation() {
        return location;
    }

    MatOfKeyPoint getKeypoints() {
        return keypoints;
    }

    Mat getDescriptors() {
        return descriptors;
    }
}
