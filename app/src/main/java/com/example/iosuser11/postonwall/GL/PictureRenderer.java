package com.example.iosuser11.postonwall.GL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.iosuser11.postonwall.MotionSensors;
import com.example.iosuser11.postonwall.GL.util.MatrixHelper;
import com.example.iosuser11.postonwall.GL.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.*;

public class PictureRenderer implements GLSurfaceView.Renderer
{
    private final Context context;

    private TextureShaderProgram textureProgram;
    private int texture;
    private Table table;
    private Bitmap image;
    private float imageHeight;
    private float imageWidth;

    private final float[] identMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private float[] modelMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];
    private final float[] finalTransformMatrix = new float[16];

    private MotionSensors motionSensors;
    float[] rotationMatrix = new float[16];
    float[] matCache = new float[16];
    float[] matCacheTranspose = new float[16];

    float[] result = new float[16];

    private boolean pttvel = false;

    private float d = 10;

//    double translateX;
//    double translateY;

    public PictureRenderer(Activity activity, Bitmap image) {
        this.context = activity.getApplicationContext();
        motionSensors = new MotionSensors(activity);
        this.image = image;
        imageHeight = (float) image.getHeight();
        imageWidth = (float) image.getWidth();
        setIdentityM(identMatrix, 0);
        modelMatrix = identMatrix.clone();
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        table = new Table(imageHeight, imageWidth);
        textureProgram = new TextureShaderProgram(context);
        texture = TextureHelper.loadTexture(context, image);

    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        glViewport(0, 0, width, height);
        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 1000f);
        setLookAtM(viewMatrix, 0,
                0f, 0f, 0.1f,
                0f, 0f, 0f,
                0f, 1f, 0f);
    }


    @Override
    public void onDrawFrame(GL10 glUnused) {
        glClear(GL_COLOR_BUFFER_BIT);
        GLES20.glEnable(GL10.GL_CULL_FACE);     // enable face culling feature
        GLES20.glCullFace(GL10.GL_BACK);        // specify which faces to not draw

        rotationMatrix = motionSensors.getRotationMatrix();
//        float[] translateXYZ = motionSensors.getTranslationXYZ();

        multiplyMM(finalTransformMatrix, 0, projectionMatrix, 0, identMatrix, 0);
        multiplyMM(finalTransformMatrix, 0, finalTransformMatrix, 0, viewMatrix, 0);
        multiplyMM(finalTransformMatrix, 0, finalTransformMatrix, 0, modelMatrix, 0);


        if(pttvel && rotationMatrix != null) {
            //translation due to rotation and seekbar
            transposeM(matCacheTranspose, 0, matCache, 0);
            multiplyMM(result, 0, rotationMatrix, 0, matCacheTranspose, 0);
            translateM(finalTransformMatrix, 0, -(d)*result[8],  -(d)*result[9],  -(d)*result[10]);

            //rotation
            multiplyMM(finalTransformMatrix, 0, finalTransformMatrix, 0, rotationMatrix, 0);
            multiplyMM(finalTransformMatrix, 0, finalTransformMatrix, 0, matCacheTranspose, 0);
        } else if (!pttvel) {
            matCache = motionSensors.getRotationMatrix();
            translateM(finalTransformMatrix, 0, 0, 0, -(d));
        }

        // Draw the table.
//        positionTableInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(finalTransformMatrix, texture);
        table.bindData(textureProgram);
        table.draw();
    }

    public void attachToWall() {
        pttvel = true;
    }

    private void positionTableInScene() {
        setIdentityM(modelMatrix, 0);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }

    public void updateDistance(int d) {
        this.d = (float) d;
    }
}