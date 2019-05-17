package com.nickmafra.mygame;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.nickmafra.gl.MainGLRenderer;
import com.nickmafra.gl.Object3D;
import com.nickmafra.gl.PiramideModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine implements Runnable {

    private long tickPeriod = 15; // milissegundos entre cada tick
    private long framePeriod = 20; // milissegundos entre cada frame

    private Thread thread;
    private MainGLRenderer renderer;
    private GLSurfaceView glView;

    private PiramideModel piramideModel;
    private Object3D piramide;
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
        if (stop == false) {
            throw new RuntimeException("Thread is running.");
        }
        if (glView == null) {
            throw new RuntimeException("GLView is not defined.");
        }
        stop = false;

        renderer.getModels().clear();
        renderer.getObjetos().clear();
        startContexto1();
        renderer.requestLoadModels();

        thread = new Thread(this, "GameEngine");
        thread.start();
    }

    private volatile boolean stop = true;

    public void stop() {
        stop = true;
        while (thread.isAlive());
        thread = null;
    }

    public void run() {
        calcTimeStart();
        while (!stop) {
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

    // roteiro do jogo

    private Random random = new Random();

    private void updateObstaculo(Object3D obstaculo) {
        obstaculo.update();
        float scale = (2 + random.nextInt(4)) / 200f;
        obstaculo.setScale(scale, scale, scale);
        float x = (random.nextInt(401) - 200) / 100f;
        float y = (random.nextInt(401) - 200) / 100f;
        obstaculo.setPosition(x, y, -1);
        float vx = (random.nextInt(41) - 20) / 10000f;
        float vy = (random.nextInt(41) - 20) / 10000f;
        float vz = (random.nextInt(91) + 10) / 10000f;
        obstaculo.setVelocity(0, 0, vz);
        float a = (random.nextInt(41) - 20) / 100f;
        obstaculo.setVRotation(0, 0, 1, a);
    }

    private void startContexto1() {
        piramideModel = new PiramideModel();
        renderer.getModels().add(piramideModel);

        piramide = new Object3D(piramideModel);
        piramide.setPosition(0, 0, -0.25f);
        piramide.setScale(0.2f, 0.2f, 0.2f);
        renderer.getObjetos().add(piramide);
        obstaculos = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Object3D obstaculo = new Object3D(piramideModel);
            updateObstaculo(obstaculo);
            obstaculos.add(obstaculo);
        }
        renderer.getObjetos().addAll(obstaculos);
    }

    private void runContexto1() {
        updatePiramide();
        for (Object3D obstaculo : obstaculos) {
            float[] p = obstaculo.getPositionNow();
            if (p[2] >= 1) {
                updateObstaculo(obstaculo);
            }
        }
    }

    // pirâmide

    float x, y, a;
    private volatile int lastTick1;
    private volatile int lastTick2;
    private volatile int lock;

    public void setRotationVector(float x, float y, float a) {
        this.lock = 3;
        this.x = x;
        this.y = y;
        this.a = a;
        lastTick2 = lastTick1;
        lastTick1 = ticks;
    }

    public void unlock() {
        lock = 1;
        float dTick = (lastTick1 - lastTick2);
        if (dTick < 1) {
            dTick = 1;
        }
        a = a / dTick;
    }

    private void updatePiramide() {
        if (lock > 0) {
            piramide.update();
            if (lock == 1 || lock == 3) {
                piramide.setVRotation(-y, x, 0, a);
                lock--;
            } else {
                piramide.setVRotationAngle(0);
            }
        } else {
            float a = piramide.getVRotationAngle();
            if (a != 0) {
                piramide.update();
                if (a * a < 1e-6) {
                    a = 0;
                } else {
                    float resistance = a*a*1e-3f;
                    if (resistance > a) {
                        resistance = a - 1e-3f;
                    }
                    a -= resistance;
                }
                piramide.setVRotationAngle(a);
            }
        }
    }
}
