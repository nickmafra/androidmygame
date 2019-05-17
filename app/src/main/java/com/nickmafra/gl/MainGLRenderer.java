package com.nickmafra.gl;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainGLRenderer implements GLSurfaceView.Renderer {

    private Context context;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private List<Object3D> objetos;

    public MainGLRenderer(Context context) {
        this.context = context;

        objetos = new ArrayList<>();
    }

    public List<Object3D> getObjetos() {
        return objetos;
    }

    private volatile boolean modelsLoaded = true;

    public void requestLoadModels() {
        modelsLoaded = false;
    }

    private void loadModels() {
        for (Object3D obj : objetos) {
            obj.getModel().load(context);
        }
        modelsLoaded = true;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    // mVP is an abbreviation for "View Projection Matrix"
    private final float[] mVP = new float[16];

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!modelsLoaded) {
            loadModels();
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5, 0f, 0f, 2f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mVP, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        for (Object3D obj : objetos) {
            obj.draw(mVP);
        }
    }
}
