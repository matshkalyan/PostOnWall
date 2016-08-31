package com.example.iosuser11.postonwall;

import android.graphics.Bitmap;
import android.location.Location;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

import java.net.URL;


public class NormalPictureObject {




    private Location location;
    private MatOfKeyPoint keypoints;
    private Mat descriptors;
    private Bitmap picture;
    private int scale;
    private String url;

    NormalPictureObject(Bitmap p) {
        setPicture(p);
    }

    NormalPictureObject() {

    }

    NormalPictureObject(Location l, MatOfKeyPoint k, Mat d, Bitmap p, int s) {
        location = l;
        keypoints = k;
        descriptors = d;
        picture = p;
        scale = s;
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


    public void setPicture(Bitmap p) {
        picture = p;
    }

    void setScale(int s)
    {
        scale = s;
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

    int getScale() {
        return scale;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
