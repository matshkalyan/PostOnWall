package com.example.iosuser11.postonwall;

/**
 * Created by iosuser9 on 25.08.16.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by iosuser9 on 22.08.16.
 */
public class ImageAdapter extends BaseAdapter {
    private Context context;


    public List<Bitmap> images;

    public ImageAdapter(Context c, List<Bitmap> lb) {
        context = c;
        images = lb;

    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(images.get(position));
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setLayoutParams(new GridView.LayoutParams(400, 400));
        return imageView;
    }
}
