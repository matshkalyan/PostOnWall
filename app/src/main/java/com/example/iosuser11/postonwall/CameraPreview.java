package com.example.iosuser11.postonwall;

import android.app.ActionBar;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback  {

    Camera camera;
    SurfaceHolder holder;

    public CameraPreview(Context context) {
        super(context);
        initCamera();
        this.setLayoutParams(new ViewGroup.LayoutParams(camera.getParameters().getPreviewSize().height, camera.getParameters().getPreviewSize().width));
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }

    void pause() {
        camera.release();
    }

    void initCamera() {
            camera = Camera.open();
            camera.setPreviewCallback(this);
            camera.setDisplayOrientation(90);
//
    }
}
