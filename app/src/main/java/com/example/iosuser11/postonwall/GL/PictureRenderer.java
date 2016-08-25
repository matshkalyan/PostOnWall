package com.example.iosuser11.postonwall.GL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.iosuser11.postonwall.GRVCoordinates;
import com.example.iosuser11.postonwall.GL.objects.Table;
import com.example.iosuser11.postonwall.GL.programs.TextureShaderProgram;
import com.example.iosuser11.postonwall.GL.util.MatrixHelper;
import com.example.iosuser11.postonwall.GL.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glGetError;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.setLookAtM;
import static android.opengl.Matrix.translateM;

public class PictureRenderer implements GLSurfaceView.Renderer
{
    private final Context context;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] viewProjectionMatrix = new float[16];
    private final float[] modelViewProjectionMatrix = new float[16];

    float[] mat = new float[16];
    float[] matCache = new float[16];
    float[] tmp = new float[16];

    float[] matCacheTranspose = new float[16];
    float[] result = new float[16];

    private boolean useCustomImge = false;
    private Bitmap image;

    private Table table;
    private GRVCoordinates grvCoordinates;

    private TextureShaderProgram textureProgram;

    private int texture;
    private boolean pttvel = false;

    private float d = 10;

    public PictureRenderer(Activity activity, Bitmap image)
    {
        this.context = activity.getApplicationContext();

        grvCoordinates = new GRVCoordinates(activity);

        this.image = image;
        useCustomImge = true;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
    {

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        table = new Table();

        textureProgram = new TextureShaderProgram(context);
        texture = TextureHelper.loadTexture(context, image);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height)
    {
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);

        MatrixHelper.perspectiveM(projectionMatrix, 45, (float) width / (float) height, 1f, 1000f);

        setLookAtM(
//                float[] rm
//                This is the destination array.
                viewMatrix,

//                int rmOffset
//                setLookAtM() will begin writing the result at this offset into rm
                0,

//                float eyeX, eyeY, eyeZ
//                This is where the eye will be. Everything in the
//                scene will appear as if we’re viewing it from this
//                point.

//                With an eye of (0, 1.2, 2.2), meaning your eye will be 1.2
//                units above the x-z plane and 2.2 units back. In other words, everything in
//                the scene will appear 1.2 units below you and 2.2 units in front of you.

                0f, 0f, 0.1f,

//                float centerX, centerY,centerZ
//                This is where the eye is looking; this position will appear in the center of the scene.

//                A center of (0, 0, 0) means you’ll be looking down toward the origin in front of you
                0f, 0f, 0f,

//                float upX, upY, upZ
//                If we were talking about your eyes, then this is
//                where your head would be pointing. An upY of 1
//                means your head would be pointing straight up.

//                An up of (0, 1, 0) means that your head will be pointing straight up and the scene won’t be rotated to either side
                0f, 1f, 0f);
    }


    @Override
    public void onDrawFrame(GL10 glUnused)
    {
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        if(pttvel) {
            mat = grvCoordinates.getRotationMatrix();

            Matrix.transposeM(matCacheTranspose, 0, matCache, 0);
            multiplyMM(result, 0, mat, 0, matCacheTranspose, 0);

            multiplyMM(tmp, 0, viewMatrix, 0, result, 0);

//            tmp[3]  = tmp[3]  + 1 * result[8];
//            tmp[7]  = tmp[7]  + 1 * result[9];
//            tmp[11] = tmp[11] + 1 * result[10];

            multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, tmp, 0);

            translateM(viewProjectionMatrix, 0, result[8],  result[9],  -(d)*result[10]);
//            translateM(viewProjectionMatrix, 0, -(6) * result[8], -(6) * result[9],  -(6) * result[10]);


        } else {
            matCache = grvCoordinates.getRotationMatrix();
            multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            translateM(viewProjectionMatrix, 0, 0 , 0 ,   -(d) );
        }

        // Draw the table.
        positionTableInScene();
        textureProgram.useProgram();
        textureProgram.setUniforms(modelViewProjectionMatrix, texture);
        table.bindData(textureProgram);
        table.draw();
        int err =  glGetError();
        if(err != 0) {
            Log.d("", "onDrawFrame: " + err);
        }
    }

    public void attachToWall(){pttvel = true;}
    public void detachFromWall(){pttvel = false;}

    private void positionTableInScene()
    {
        // The table is defined in terms of X & Y coordinates, so we rotate it
        // 90 degrees to lie flat on the XZ plane.
        setIdentityM(modelMatrix, 0);
//        rotateM(modelMatrix, 0, 90f, 1f, 0f, 0f);
        multiplyMM(modelViewProjectionMatrix, 0, viewProjectionMatrix, 0, modelMatrix, 0);
    }

    public void updateDistance(int d) {
        this.d =(float) d;
    }
}