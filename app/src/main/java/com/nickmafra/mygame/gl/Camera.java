package com.nickmafra.mygame.gl;

import android.opengl.Matrix;

import java.util.Arrays;

public class Camera {

    private Object3D obj;
    private float objScale = 1;
    private boolean relative = true;
    private float zoom = 1;
    private float[] rmm = new float[16]; // relative model matrix
    private float[] vm = new float[16]; // view matrix

    private float[] f = new float[] {-1, 1, -1, 1, 0.5f, 6}; // left, right, bottom, top, near, far
    private float[] pm = new float[16]; // projection matrix

    private float[] vpm = new float[16]; // view projection matrix

    public Camera() {
        Matrix.setIdentityM(rmm, 0);
        Matrix.setIdentityM(vm, 0);
        Matrix.setIdentityM(pm, 0);
        Matrix.setIdentityM(vpm, 0);
    }

    public void setObject(Object3D obj) {
        this.obj = obj;
        if (relative) {
            float[] s = obj.getScale();
            if (s[0] == s[1] && s[0] == s[2]) {
                objScale = 1 / s[0];
            } else {
                objScale = 1 / (float) Math.pow(s[0] * s[1] * s[2], 1/3f);
            }
        }
    }

    public void setRelative(boolean relative) {
        this.relative = relative;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public float[] getRelativeModelMatrix() {
        return Arrays.copyOf(rmm, 16);
    }

    public void setRelativeModelMatrix(float[] rmm) {
        this.rmm = rmm;
    }

    public void setLookAt(float eyeX, float eyeY, float eyeZ, float upX, float upY, float upZ) {
        float[] rmmTemp = new float[16];
        Matrix.setLookAtM(rmmTemp, 0, eyeX, eyeY, eyeZ, 0, 0, 0, upX, upY, upZ);
        Matrix.invertM(rmm, 0, rmmTemp, 0);
    }

    public void setLookAtCenter(float eyeY, float eyeZ, float angle) {
        float upY = (float) Math.cos(angle);
        float upZ = (float) -Math.sin(angle);
        float[] rmmTemp = new float[16];
        Matrix.setLookAtM(rmmTemp, 0, 0, eyeY, eyeZ, 0, 0, 0, 0, upY, upZ);
        Matrix.invertM(rmm, 0, rmmTemp, 0);
    }

    public synchronized void recalcViewMatrix() {
        float[] vmTemp = new float[16];
        float[] refMatrix;
        if (relative) {
            float[] rmm2 = Arrays.copyOf(rmm, 16);
            for (int i = 0; i < 4; i++) {
                rmm2[4*i    ] *= objScale;
                rmm2[4*i + 1] *= objScale;
                rmm2[4*i + 2] *= objScale;
            }
            refMatrix = obj.getModelMatrixNow();
            Matrix.multiplyMM(vmTemp, 0, refMatrix, 0, rmm2, 0);
        } else {
            refMatrix = obj.getTranslationNow();
            Matrix.multiplyMM(vmTemp, 0, rmm, 0, refMatrix, 0);
        }
        Matrix.invertM(vm, 0, vmTemp, 0);
        Matrix.scaleM(vm, 0, zoom, zoom, zoom);
    }

    public float[] getViewMatrix() {
        return vm;
    }

    public synchronized void setViewMatrix(float[] vm) {
        this.vm = vm;
    }

    public synchronized void recalcProjectionMatrix() {
        Matrix.frustumM(pm, 0, f[0], f[1], f[2], f[3], f[4], f[5]);
    }

    public void setScreen(float width, float height) {
        if (width >= height) {
            float ratio = height / width;
            f[0] = -1;
            f[1] = 1;
            f[2] = -ratio;
            f[3] = ratio;
        } else {
            float ratio = width / height;
            f[0] = -ratio;
            f[1] = ratio;
            f[2] = -1;
            f[3] = 1;
        }
    }

    public void setNearFar(float near, float far) {
        f[4] = near;
        f[5] = far;
    }

    public float[] getProjectionMatrix() {
        return pm;
    }

    public synchronized void setProjectionMatrix(float[] pm) {
        this.pm = pm;
    }

    public synchronized void recalcViewProjectionMatrix() {
        Matrix.multiplyMM(vpm, 0, pm, 0, vm, 0);
    }

    public float[] getViewProjectionMatrix() {
        return vpm;
    }
}
