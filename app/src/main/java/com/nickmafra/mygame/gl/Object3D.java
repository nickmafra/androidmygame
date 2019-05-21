package com.nickmafra.mygame.gl;

import android.opengl.Matrix;

import java.util.Arrays;

public class Object3D {

    private GLModel model;

    // propriedades estáticas
    private float[] p; // position (model translation)
    private float[] rm; // rotation matrix
    private float[] s; // scale
    private float timeRef;
    private float timeNow;

    // propriedades dinâmicas
    private float[] v; // linear velocity (model translation)
    private float[] vr; // velocity of rotation: x, y, z, a

    public Object3D() {
        p = new float[3];
        rm = new float[16];
        Matrix.setIdentityM(rm, 0);
        s = new float[]{ 1, 1, 1 };
        v = new float[3];
        vr = new float[4];
    }

    public Object3D(GLModel model) {
        this();
        this.model = model;
    }

    public GLModel getModel() {
        return model;
    }

    public float[] getPosition() {
        return p;
    }

    public synchronized void setPosition(float[] p) {
        this.p = p;
    }

    public synchronized void setPosition(float x, float y, float z) {
        this.p = new float[] { x, y, z };
    }

    public float[] getRotationMatrix() {
        return rm;
    }

    public synchronized void setRotationMatrix(float[] rm) {
        this.rm = rm;
    }

    /**
     * Define a rotação do modelo a partir de um eixo e um ângulo.
     *
     * @param x coordenada x do eixo
     * @param y coordenada y do eixo
     * @param z coordenada z do eixo
     * @param a ângulo de rotação, em graus
     */
    public synchronized void setRotation(float x, float y, float z, float a) {
        if (x == 0 && y == 0 && z == 0) {
            a = 0; // evita rotação sobre eixo nulo
        }
        if (a == 0) {
            Matrix.setIdentityM(rm, 0);
        } else {
            Matrix.setRotateM(rm, 0, a, x, y, z);
        }
    }

    public float[] getScale() {
        return s;
    }

    public synchronized void setScale(float x, float y, float z) {
        this.s = new float[] { x, y, z };
    }

    public float getTimeRef() {
        return timeRef;
    }

    public synchronized void setTimeRef(float timeRef) {
        this.timeRef = timeRef;
    }

    public synchronized void setTimeNow(float timeNow) {
        this.timeNow = timeNow;
    }

    public float[] getVRotationAxis() {
        return vr;
    }

    public float getVRotationAngle() {
        return vr[3];
    }

    public synchronized void setVRotationAngle(float a) {
        vr[3] = a;
    }

    /**
     * Define a velocidade de rotação do modelo a partir de um eixo e uma velocidade angular.
     *
     * @param x coordenada x do eixo
     * @param y coordenada y do eixo
     * @param z coordenada z do eixo
     * @param a velocidade angular, em graus por unidade de tempo
     */
    public synchronized void setVRotation(float x, float y, float z, float a) {
        if (x == 0 && y == 0 && z == 0) {
            a = 0; // evita rotação sobre eixo nulo
        }
        if (Float.isNaN(a) || Float.isInfinite(a)) {
            a = 0;
        }
        vr[0] = x;
        vr[1] = y;
        vr[2] = z;
        vr[3] = a;
    }

    public float[] getVelocity() {
        return Arrays.copyOf(v, 3);
    }

    public synchronized void setVelocity(float x, float y, float z) {
        this.v = new float[] { x, y, z };
    }

    public synchronized void setVelocity(float[] v) {
        this.v = v;
    }

    public float[] getPositionNow() {
        float dt = timeNow - timeRef;
        float[] pNow = new float[3];
        pNow[0] = p[0] + dt * v[0];
        pNow[1] = p[1] + dt * v[1];
        pNow[2] = p[2] + dt * v[2];
        return pNow;
    }

    public float[] getTranslationNow() {
        float dt = timeNow - timeRef;
        float[] tNow = new float[16];
        Matrix.setIdentityM(tNow, 0);
        Matrix.translateM(tNow, 0, p[0] + dt * v[0], p[1] + dt * v[1], p[2] + dt * v[2]);
        return tNow;
    }

    public float[] getRotationNow() {
        float da = (timeNow - timeRef) * vr[3]; // dt * a
        if (da == 0) {
            return Arrays.copyOf(rm, 16);
        }
        float[] rmNow = new float[16];
        float[] rmTemp = new float[16];
        Matrix.setRotateM(rmTemp, 0, da, vr[0], vr[1], vr[2]);
        Matrix.multiplyMM(rmNow, 0, rmTemp, 0, rm, 0);
        return rmNow;
    }

    public synchronized void update() {
        p = getPositionNow();
        rm = getRotationNow();
        timeRef = timeNow;
    }

    public float[] getModelMatrixNow() {
        float[] mmTemp = Arrays.copyOf(getRotationNow(), 16);
        Matrix.scaleM(mmTemp, 0, s[0], s[1], s[2]);
        float[] mmNow = new float[16];
        Matrix.multiplyMM(mmNow, 0, getTranslationNow(), 0, mmTemp, 0);
        return mmNow;
    }

    public synchronized void draw(float[] vpMatrix) {
        float[] mmNow = getModelMatrixNow();
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, mmNow, 0);
        model.setMvpMatrix(mvpMatrix);
        model.draw();
    }
}
