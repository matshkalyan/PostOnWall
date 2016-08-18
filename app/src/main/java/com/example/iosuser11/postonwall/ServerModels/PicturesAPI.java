package com.example.iosuser11.postonwall.ServerModels;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by iosuser9 on 18.08.16.
 */
public interface PicturesAPI {
    @POST("/api/photos")
    void postData(@Body Picture picture,
                  Callback<Picture> serverResponseCallback);
    @GET("/api/photos")
    void getData(@Query("coordinates")  String coordinates, @Query("distance") double distance,
                 Callback<PictureObject> serverResponseCallback);

}
