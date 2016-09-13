package com.example.iosuser11.postonwall;

import com.example.iosuser11.postonwall.GL.PictureRenderer;
import com.example.iosuser11.postonwall.GL.PictureView;

/**
 * Created by iosuser11 on 8/25/16.
 */
public class PicViewAndRend {
    private PictureView view;
    private PictureRenderer renderer;

    public PicViewAndRend(PictureView pictureView, PictureRenderer pictureRenderer) {
        view = pictureView;
        renderer = pictureRenderer;
    }

    void setView(PictureView pictureView) {
        view = pictureView;
    }

    void setRenderer(PictureRenderer pictureRenderer) {
        renderer = pictureRenderer;
    }

    PictureView getView() {
        return view;
    }

    PictureRenderer getRenderer() {
        return renderer;
    }

}
