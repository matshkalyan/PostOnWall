package com.example.iosuser11.postonwall.Network;

import android.util.Log;

import com.example.iosuser11.postonwall.ServerModels.Picture;
import com.example.iosuser11.postonwall.ServerModels.PictureObject;
import com.example.iosuser11.postonwall.ServerModels.PicturesAPI;
import com.jakewharton.retrofit.Ok3Client;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.JacksonConverter;

/**
 * Created by iosuser9 on 18.08.16.
 */
public class Communicator {
    private static  final String TAG = "Communicator";
    private static final String Server_URL = "http://192.168.40.33:3011"; // Servers URL : port

    public void picturePost(Picture picture) {


        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();


        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Server_URL).setClient(new Ok3Client(okHttpClient))
                .build();

        PicturesAPI communicatorInterface = restAdapter.create(PicturesAPI.class);
        Callback<Picture> callback = new Callback<Picture>() {
            @Override
            public void success(Picture picture, Response response) {
                System.out.println(response.getStatus());
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("FAIL! FAIL! FAIL!!!!!!!");
                if(error != null ){
                    Log.e(TAG, error.getMessage());
                    error.printStackTrace();
                }

            }
        };
        communicatorInterface.postData(picture, callback);
    }

    public void pictureGet(String coordinates, double distance) {
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Server_URL).setConverter(new JacksonConverter()).build();
        PicturesAPI communicatorInterface = restAdapter.create(PicturesAPI.class);
        Callback<PictureObject> callback = new Callback<PictureObject>() {
            @Override
            public void success(PictureObject pictureObject, Response response) {
                System.out.println("Success:  "+pictureObject.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("FAIL");
            }
        };


        communicatorInterface.getData(coordinates, distance, callback);

    }
}
