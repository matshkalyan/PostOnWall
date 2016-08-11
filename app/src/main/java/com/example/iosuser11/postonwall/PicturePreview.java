package com.example.iosuser11.postonwall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by iosuser11 on 8/10/16.
 */
public class PicturePreview extends View {

    private Matrix transformMat;
    private Bitmap picture;

    public PicturePreview(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setMatrix(transformMat);
        picture = BitmapFactory.decodeResource(getResources(), R.drawable.picture1);
        canvas.drawBitmap(picture, 100, 100, null);
        Log.d("", "onDraw: called");
    }

    void setTransformMatrix(Matrix mat) {
        transformMat = mat;
    }
}
