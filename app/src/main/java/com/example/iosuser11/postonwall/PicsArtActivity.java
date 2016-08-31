package com.example.iosuser11.postonwall;

/**
 * Created by iosuser9 on 25.08.16.
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PicsArtActivity extends AppCompatActivity {
    private List<Bitmap> bitmapList = new ArrayList<>();
    private ProgressDialog simpleWaitDialog;
    private GridView gridView;
    private String[][] urlsAndIds;
    private String picsImageUrl;
    private String picsImageId;
    private String folderPath;
    private String chosenImagePath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pics_art);

        gridView = (GridView)findViewById(R.id.gridView);
        urlsAndIds = new String[2][3];

        for(int i = 0; i < urlsAndIds.length; i++) {
            for (int j = 0; j < urlsAndIds[i].length; j++) {
                if (i == 0) {
                    urlsAndIds[i][j] = MainActivity.getPicsObject().getResponse().get(j).getUrl() + "?r1024x1024";
                }
                if(i == 1) {
                    urlsAndIds[i][j] = MainActivity.getPicsObject().getResponse().get(j).getIdAsString();
                }
            }
        }

        for(int i = 0; i < urlsAndIds.length; i++) {
            for (int j = 0; j < urlsAndIds[i].length; j++) {

                System.out.println(urlsAndIds[i][j]);
            }
        }

        new ImageDownloader().execute(urlsAndIds);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                if(position == 0) {
                    picsImageId = urlsAndIds[1][0];
                    picsImageUrl = urlsAndIds[0][0];
                    System.out.println("ID: " + picsImageId + " URL: " + picsImageUrl);
                    chosenImagePath = folderPath + "/" + picsImageId;

                }
                if(position == 1) {
                    picsImageId = urlsAndIds[1][1];
                    picsImageUrl = urlsAndIds[0][1];
                    System.out.println("ID: " + picsImageId + " URL: " + picsImageUrl);
                    chosenImagePath = folderPath + "/" + picsImageId;



                }

                if(position == 2) {
                    picsImageId = urlsAndIds[1][2];
                    picsImageUrl = urlsAndIds[0][2];
                    System.out.println("ID: " + picsImageId + " URL: " + picsImageUrl);
                    chosenImagePath = folderPath +  "/" +picsImageId;



                }


                Intent resultIntent = new Intent();
                resultIntent.putExtra("url", picsImageUrl);
                resultIntent.putExtra("id", picsImageId);

                resultIntent.putExtra("filePath", chosenImagePath);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

        });




    }



    private class ImageDownloader extends AsyncTask<String[], Void, List<Bitmap>> {

//        @Override
//        protected List<Bitmap> doInBackground(String[][] strings) {
//            for(int i = 0; i < strings.length; i++) {
//                System.out.println("STRINGS[0]" + strings[i]);
//                bitmapList.add(downloadBitmap(strings[i]));
//            }
//            return bitmapList;
//        }

        @Override
        protected List<Bitmap> doInBackground(String[][] strings) {
            // for(int j = 0; j < strings[0].length; j++) {
            for(int j = 0; j < 3; j++) {
                bitmapList.add(downloadBitmap(strings[0][j], strings[1][j]));
            }
            return bitmapList;
        }

        @Override
        protected void onPreExecute() {
            simpleWaitDialog = ProgressDialog.show(PicsArtActivity.this, "Wait", "Downloading Image");
        }


        @Override
        protected void onPostExecute(List<Bitmap> bitmapL) {
            gridView.setAdapter(new ImageAdapter(PicsArtActivity.this, bitmapL));
            simpleWaitDialog.dismiss();
        }
//
//        private Bitmap downloadBitmap(String url) {
//            final DefaultHttpClient client = new DefaultHttpClient();
//            final HttpGet getRequest = new HttpGet(url);
//            try {
//                HttpResponse response = client.execute(getRequest);
//                final int statusCode = response.getStatusLine().getStatusCode();
//
//                if(statusCode != HttpStatus.SC_OK) {
//                    return null;
//                }
//                final HttpEntity entity = response.getEntity();
//
//                if(entity != null) {
//                    InputStream inputStream = null;
//
//                    try {
//                        inputStream = entity.getContent();
//
//                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//
//                        return bitmap;
//                    } finally {
//                        if (inputStream != null) {
//                            inputStream.close();
//                        }
//                        entity.consumeContent();
//                    }
//                }
//            }
//            catch (Exception e) {
//                getRequest.abort();
//            }
//            return null;
//
//        }



                private Bitmap downloadBitmap(String url, String id) {
            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpGet getRequest = new HttpGet(url);
            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();

                if(statusCode != HttpStatus.SC_OK) {
                    return null;
                }
                final HttpEntity entity = response.getEntity();

                if(entity != null) {
                    InputStream inputStream = null;

                    try {
                        inputStream = entity.getContent();

                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);


//                        File extStore = Environment.getExternalStorageDirectory();
//                        File myFile = new File(extStore.getAbsolutePath() +id.toString());
//                        if(myFile.exists()) {
//                            return null;
//                        }
                        ImageStore img = new ImageStore(bitmap);
                        img.setId(id);
                        img.store(ImageStore.SAVE_AS_BITMAP);
                        folderPath = img.getFolderPath();
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 960, 730, false);
                        return resizedBitmap;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            }
            catch (Exception e) {
                getRequest.abort();
            }
            return null;

        }


    }


    public String getClickedId() {
        return picsImageId;
    }

    public String getClickedUrl() {
        return picsImageUrl;
    }


}
