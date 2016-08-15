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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("", "onDraw: called");
        super.onDraw(canvas);
//        canvas.setMatrix(transformMat);
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

    void setTransformMatrix(Matrix mat) {
        transformMat = mat;
    }

    void setParentSize(int width, int height) {
        parentHeight = width;
        parentWidth = height;
        setLayoutParams(new ViewGroup.LayoutParams(parentWidth, parentHeight));
//        setBackgroundColor(Color.RED);
    }
}
