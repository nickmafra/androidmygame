package com.nickmafra.mygame;

import android.content.Context;
import android.opengl.Matrix;

import com.nickmafra.mygame.gl.GLModel;
import com.nickmafra.mygame.gl.TextureModel;
import com.nickmafra.util.Vetor;

public class NaveModel implements GLModel {

    private TextureModel bodyModel, propModel;
    private float m = 1000; // massa total (Kg)
    private float[] p1 = new float[] { -1f, -0.25f, 2}; // posição do propulsor 1
    private float[] p2 = new float[] { +1f, -0.25f, 2}; // posição do propulsor 2
    private float[] cm = new float[] { 0, -0.25f, -0.25f}; // centro de massa
    private float mi = m * 0.0005f; // momento de inércia
    private float aMax = 45; // ângulo máximo

    private float[] d1, d2;

    private float f = 0.3f; // força de propulsão
    private float a1, a2; // ângulos

    public NaveModel() {
        bodyModel = new TextureModel("spaceship_body.obj", "white_sand.bmp");
        propModel = new TextureModel("spaceship_prop.obj", "white_sand.bmp");

        d1 = Vetor.subtract(p1, cm);
        d2 = Vetor.subtract(p2, cm);
    }

    public void setModelAssets(String bodyAsset, String propAsset) {
        bodyModel.setModelAsset(bodyAsset);
        propModel.setModelAsset(propAsset);
    }

    public float getAMax() {
        return aMax;
    }

    public void load(Context context) {
        bodyModel.load(context);
        propModel.load(context);
    }

    private float[] mvpMatrix;

    public void setMvpMatrix(float[] mvpMatrix) {
        this.mvpMatrix = mvpMatrix;
    }

    public void setForce(float f) {
        this.f = f;
    }

    public void setAngle1(float a1) {
        if (a1 > aMax) {
            a1 = aMax;
        } else if (a1 < -aMax) {
            a1 = -aMax;
        }
        this.a1 = a1;
    }

    public void setAngle2(float a2) {
        if (a2 > aMax) {
            a2 = aMax;
        } else if (a2 < -aMax) {
            a2 = -aMax;
        }
        this.a2 = a2;
    }

    private static float[] calcP(float f, float a) {
        float[] p = new float[3];
        p[1] = (float) (-f*Math.sin(Math.PI * a / 180));
        p[2] = (float) (-f*Math.cos(Math.PI * a / 180));
        return p;
    }

    public float[] getAcc1() {
        p1 = calcP(f, a1);
        return Vetor.scale(1 / m, Vetor.projection(p1, d1));
    }

    public float[] getAcc2() {
        p2 = calcP(f, a2);
        return Vetor.scale(1 / m, Vetor.projection(p2, d2));
    }

    public float[] getAccAngular1() {
        p1 = calcP(f, a1);
        return Vetor.scale(1 / mi, Vetor.crossProduct(d1, p1));
    }

    public float[] getAccAngular2() {
        p2 = calcP(f, a2);
        return Vetor.scale(1 / mi, Vetor.crossProduct(d2, p2));
    }

    float[] mTemp = new float[16];
    float[] mTemp2 = new float[16];
    float[] mTemp3 = new float[16];
    private void drawProp(float[] d, float a) {
        // mvp' = mvp * translate * rotate
        Matrix.setRotateM(mTemp, 0, a, 1, 0, 0);
        Matrix.setIdentityM(mTemp2, 0);
        Matrix.translateM(mTemp2, 0, d[0], d[1], d[2]);
        Matrix.multiplyMM(mTemp3, 0, mTemp2, 0, mTemp, 0);
        Matrix.multiplyMM(mTemp, 0, mvpMatrix, 0, mTemp3, 0);
        propModel.setMvpMatrix(mTemp);
        propModel.draw();
    }

    public void draw() {
        bodyModel.setMvpMatrix(mvpMatrix);
        bodyModel.draw();

        p1 = calcP(f, a1);
        drawProp(d1, a1);
        p2 = calcP(f, a2);
        drawProp(d2, a2);
    }

}
