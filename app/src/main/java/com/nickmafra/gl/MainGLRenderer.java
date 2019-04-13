package com.nickmafra.gl;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainGLRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private PiramideModel piramide;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public MainGLRenderer(Context context) {
        this.context = context;
        piramide = new PiramideModel();
        Matrix.setIdentityM(rotationMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        piramide.load(context);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Draw background color
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5, 0f, 0f, 2f, 0f, 1.0f, 0.0f);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        // Draw model
        drawPiramide();
    }

    private volatile float rvX;
    private volatile float rvY;

    public void setRotationVector(float rvX, float rvY) {
        this.rvX = rvX;
        this.rvY = rvY;
    }

    private float[] rotationMatrix = new float[16];
    private float[] scratch = new float[16];

    private void drawPiramide() {
        float v = (float) Math.sqrt(rvX*rvX + rvY*rvY);
        if (v > 0.01) {
            // acumula rotações
            Matrix.setRotateM(scratch, 0, v, rvY, rvX, 0);
            Matrix.multiplyMM(rotationMatrix, 0, scratch, 0, rotationMatrix, 0);
        }

        // calcula matriz resultante
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, rotationMatrix, 0);
        piramide.setMvpMatrix(scratch);
        piramide.draw();
    }
}
