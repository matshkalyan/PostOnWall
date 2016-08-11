package com.example.iosuser11.postonwall;

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
        setLayoutParams(new ViewGroup.LayoutParams(camera.getParameters().getPreviewSize().height, camera.getParameters().getPreviewSize().width));
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        holder = getHolder();
        holder.addCallback(this);
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
        Log.d("", "surfaceDestroyed: surface destroyed");
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

    }

    void pause() {
        camera.stopPreview();
        camera.setPreviewCallback(null);
        camera.release();
    }

    void initCamera() {
        camera = Camera.open();
        Camera.Parameters params;
        params = camera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(params);
        camera.setPreviewCallback(this);
        camera.setDisplayOrientation(90);
    }

}
