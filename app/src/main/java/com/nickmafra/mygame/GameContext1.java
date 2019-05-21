package com.nickmafra.mygame;

import android.opengl.Matrix;

import com.nickmafra.mygame.gl.Camera;
import com.nickmafra.mygame.gl.Object3D;
import com.nickmafra.mygame.gl.PiramideModel;
import com.nickmafra.util.Vetor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameContext1 implements GameContext {

    private GameEngine engine;
    private PiramideModel piramideModel;
    private NaveModel naveModel;
    private Object3D player;
    private List<Object3D> obstaculos;

    private float maxDist = 3;
    private float loopSpaceDist = 1000f;

    public GameContext1(GameEngine engine) {
        this.engine = engine;
    }

    private static final Random random = new Random();

    private static float nextRandom(float min, float max) {
        return min + (max - min) * random.nextFloat();
    }

    private float[] nextSpherePosition() {
        float a1 = nextRandom(0, 2 * (float) Math.PI);
        float a2 = nextRandom(0, 2 * (float) Math.PI);
        return new float[] {
                (float) (Math.sin(a1) * Math.cos(a2)),
                (float) (Math.sin(a1) * Math.sin(a2)),
                (float) Math.cos(a1)
        };
    }

    private void recriarObstaculo(Object3D obstaculo, float[] c) {
        obstaculo.update();
        float scale = nextRandom(0.02f, 0.05f);
        obstaculo.setScale(scale, scale, scale);

        float[] p = nextSpherePosition();
        float[] p2 = nextSpherePosition();
        float speed = nextRandom(0, 0.005f);
        float[] v = new float[3];
        for (int i = 0; i < 3; i++) {
            v[i] = speed * (p2[i] - p[i]) / 2;
            p[i] = c[i] + p[i] * maxDist;
        }
        obstaculo.setPosition(p);
        obstaculo.setVelocity(v);

        float a = (random.nextInt(41) - 20) / 100f;
        obstaculo.setVRotation(nextRandom(0, 1), nextRandom(0, 1), nextRandom(0, 1), a);
    }

    private int qtObstaculos = 50;

    @Override
    public void start() {
        piramideModel = new PiramideModel();
        engine.getGlRendererView().getModels().add(piramideModel);

        // player
        engine.getGlRendererView().addObject(player = new Object3D(naveModel = new NaveModel()));
        player.setPosition(0, 0, -0.25f);
        player.setScale(0.2f, 0.2f, 0.2f);
        engine.getGlRendererView().getObjetos().add(player);

        // obstáculos
        obstaculos = new ArrayList<>();
        for (int i = 0; i < qtObstaculos; i++) {
            Object3D obstaculo = new Object3D(piramideModel);
            recriarObstaculo(obstaculo, player.getPosition());
            obstaculos.add(obstaculo);
        }
        engine.getGlRendererView().getObjetos().addAll(obstaculos);

        // câmera
        Camera camera = engine.getGlRendererView().getCamera();
        //camera.setRelative(false);
        camera.setObject(player);
        camera.setLookAtCenter(0.5f, 1.0f, (float) Math.PI / 6);
    }

    @Override
    public void run() {
        updatePlayer();
        engine.getGlRendererView().getCamera().recalcViewMatrix();
        for (Object3D obstaculo : obstaculos) {
            obstaculo.update();
            //obstaculo.setPosition(Vetor.loop(obstaculo.getPosition(), player.getPosition(), loopSpaceDist));
            float k = 2.0f;
            float[] c = Vetor.add(player.getPosition(), Vetor.scale(k, player.getVelocity()));
            float lim = 1.2f * maxDist;
            if (Vetor.distance(obstaculo.getPosition(), c) > lim * lim) {
                recriarObstaculo(obstaculo, c);
            }
        }
    }

    // pirâmide

    float x, y, a;
    private volatile int lastTick1;
    private volatile int lastTick2;
    private volatile int lockRotation;

    public void setPlayerRotation(float x, float y, float a) {
        this.lockRotation = 3;
        this.x = x;
        this.y = y;
        if (x == 0 && y == 0) {
            a = 0;
        }
        this.a = a;
        lastTick2 = lastTick1;
        lastTick1 = engine.getTicks();
    }

    public void unlockRotation() {
        lockRotation = 1;
        float dTick = (lastTick1 - lastTick2);
        if (dTick < 1) {
            dTick = 1;
        }
        a = a / dTick;
    }

    private volatile boolean isAcc1, isAcc2;

    public void setIsAccelerating1(boolean isAcc1) {
        this.isAcc1 = isAcc1;
    }

    public void setIsAccelerating2(boolean isAcc2) {
        this.isAcc2 = isAcc2;
    }

    public void setYProp1(float y) {
        naveModel.setAngle1(-y * naveModel.getAMax());
    }

    public void setYProp2(float y) {
        naveModel.setAngle2(-y * naveModel.getAMax());
    }

    private void girarCamera() {
        if (a == 0) {
            return;
        }
        Camera camera = engine.getGlRendererView().getCamera();
        float[] rmTemp = new float[16];
        Matrix.setRotateM(rmTemp, 0, -a, -y, x, 0);
        float[] rmNew = new float[16];
        Matrix.multiplyMM(rmNew, 0, camera.getRelativeModelMatrix(), 0, rmTemp, 0);
        camera.setRelativeModelMatrix(rmNew);
    }

    private float maxV = 1000f;
    private void acelerarLinear(float[] acc) {
        float[] v = player.getVelocity();
        float[] rNow = player.getRotationNow();
        float[] accTemp = new float[] {acc[0], acc[1], acc[2], 1};
        float[] dV = new float[4];
        Matrix.multiplyMV(dV, 0, rNow, 0, accTemp, 0);
        for (int i = 0; i < 3; i++) {
            v[i] += dV[i];
        }
        float m = Vetor.magnitude(v);
        if (m > maxV) {
            v = Vetor.scale(maxV / m, v);
        }
        player.setVelocity(v);
    }

    private void acelerarAngular(float[] accAngular) {
        float a = Vetor.magnitude(accAngular);
        float[] rmTemp = new float[16];
        Matrix.setRotateM(rmTemp, 0, a, accAngular[0], accAngular[1], accAngular[2]);
        float[] rmTemp2 = new float[16];
        Matrix.multiplyMM(rmTemp2, 0, player.getRotationMatrix(), 0, rmTemp, 0);
        player.setRotationMatrix(rmTemp2);
    }

    private void acelerar1() {
        acelerarLinear(naveModel.getAcc1());
        acelerarAngular(naveModel.getAccAngular1());
    }

    private void acelerar2() {
        acelerarLinear(naveModel.getAcc2());
        acelerarAngular(naveModel.getAccAngular2());
    }

    private void freiar() {
        float k = 10f;
        float[] v = player.getVelocity();
        if (v[0] == 0 && v[1] == 0 && v[2] == 0) {
            return;
        }
        float s2 = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
        float fk = s2 * k;
        if (fk > 1 || s2 < 1.0e-8) {
            fk = 1;
        }
        for (int i = 0; i < 3; i++) {
            v[i] -= v[i] * fk;
        }
        player.setVelocity(v);
    }

    private void updateCamera() {
        Camera camera = engine.getGlRendererView().getCamera();
    }

    private void updatePlayer() {
        player.update();
        // rotação
        if (lockRotation > 0) {
            if (lockRotation == 3) {
                girarCamera();
                lockRotation = 2;
            }
            if (lockRotation == 1) {
                lockRotation = 0;
            }
        }
        // velocidade
        if (isAcc1) {
            acelerar1();
        }
        if (isAcc2) {
            acelerar2();
        }
        freiar();

        //player.setPosition(Vetor.loop(player.getPosition(), loopSpaceDist));
    }

}
