package com.example.iosuser11.postonwall.Network;

import com.example.iosuser11.postonwall.ServerModels.PicsArtPicturesObject;
import com.example.iosuser11.postonwall.ServerModels.PicsArtPicturesAPI;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.JacksonConverter;

/**
 * Created by iosuser9 on 18.08.16.
 */
public class CommunicatorPicsArt {
    private static  final String TAG = "CommunicatorPicsArt";
    private static final String ServerPicsArt_URL = "http://api.picsart.com"; // Servers URL : port

    public void picsArtPictureGet() {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(ServerPicsArt_URL).setConverter(new JacksonConverter()).build();
        PicsArtPicturesAPI communicatorPicsArtInterface = restAdapter.create(PicsArtPicturesAPI.class);
        Callback<PicsArtPicturesObject> callback = new Callback<PicsArtPicturesObject>() {

            @Override
            public void success(PicsArtPicturesObject picsArtPictureObject, Response response) {
                System.out.println(picsArtPictureObject.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("FAIL_PICS_ART!!!");

            }
        };

        communicatorPicsArtInterface.getData(callback);


    }
}
