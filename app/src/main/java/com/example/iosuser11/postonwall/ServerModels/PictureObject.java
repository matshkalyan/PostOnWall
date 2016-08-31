package com.example.iosuser11.postonwall.ServerModels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iosuser9 on 18.08.16.
 */
public class PictureObject {
    private List<Picture> photos = new ArrayList<>();


    public List<Picture> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Picture> photosObject) {
        this.photos = photosObject;
    }

    @Override
    public String toString() {
        return "PictureObject{" +
                "photos=" + photos.size() +
                '}';
    }
}
