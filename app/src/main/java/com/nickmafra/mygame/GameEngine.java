package com.nickmafra.mygame;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.nickmafra.gl.Camera;
import com.nickmafra.gl.MainGLRenderer;
import com.nickmafra.gl.NaveModel;
import com.nickmafra.gl.Object3D;
import com.nickmafra.gl.PiramideModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {

    private long tickPeriod = 15; // milissegundos entre cada tick
    private long framePeriod = 20; // milissegundos entre cada frame

    private volatile Thread thread;
    private MainGLRenderer renderer;
    private GLSurfaceView glView;

    private PiramideModel piramideModel;
    private Object3D player;
    private List<Object3D> obstaculos;

    public GameEngine(Context context) {
        renderer = new MainGLRenderer(context);
    }

    public GLSurfaceView.Renderer getRenderer() {
        return renderer;
    }

    public void setGlView(GLSurfaceView glView) {
        this.glView = glView;
    }

    // time

    private int ticks;
    private long startTime;
    private long timeNow;
    private long lastTime;
    private long lastFrameTime;
    private long lastTickTime;

    private long clockTime() {
        return System.currentTimeMillis();
    }

    public void calcTimeStart() {
        ticks = 0;
        startTime = clockTime();
        timeNow = 0;
        lastFrameTime = -framePeriod; // força primeiro frame
        lastTickTime = -tickPeriod; // força primeiro tick
    }

    public void calcTimeNow() {
        lastTime = timeNow;
        timeNow = clockTime() - startTime;
    }

    public synchronized void start() {
        if (thread != null) {
            throw new RuntimeException("Thread is running.");
        }
        if (glView == null) {
            throw new RuntimeException("GLView is not defined.");
        }

        renderer.getModels().clear();
        renderer.getObjetos().clear();
        startContexto1();
        renderer.requestLoadModels();

        thread = new GameThread();
        thread.start();
    }

    public synchronized void stop() {
        if (thread == null) {
            return;
        }
        thread.interrupt();
        while (thread.isAlive());
        thread = null;
    }

    private class GameThread extends Thread {

        public GameThread() {
            super("GameThread");
        }

        @Override
        public void run() {
            calcTimeStart();
            while (!isInterrupted()) {
                calcTimeNow();
                if (timeNow - lastTickTime > tickPeriod) {
                    lastTickTime = timeNow;
                    ticks++;
                    for (Object3D obj : renderer.getObjetos()) {
                        obj.setTimeNow(ticks);
                    }
                    runContexto1();
                }
                calcTimeNow();
                if (timeNow - lastFrameTime > framePeriod) {
                    lastFrameTime = timeNow;
                    glView.requestRender();
                }
            }
        }
    }

    // roteiro do jogo

    private float maxDist = 2;

    private Random random = new Random();

    private float nextRandom(float min, float max) {
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

    private void recriarObstaculo(Object3D obstaculo) {
        float[] ppNow = player.getPositionNow();
        obstaculo.update();
        float scale = nextRandom(0.02f, 0.05f);
        obstaculo.setScale(scale, scale, scale);

        float[] p = nextSpherePosition();
        float[] p2 = nextSpherePosition();
        float speed = nextRandom(0.001f, 0.010f);
        float[] v = new float[3];
        for (int i = 0; i < 3; i++) {
            v[i] = speed * (p2[i] - p[i]) / 2;
            p[i] = p[i] * maxDist + ppNow[i];
        }
        obstaculo.setPosition(p);
        obstaculo.setVelocity(v);

        float a = (random.nextInt(41) - 20) / 100f;
        obstaculo.setVRotation(nextRandom(0, 1), nextRandom(0, 1), nextRandom(0, 1), a);
    }

    private void startContexto1() {
        piramideModel = new PiramideModel();
        renderer.getModels().add(piramideModel);

        // player
        renderer.addObject(player = new Object3D(new NaveModel()));
        player.setPosition(0, 0, -0.25f);
        player.setScale(0.2f, 0.2f, 0.2f);
        renderer.getObjetos().add(player);

        // obstáculos
        obstaculos = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Object3D obstaculo = new Object3D(piramideModel);
            recriarObstaculo(obstaculo);
            obstaculos.add(obstaculo);
        }
        renderer.getObjetos().addAll(obstaculos);

        // câmera
        Camera camera = renderer.getCamera();
        camera.setObject(player);
        camera.setLookAtCenter(0.5f, 1.0f, (float) Math.PI / 6);
    }

    private void runContexto1() {
        updatePlayer();
        renderer.getCamera().recalcViewMatrix();
        for (Object3D obstaculo : obstaculos) {
            float[] p = obstaculo.getPositionNow();
            for (int i = 0; i < 3; i++) {
                p[i] -= player.getPosition()[i];
            }
            float lim = 1.5f * maxDist;
            if (p[0]*p[0] + p[1]*p[1] + p[2]*p[2] > lim * lim) {
                recriarObstaculo(obstaculo);
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
        lastTick1 = ticks;
    }

    public void unlockRotation() {
        lockRotation = 1;
        float dTick = (lastTick1 - lastTick2);
        if (dTick < 1) {
            dTick = 1;
        }
        a = a / dTick;
    }

    private volatile boolean isAcc;

    public void setIsAccelerating(boolean isAcc) {
        this.isAcc = isAcc;
    }

    private void girar() {
        if (a == 0) {
            return;
        }
        float[] rmTemp = new float[16];
        Matrix.setRotateM(rmTemp, 0, a, -y, x, 0);
        float[] rmNew = new float[16];
        Matrix.multiplyMM(rmNew, 0, player.getRotationMatrix(), 0, rmTemp, 0);
        player.setRotationMatrix(rmNew);
    }

    private void acelerar(float acc) {
        if (acc < 0) {
            return;
        }
        float k = 0.0003f;
        float[] rNow = player.getRotationNow();
        float[] vDir = new float[] {0, 0, -1, 0};
        float[] dV = new float[4];
        Matrix.multiplyMV(dV, 0, rNow, 0, vDir, 0);
        float[] v = player.getVelocity();
        float s2 = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
        if (s2 > 1000) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            v[i] += dV[i] * acc * k;
        }
        player.setVelocity(v);
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

    private void updatePlayer() {
        player.update();
        float acc = 1.0f;
        // rotação
        if (lockRotation > 0) {
            if (lockRotation == 3) {
                acc = 1 - a;
                if (acc < 0) {
                    acc = 0;
                }
                girar();
                lockRotation = 2;
            }
            if (lockRotation == 1) {
                lockRotation = 0;
            }
        }
        // velocidade
        if (isAcc) {
            acelerar(acc);
        }
        freiar();
    }
}
