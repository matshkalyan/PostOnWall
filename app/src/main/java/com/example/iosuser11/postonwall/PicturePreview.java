package com.example.iosuser11.postonwall;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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

    public PicturePreview(Context context) {
        super(context);
        this.context = context;
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setMatrix(transformMat);
        p = BitmapFactory.decodeResource(getResources(), R.drawable.picture1);
        picture = Bitmap.createScaledBitmap(p, p.getWidth()/5, p.getHeight()/5, true);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point displaysize = new Point();
        wm.getDefaultDisplay().getSize(displaysize);
        canvas.drawBitmap(picture, (displaysize.x - picture.getWidth())/2, (displaysize.y - picture.getHeight())/2, null);
    }

    void setTransformMatrix(Matrix mat) {
        transformMat = mat;
    }
}
