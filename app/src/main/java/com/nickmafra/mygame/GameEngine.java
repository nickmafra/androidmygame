package com.nickmafra.mygame;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.nickmafra.gl.MainGLRenderer;
import com.nickmafra.gl.Object3D;
import com.nickmafra.gl.PiramideModel;

public class GameEngine implements Runnable {

    private long framePeriod = 16; // milissegundos entre cada frame

    private Thread thread;
    private MainGLRenderer renderer;
    private GLSurfaceView glView;

    private Object3D piramide;

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

    private long clockTime() {
        return System.currentTimeMillis();
    }

    public void calcTimeStart() {
        ticks = 0;
        startTime = clockTime();
        timeNow = 0;
        lastFrameTime = -framePeriod; // força primeira renderização
    }

    public void calcTimeNow() {
        ticks++;
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

        renderer.getObjetos().clear();
        renderer.getObjetos().add(piramide = new Object3D(new PiramideModel()));
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
            updatePiramide();
            if (timeNow - lastFrameTime > framePeriod) {
                lastFrameTime = timeNow;
                glView.requestRender();
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
        piramide.setTimeNow((float) ticks);
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
