package com.example.iosuser11.postonwall;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback  {

    Context context;
    Camera camera;
    SurfaceHolder holder;
    byte[] currentFrame;

    public CameraPreview(Context context) {
        super(context);
        this.context = context;
        initCamera();
        setLayoutParams(new ViewGroup.LayoutParams(camera.getParameters().getPreviewSize().height, camera.getParameters().getPreviewSize().width));
//        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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
        currentFrame = bytes;
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

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point displaysize = new Point();
        display.getSize(displaysize);

        for (Camera.Size size : params.getSupportedPreviewSizes()) {
            if (size.width * size.height > params.getPreviewSize().width * params.getPreviewSize().height && size.width < displaysize.x && size.height < displaysize.y) {
                params.setPreviewSize(size.width, size.height);
            }
        }

        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        camera.setParameters(params);
        camera.setPreviewCallback(this);
        camera.setDisplayOrientation(90);
    }

    byte[] getCurrentFrame() {
        return currentFrame;
    }

    Camera.Size getPreviewSize() {
        return camera.getParameters().getPreviewSize();
    }

}