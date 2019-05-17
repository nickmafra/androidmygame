package com.nickmafra.gl;

import android.opengl.Matrix;

import java.util.Arrays;

public class Object3D {

    private GLModel model;

    // propriedades estáticas
    private float[] p; // position (translation)
    private float[] rm; // rotation matrix
    private float[] s; // scale
    private float timeRef;
    private float timeNow;

    // propriedades dinâmicas
    private float[] v; // linear velocity (translation)
    private float[] vr; // velocity of rotation: x, y, z, a

    public Object3D() {
        this.model = model;
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

    /**
     * Calcula a model matrix.
     *
     * @param p   as coordenadas de posição (translação)
     * @param rm  a matrix de rotação
     * @param s   as coordenadas de escala
     * @param v   as coordenadas de velocidade linear
     * @param vr  as coordenadas de velocidade angular
     * @param dt  o tempo decorrido
     * @return a model matrix
     */
    private static float[] calcModelMatrixNow(float[] p, float[] rm, float[] s, float[] v, float[] vr, float dt) {
        float[] rmm = new float[16];
        Matrix.setIdentityM(rmm, 0);
        Matrix.scaleM(rmm, 0, s[0], s[1], s[2]);
        // rotação
        Matrix.multiplyMM(rmm, 0, rm, 0, rmm, 0);
        float[] rvm = new float[16];
        float da = dt * vr[3];
        if (da != 0) {
            Matrix.rotateM(rmm, 0, da, vr[0], vr[1], vr[2]);
        }
        // translação
        Matrix.translateM(rmm, 0, p[0] + dt * v[0], p[1] + dt * v[1], p[2] + dt * v[2]);
        return rmm;
    }

    public float[] getPosition() {
        return Arrays.copyOf(p, 3);
    }

    public void setPosition(float x, float y, float z) {
        this.p = new float[] { x, y, z };
    }

    public float[] getRotationMatrix() {
        return Arrays.copyOf(rm, 16);
    }

    /**
     * Define a rotação do modelo a partir de um eixo e um ângulo.
     *
     * @param x coordenada x do eixo
     * @param y coordenada y do eixo
     * @param z coordenada z do eixo
     * @param a ângulo de rotação, em graus
     */
    public void setRotation(float x, float y, float z, float a) {
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
        return Arrays.copyOf(s, 3);
    }

    public void setScale(float x, float y, float z) {
        this.s = new float[] { x, y, z };
    }

    public float getTimeRef() {
        return timeRef;
    }

    public void setTimeRef(float timeRef) {
        this.timeRef = timeRef;
    }

    public void setTimeNow(float timeNow) {
        this.timeNow = timeNow;
    }

    public float[] getVRotationAxis() {
        return Arrays.copyOf(vr, 3);
    }

    public float getVRotationAngle() {
        return vr[3];
    }

    public void setVRotationAngle(float a) {
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
    public void setVRotation(float x, float y, float z, float a) {
        if (x == 0 && y == 0 && z == 0) {
            a = 0; // evita rotação sobre eixo nulo
        }
        vr[0] = x;
        vr[1] = y;
        vr[2] = z;
        vr[3] = a;
    }

    public float[] getVelocity() {
        return Arrays.copyOf(v, 3);
    }

    public void setVelocity(float x, float y, float z) {
        this.v = new float[] { x, y, z };
    }

    public float[] getPositionNow() {
        float dt = timeNow - timeRef;
        if (dt == 0) {
            return getPosition();
        }
        float[] pNow = new float[3];
        pNow[0] = p[0] + dt * v[0];
        pNow[1] = p[1] + dt * v[1];
        pNow[2] = p[2] + dt * v[2];
        return pNow;
    }

    public float[] getRotationNow() {
        float da = (timeNow - timeRef) * vr[3]; // dt * a
        if (da == 0) {
            return getRotationMatrix();
        }
        float[] rmNow = new float[16];
        Matrix.setRotateM(rmNow, 0, da, vr[0], vr[1], vr[2]);
        Matrix.multiplyMM(rmNow, 0, rmNow, 0, rm, 0);
        return rmNow;
    }

    public float[] getModelMatrixNow() {
        float dt = timeNow - timeRef;
        return calcModelMatrixNow(p, rm, s, v, vr, dt);
    }

    public synchronized void update() {
        p = getPositionNow();
        rm = getRotationNow();
        timeRef = timeNow;
    }

    public void draw(float[] mVP) {
        float[] mmNow = getModelMatrixNow();
        float[] mMVP = new float[16];
        Matrix.multiplyMM(mMVP, 0, mVP, 0, mmNow, 0);
        model.setMvpMatrix(mMVP);
        model.draw();
    }
}
