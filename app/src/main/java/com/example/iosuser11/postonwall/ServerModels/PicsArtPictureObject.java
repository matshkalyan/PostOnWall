package com.example.iosuser11.postonwall.ServerModels;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iosuser9 on 18.08.16.
 */
public class PicsArtPictureObject {
    private List<PicsArtPictures> response = new ArrayList<>();


    public List<PicsArtPictures> getResponse() {
        return response;
    }

    public void setResponse(List<PicsArtPictures> photosPicsArtObject) {
        this.response = photosPicsArtObject;
    }

    @Override
    public String toString() {
        return "PicturePicsArtObject{" +
                "photosPicsArt =" + response +
                '}';
    }
}
