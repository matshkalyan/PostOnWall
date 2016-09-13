package com.example.iosuser11.postonwall.GL;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;

/**
 * Created by iosuser11 on 8/19/16.
 */
public class PictureView extends GLSurfaceView{

    private int parentWidth;
    private int parentHeight;

    public PictureView(Context context, int width, int height) {
        super(context);
        parentHeight = width;
        parentWidth = height;
        setLayoutParams(new ViewGroup.LayoutParams(parentWidth, parentHeight));
    }
}