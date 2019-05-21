package com.nickmafra.mygame.android;

import static android.opengl.GLES20.*;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.nickmafra.mygame.gl.Camera;
import com.nickmafra.mygame.gl.GLModel;
import com.nickmafra.mygame.gl.Object3D;
import com.nickmafra.mygame.GameEngine;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRendererView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private Camera camera;
    private Set<GLModel> models;
    private Set<Object3D> objetos;

    public GLRendererView(Context context, GameEngine gameEngine) {
        super(context);

        camera = new Camera();
        models = new LinkedHashSet<>();
        objetos = new LinkedHashSet<>();

        setEGLContextClientVersion(3);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        setOnTouchListener(new GameListener(gameEngine));
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
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
            model.load(getContext());
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

        camera.setScreen(width, height);
        camera.recalcProjectionMatrix();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!modelsLoaded) {
            reloadModels();
        }
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        camera.recalcViewProjectionMatrix();
        float[] vpm = camera.getViewProjectionMatrix();

        for (Object3D obj : objetos) {
            obj.draw(vpm);
        }
    }
}
