package com.example.iosuser11.postonwall;

import android.graphics.Bitmap;
import android.location.Location;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

/**
 * Created by iosuser11 on 8/19/16.
 */
public class PictureObject {
    private Location location;
    private MatOfKeyPoint keypoints;
    private Mat descriptors;
    private Bitmap picture;

    PictureObject(Bitmap p) {
        setPicture(p);
    }

    void setLocation(Location l) {
        location = l;
    }

    void setKeypoints(MatOfKeyPoint k) {
        keypoints = k;
    }

    void setDescriptors(Mat d) {
        descriptors = d;
    }

    void setPicture(Bitmap p) {
        picture = p;
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

    Bitmap getPicture() {
        return picture;
    }
}