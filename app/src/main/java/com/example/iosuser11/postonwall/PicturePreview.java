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
    private Point displaySize;

    public PicturePreview(Context context, int width, int height) {
        super(context);
        this.context = context;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        displaySize = new Point();
        wm.getDefaultDisplay().getSize(displaySize);
        p = BitmapFactory.decodeResource(getResources(), R.drawable.picture1);
        picture = Bitmap.createScaledBitmap(p, p.getWidth()/5, p.getHeight()/5, true);
        transformMat = new Matrix();
//        transformMat.setValues(new float[]{1, 0, 0, 0, 1, 0, 0, 0, 1});
//        transformMat.setValues(new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0});

        parentHeight = width;
        parentWidth = height;
        setLayoutParams(new ViewGroup.LayoutParams(parentWidth, parentHeight));

        transformMat.setValues(new float[]{1, 0, (displaySize.x - parentWidth)/2, 0, 1, (displaySize.y - parentHeight)/2, 0, 0, 1});
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.setMatrix(transformMat);
        transformMat = canvas.getMatrix();
        canvas.drawBitmap(picture, (parentWidth - picture.getWidth())/2, (parentHeight - picture.getHeight())/2, null);
    }

    void setTransformMatrix(float[] transform) {

//        float[] arr = new float[9];
//        transformMat.getValues(arr);
//        float[][] ident = {{arr[0], arr[1], arr[2]}, {arr[3], arr[4], arr[5]}, {arr[6], arr[7], arr[8]}};
        float[][] ident = {{1, 0, (displaySize.x - parentWidth)/2}, {0, 1, (displaySize.y - parentHeight)/2}, {0, 0, 1}};


        //try getting the transformation different ways
        float[][] trans = {{transform[0], transform[1], transform[2]}, {transform[3], transform[4], transform[5]}, {transform[6], transform[7], transform[8]}};
        Log.d("", "setTransformMatrix: transform is: " + transform[0] + " " + transform[1] + " " + transform[2] + " " + transform[3] + " " + transform[4] + " " + transform[5] + " " + transform[6] + " " + transform[7] + " " + transform[8] + " " );
        float[][] temp = new float[3][3];
        multiply(ident, trans, temp);
//        transformMat.setValues(new float[]{temp[0][0],temp[0][1],temp[0][2],temp[1][0],temp[1][1],temp[1][2],temp[2][0],temp[2][1],temp[2][2]});
//        Log.d("", "setTransformMatrix: transformmat is: " + transformMat);

        //added for just rotation
//        double yaw=Math.atan2(transform[3],transform[0]);
//        double pitch=Math.atan2(-transform[6],Math.sqrt(transform[7]*transform[7]+transform[8]*transform[8]));
//        double roll=Math.atan2(transform[7],transform[8]);

        double yaw=Math.atan2(transform[6],transform[7]);
        double pitch=Math.acos(transform[8]);
        double roll=Math.atan2(transform[2],transform[5]);

//        transformMat.postRotate((float)(roll*180.0/Math.PI));
        transformMat.setValues(new float[]{ident[0][0], ident[0][1], ident[0][2], ident[1][0], ident[1][1], ident[1][2], ident[2][0], ident[2][1], ident[2][2]});

        if(Math.abs(Math.PI - yaw) < 0.01 || Math.abs(-Math.PI - yaw) < 0.01)
            transformMat.postRotate((float)(0), displaySize.x/2, displaySize.y/2);
        else
            transformMat.postRotate((float)(yaw * 180.0/Math.PI * 2), displaySize.x/2, displaySize.y/2);

        Log.d("", "setTransformMatrix: roll is: " + roll * 180.0/Math.PI );

        Log.d("", "");

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
        transformMat.setValues(new float[]{1, 0, (displaySize.x - parentWidth)/2, 0, 1, (displaySize.y - parentHeight)/2, 0, 0, 1});
        this.invalidate();
    }
}