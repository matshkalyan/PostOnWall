package com.example.iosuser11.postonwall.ServerModels;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by iosuser9 on 18.08.16.
 */
public interface PicsArtPicturesAPI {
    @GET("/photos/show/featured.json")
    void getData(Callback<PicsArtPicturesObject> serverResponseCallback);
}
