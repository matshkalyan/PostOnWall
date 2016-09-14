package com.example.iosuser11.postonwall.GL;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.iosuser11.postonwall.GL.util.TextureHelper;

import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glDrawArrays;


public class Table {
    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COORDINATES_COMPONENT_COUNT = 2;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    private final VertexArray vertexArray;

    private Bitmap bitmap;
    private TextureShaderProgram textureProgram;
    private int texture;

    private Context context;

    public Table(Context c, Bitmap b)
    {
        context = c;

        bitmap = b;
        float width = b.getWidth();
        float height = b.getHeight();

        final float[] VERTEX_DATA = {
                // Order of coordinates:
                // X,            Y,                       S,     T
                0f,              0f,                      0.5f,  0.5f,
                -width / 900f,   -height / 900f,           0f,    1f,
                width / 900f,   -height / 900f,           1f,    1f,
                width / 900f,    height / 900f,           1f,    0f,
                -width / 900f,    height / 900f,           0f,    0f,
                -width / 900f,   -height / 900f,           0f,    1f
        };

        vertexArray = new VertexArray(VERTEX_DATA);

        textureProgram = new TextureShaderProgram(context);
        texture = TextureHelper.loadTexture(context, bitmap);
        textureProgram.useProgram();


    }

    public void bindData(TextureShaderProgram textureProgram) {
        vertexArray.setVertexAttribPointer(0, textureProgram.getPositionAttributeLocation(), POSITION_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, textureProgram.getTextureCoordinatesAttributeLocation(), TEXTURE_COORDINATES_COMPONENT_COUNT, STRIDE);
    }

    public void draw(float[] finalTransformMatrix) {
        textureProgram.useProgram();
        textureProgram.setUniforms(finalTransformMatrix, texture);
        bindData(textureProgram);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
    }
}