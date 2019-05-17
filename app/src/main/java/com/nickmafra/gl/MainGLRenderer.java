package com.nickmafra.gl;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainGLRenderer implements GLSurfaceView.Renderer {

    private Context context;

    private float[] cp = new float[] { 0, 0, +1.0f}; // camera position
    private float viewMaxDistance = 2.0f;
    private float nearFarRatio = 0.25f;
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private Set<GLModel> models;
    private Set<Object3D> objetos;

    public MainGLRenderer(Context context) {
        this.context = context;

        models = new LinkedHashSet<>();
        objetos = new LinkedHashSet<>();
    }

    public Set<GLModel> getModels() {
        return models;
    }

    public Set<Object3D> getObjetos() {
        return objetos;
    }

    public void addObject(Object3D obj) {
        getObjetos().add(obj);
        getModels().add(obj.getModel());
    }

    private volatile boolean modelsLoaded = true;

    public void requestLoadModels() {
        modelsLoaded = false;
    }

    private void reloadModels() {
        // TODO apagar models já carregados
        for (GLModel model : models) {
            model.load(context);
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
        float far = viewMaxDistance;
        float near = nearFarRatio * far;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, near, far);
    }

    // mVP is an abbreviation for "View Projection Matrix"
    private final float[] mVP = new float[16];

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!modelsLoaded) {
            reloadModels();
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(mViewMatrix, 0, cp[0], cp[1], cp[2], 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mVP, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        for (Object3D obj : objetos) {
            obj.draw(mVP);
        }
    }
}
