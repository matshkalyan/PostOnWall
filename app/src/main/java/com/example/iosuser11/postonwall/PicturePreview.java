package com.example.iosuser11.postonwall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class PicturePreview extends View {

    private Matrix transformMat;
    private Bitmap p, picture;
    private Context context;
    private int parentWidth, parentHeight;

    public PicturePreview(Context context) {
        super(context);
        this.context = context;
        p = BitmapFactory.decodeResource(getResources(), R.drawable.picture1);
        picture = Bitmap.createScaledBitmap(p, p.getWidth()/5, p.getHeight()/5, true);
//        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        setLayoutParams(new ViewGroup.LayoutParams(picture.getWidth(), picture.getHeight()));
        transformMat = new Matrix();
        transformMat.setValues(new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1});
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("", "onDraw: called");
        super.onDraw(canvas);
        canvas.setMatrix(transformMat);
        Log.d("", "onDraw: transformmat is: " + transformMat);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point displaysize = new Point();
        wm.getDefaultDisplay().getSize(displaysize);
//        canvas.drawBitmap(picture, (displaysize.x - picture.getWidth())/2, (displaysize.y - picture.getHeight())/2, null);
//        canvas.drawBitmap(picture, 700, 0, null);
//        canvas.drawBitmap(picture, 200, 100, null);
        canvas.drawBitmap(picture, (parentWidth - picture.getWidth())/2, (parentHeight - picture.getHeight())/2, null);
//        canvas.drawBitmap(picture, (parentHeight - picture.getHeight())/2, (parentWidth - picture.getWidth())/2, null);
        Log.d("", "onDraw: coords of the picture being drawn is: " + (parentWidth - picture.getWidth())/2 + " " + (parentHeight - picture.getHeight())/2);
    }

    void setTransformMatrix(float[] transform) {
//        transformMat = mat;
        //we should multiply the identity matrix with transform

        float[][] ident = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
        float[][] trans = {{transform[0], transform[1], transform[2]}, {transform[3], transform[4], transform[5]}, {transform[6], transform[7], transform[8]}};
        float[][] temp = new float[3][3];
        multiply(ident, trans, temp);
        transformMat.setValues(new float[]{temp[0][0],temp[0][1],temp[0][2],temp[1][0],temp[1][1],temp[1][2],temp[2][0],temp[2][1],temp[2][2]});
        Log.d("", "setTransformMatrix: transformmat is: " + transformMat);
    }

    void setParentSize(int width, int height) {
        parentHeight = width;
        parentWidth = height;
        setLayoutParams(new ViewGroup.LayoutParams(parentWidth, parentHeight));
    }

    public static void multiply(float[][] m1, float[][] m2, float[][] result) {
        int m1ColLength = m1[0].length; // m1 columns length
        int mRRowLength = m1.length;    // m result rows length
        int mRColLength = m2[0].length; // m result columns length
        float[][] mResult = new float[mRRowLength][mRColLength];
        for(int i = 0; i < mRRowLength; i++) {         // rows from m1
            for(int j = 0; j < mRColLength; j++) {     // columns from m2
                for(int k = 0; k < m1ColLength; k++) { // columns from m1
                    result[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        result = mResult;
    }

    void reset() {
        Log.d("", "reset: called");
        transformMat.setValues(new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1});
        this.invalidate();
        Log.d("", "reset: called");
    }
}
